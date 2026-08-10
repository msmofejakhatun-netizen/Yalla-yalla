package com.example.data.models

object BackendCodeRepository {

    private const val D = "$"

    val razorpayBackendSnippet = """
// ============================================================================
// RAZORPAY PAYMENT GATEWAY BACKEND INTEGRATION (Node.js / Express + TypeScript)
// ============================================================================

import express, { Request, Response } from 'express';
import Razorpay from 'razorpay';
import crypto from 'crypto';
import { Pool } from 'pg'; // PostgreSQL Client

const app = express();
app.use(express.json());

const db = new Pool({ connectionString: process.env.DATABASE_URL });

const razorpay = new Razorpay({
  key_id: process.env.RAZORPAY_KEY_ID!,
  key_secret: process.env.RAZORPAY_KEY_SECRET!,
});

/**
 * 1. CREATE ORDER API
 * Client calls this endpoint before opening Razorpay Checkout.
 */
app.post('/api/v1/payments/create-order', async (req: Request, res: Response) => {
  const { orderId, amountInInr, idempotencyKey } = req.body;

  try {
    // Check idempotency in DB
    const existingPayment = await db.query(
      'SELECT * FROM payments WHERE order_id = ${D}1 AND payment_status = ${D}2',
      [orderId, 'INITIATED']
    );

    if (existingPayment.rows.length > 0) {
      return res.status(200).json({
        success: true,
        razorpayOrderId: existingPayment.rows[0].razorpay_order_id,
        amount: existingPayment.rows[0].amount_in_paise,
      });
    }

    const amountInPaise = Math.round(amountInInr * 100);

    // Call Razorpay API
    const rzpOrder = await razorpay.orders.create({
      amount: amountInPaise,
      currency: 'INR',
      receipt: `rcpt_order_${D}{orderId}`,
      notes: { order_id: orderId, idempotency_key: idempotencyKey },
    });

    // Save payment intent to DB
    await db.query(
      `INSERT INTO payments (order_id, razorpay_order_id, payment_status, amount_in_paise)
       VALUES (${D}1, ${D}2, 'INITIATED', ${D}3)`,
      [orderId, rzpOrder.id, amountInPaise]
    );

    return res.status(200).json({
      success: true,
      razorpayOrderId: rzpOrder.id,
      amount: amountInPaise,
      keyId: process.env.RAZORPAY_KEY_ID,
    });
  } catch (error: any) {
    console.error('Razorpay Order Creation Failed:', error);
    return res.status(500).json({ error: 'Payment initialization failed', details: error.message });
  }
});

/**
 * 2. RAZORPAY WEBHOOK VERIFICATION & PAYMENT CAPTURE
 * Asynchronous webhook guarantees order confirmation even if user closes browser app.
 */
app.post('/api/v1/webhooks/razorpay', async (req: Request, res: Response) => {
  const webhookSecret = process.env.RAZORPAY_WEBHOOK_SECRET!;
  const signatureHeader = req.headers['x-razorpay-signature'] as string;

  // Verify HMAC SHA256 Signature
  const expectedSignature = crypto
    .createHmac('sha256', webhookSecret)
    .update(JSON.stringify(req.body))
    .digest('hex');

  if (expectedSignature !== signatureHeader) {
    console.error('Invalid Webhook Signature');
    return res.status(400).json({ error: 'Invalid HMAC signature' });
  }

  const event = req.body;
  const eventId = event.event_id || req.headers['x-razorpay-event-id'];

  // Check Idempotency Table
  const eventCheck = await db.query('SELECT id FROM webhook_events WHERE event_id = ${D}1', [eventId]);
  if (eventCheck.rows.length > 0) {
    return res.status(200).json({ status: 'already_processed' });
  }

  // Record Webhook Event
  await db.query(
    'INSERT INTO webhook_events (event_id, source_provider, event_type, payload_json) VALUES (${D}1, ${D}2, ${D}3, ${D}4)',
    [eventId, 'RAZORPAY', event.event, JSON.stringify(event)]
  );

  if (event.event === 'payment.captured' || event.event === 'order.paid') {
    const paymentEntity = event.payload.payment.entity;
    const razorpayOrderId = paymentEntity.order_id;
    const razorpayPaymentId = paymentEntity.id;
    const paymentMethod = paymentEntity.method;

    // Begin Database Transaction
    const client = await db.connect();
    try {
      await client.query('BEGIN');

      // Update Payments Table
      const updatePayment = await client.query(
        `UPDATE payments
         SET razorpay_payment_id = ${D}1, payment_status = 'CAPTURED', payment_method = ${D}2
         WHERE razorpay_order_id = ${D}3 RETURNING order_id`,
        [razorpayPaymentId, paymentMethod.toUpperCase(), razorpayOrderId]
      );

      if (updatePayment.rows.length > 0) {
        const orderId = updatePayment.rows[0].order_id;

        // Update Order Status to PAYMENT_CONFIRMED
        await client.query(
          "UPDATE orders SET status = 'PAYMENT_CONFIRMED' WHERE id = ${D}1",
          [orderId]
        );

        // Auto-Trigger Restaurant Notification & Hyperlocal Dispatcher Pipeline
        console.log(`Order ${D}{orderId} confirmed via Razorpay Webhook. Triggering merchant prep.`);
      }

      await client.query("UPDATE webhook_events SET processed = TRUE, processed_at = NOW() WHERE event_id = ${D}1", [eventId]);
      await client.query('COMMIT');
    } catch (txError) {
      await client.query('ROLLBACK');
      console.error('Transaction Failed during payment capture:', txError);
    } finally {
      client.release();
    }
  }

  return res.status(200).json({ status: 'ok' });
});

/**
 * 3. INSTANT REFUND ENGINE
 * Triggered on restaurant order cancellation or runner unavailability timeout.
 */
export async function processInstantRefund(orderId: string, reason: string) {
  const client = await db.connect();
  try {
    await client.query('BEGIN');

    // Fetch Payment details
    const payRes = await client.query(
      "SELECT * FROM payments WHERE order_id = ${D}1 AND payment_status = 'CAPTURED'",
      [orderId]
    );

    if (payRes.rows.length === 0) {
      console.log(`No captured payment found for order ${D}{orderId}. Skipping refund.`);
      await client.query('ROLLBACK');
      return { success: false, message: 'No captured payment found' };
    }

    const payment = payRes.rows[0];

    // Call Razorpay Refund API with speed: "instant"
    const refund = await razorpay.payments.refund(payment.razorpay_payment_id, {
      amount: payment.amount_in_paise,
      speed: 'instant',
      notes: { reason, order_id: orderId },
    });

    // Record Refund
    await client.query(
      `INSERT INTO payment_refunds (payment_id, order_id, razorpay_refund_id, amount_in_paise, refund_status, speed, reason)
       VALUES (${D}1, ${D}2, ${D}3, ${D}4, 'PROCESSED', 'INSTANT', ${D}5)`,
      [payment.id, orderId, refund.id, payment.amount_in_paise, reason]
    );

    // Update Payment & Order Status
    await client.query("UPDATE payments SET payment_status = 'REFUNDED' WHERE id = ${D}1", [payment.id]);
    await client.query("UPDATE orders SET status = 'CANCELLED_REFUNDED' WHERE id = ${D}1", [orderId]);

    await client.query('COMMIT');
    return { success: true, refundId: refund.id, amount: payment.amount_in_paise / 100 };
  } catch (error: any) {
    await client.query('ROLLBACK');
    console.error('Instant Refund Error:', error);
    throw error;
  } finally {
    client.release();
  }
}
    """.trimIndent()

    val deliveryBackendSnippet = """
// ============================================================================
// HYPERLOCAL DELIVERY AGGREGATOR CLIENT & FAILOVER DISPATCHER (Dunzo & Porter)
// ============================================================================

import axios from 'axios';
import { Pool } from 'pg';
import { processInstantRefund } from './razorpayService';

const db = new Pool({ connectionString: process.env.DATABASE_URL });

const DUNZO_BASE_URL = 'https://api.dunzo.in/api/v2';
const PORTER_BASE_URL = 'https://public.backend.porter.in/v1';

export interface LocationSpec {
  lat: number;
  lng: number;
  address: string;
}

/**
 * 1. GET ESTIMATED DELIVERY QUOTE (Dunzo + Porter Comparison)
 */
export async function getHyperlocalDeliveryQuotes(pickup: LocationSpec, dropoff: LocationSpec) {
  const results = await Promise.allSettled([
    // Dunzo Quote Call
    axios.post(`${D}{DUNZO_BASE_URL}/quote`, {
      pickup_details: { lat: pickup.lat, lng: pickup.lng },
      drop_details: { lat: dropoff.lat, lng: dropoff.lng },
    }, { headers: { 'client-id': process.env.DUNZO_CLIENT_ID, 'client-secret': process.env.DUNZO_CLIENT_SECRET } }),

    // Porter Quote Call
    axios.post(`${D}{PORTER_BASE_URL}/orders/quote`, {
      pickup_details: { lat: pickup.lat, lng: pickup.lng },
      drop_details: { lat: dropoff.lat, lng: dropoff.lng },
    }, { headers: { 'x-api-key': process.env.PORTER_API_KEY } })
  ]);

  const quotes = [];

  if (results[0].status === 'fulfilled') {
    const data = results[0].value.data;
    quotes.push({
      provider: 'DUNZO',
      cost: data.estimated_price / 100, // paise to INR
      etaMinutes: data.estimated_delivery_time_in_minutes || 25,
      isAvailable: true
    });
  }

  if (results[1].status === 'fulfilled') {
    const data = results[1].value.data;
    quotes.push({
      provider: 'PORTER',
      cost: data.fare.minor_amount / 100,
      etaMinutes: data.eta.minutes || 28,
      isAvailable: true
    });
  }

  return quotes;
}

/**
 * 2. AUTOMATED RIDER DISPATCH WITH FAILOVER RETRY ENGINE
 * Called when restaurant accepts order and starts preparation.
 */
export async function dispatchRiderWithFailover(orderId: string) {
  const orderRes = await db.query(
    `SELECT o.id, o.delivery_address_json, r.lat as rest_lat, r.lng as rest_lng, r.address_text as rest_address, r.name as rest_name
     FROM orders o JOIN restaurants r ON o.restaurant_id = r.id WHERE o.id = ${D}1`,
    [orderId]
  );

  const orderData = orderRes.rows[0];
  const dropoff = JSON.parse(orderData.delivery_address_json);
  const pickup = { lat: parseFloat(orderData.rest_lat), lng: parseFloat(orderData.rest_lng), address: orderData.rest_address };

  // Primary Provider: DUNZO
  try {
    console.log(`[Attempt 1] Booking Dunzo Rider for order ${D}{orderId}...`);
    const dunzoBooking = await axios.post(`${D}{DUNZO_BASE_URL}/tasks`, {
      request_id: `dunzo_req_${D}{orderId}`,
      pickup_details: { location: { lat: pickup.lat, lng: pickup.lng }, address: { street: pickup.address } },
      drop_details: { location: { lat: dropoff.lat, lng: dropoff.lng }, address: { street: dropoff.address } },
    }, { headers: { 'client-id': process.env.DUNZO_CLIENT_ID, 'client-secret': process.env.DUNZO_CLIENT_SECRET } });

    const taskId = dunzoBooking.data.task_id;

    await db.query(
      `INSERT INTO delivery_assignments (order_id, provider_name, external_task_id, assignment_status, delivery_cost, fallback_attempt_count)
       VALUES (${D}1, 'DUNZO', ${D}2, 'BOOKING_REQUESTED', ${D}3, 1)
       ON CONFLICT (order_id) DO UPDATE SET provider_name = 'DUNZO', external_task_id = ${D}2, assignment_status = 'BOOKING_REQUESTED'`,
      [orderId, taskId, 45.00]
    );

    return { provider: 'DUNZO', taskId };

  } catch (dunzoError) {
    console.warn(`Dunzo Booking failed for order ${D}{orderId}. Falling back to Porter...`);

    // Secondary Provider Fallback: PORTER
    try {
      console.log(`[Attempt 2] Booking Porter Rider for order ${D}{orderId}...`);
      const porterBooking = await axios.post(`${D}{PORTER_BASE_URL}/orders/create`, {
        request_id: `porter_req_${D}{orderId}`,
        pickup_details: { lat: pickup.lat, lng: pickup.lng, address: pickup.address },
        drop_details: { lat: dropoff.lat, lng: dropoff.lng, address: dropoff.address },
      }, { headers: { 'x-api-key': process.env.PORTER_API_KEY } });

      const taskId = porterBooking.data.order_id;

      await db.query(
        `INSERT INTO delivery_assignments (order_id, provider_name, external_task_id, assignment_status, delivery_cost, fallback_attempt_count)
         VALUES (${D}1, 'PORTER', ${D}2, 'BOOKING_REQUESTED', ${D}3, 2)
         ON CONFLICT (order_id) DO UPDATE SET provider_name = 'PORTER', external_task_id = ${D}2, assignment_status = 'BOOKING_REQUESTED'`,
        [orderId, taskId, 48.00]
      );

      return { provider: 'PORTER', taskId };

    } catch (porterError) {
      console.error(`ALL Hyperlocal Riders Unavailable for order ${D}{orderId}! Initiating instant refund rollback.`);

      await db.query("UPDATE orders SET status = 'CANCELLED' WHERE id = ${D}1", [orderId]);
      await processInstantRefund(orderId, 'Rider Unavailability across Dunzo & Porter');

      return { provider: 'NONE', error: 'No runner available. Order cancelled and refunded.' };
    }
  }
}

/**
 * 3. LOGISTICS WEBHOOK RECEIVER FOR LIVE TRACKING & TELEMETRY LOGS
 */
export async function handleDeliveryWebhook(provider: string, payload: any) {
  const taskId = payload.task_id || payload.order_id;
  const status = payload.state || payload.status; // e.g. RIDER_ASSIGNED, OUT_FOR_DELIVERY, DELIVERED
  const rider = payload.runner || payload.driver; // { name, phone, lat, lng }

  const assignRes = await db.query(
    'SELECT id, order_id FROM delivery_assignments WHERE external_task_id = ${D}1',
    [taskId]
  );

  if (assignRes.rows.length === 0) return;

  const assignment = assignRes.rows[0];

  // Update Assignment
  await db.query(
    `UPDATE delivery_assignments
     SET assignment_status = ${D}1, rider_name = ${D}2, rider_phone = ${D}3, updated_at = NOW()
     WHERE id = ${D}4`,
    [status, rider?.name || 'Assigned Rider', rider?.phone || '9876543210', assignment.id]
  );

  // Insert High-Frequency Tracking Telemetry Log
  await db.query(
    `INSERT INTO delivery_tracking_logs (delivery_assignment_id, current_status, rider_lat, rider_lng, eta_minutes, raw_webhook_payload)
     VALUES (${D}1, ${D}2, ${D}3, ${D}4, ${D}5, ${D}6)`,
    [assignment.id, status, rider?.lat, rider?.lng, payload.eta_mins || 15, JSON.stringify(payload)]
  );

  // Synchronize Order Status
  if (status === 'DELIVERED') {
    await db.query("UPDATE orders SET status = 'DELIVERED' WHERE id = ${D}1", [assignment.order_id]);
  } else if (status === 'OUT_FOR_DELIVERY' || status === 'DISPATCHED') {
    await db.query("UPDATE orders SET status = 'OUT_FOR_DELIVERY' WHERE id = ${D}1", [assignment.order_id]);
  }
}
    """.trimIndent()
}
