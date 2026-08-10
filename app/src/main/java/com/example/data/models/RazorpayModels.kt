package com.example.data.models

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

enum class PaymentMethodType {
    UPI, CARD, NETBANKING, WALLET
}

enum class RazorpayPaymentStatus {
    CREATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED
}

data class RazorpayOrderRequest(
    val amountInPaise: Long,
    val currency: String = "INR",
    val receipt: String,
    val notes: Map<String, String> = emptyMap()
)

data class RazorpayOrderResponse(
    val id: String,
    val entity: String = "order",
    val amount: Long,
    val amountPaid: Long = 0,
    val amountDue: Long,
    val currency: String = "INR",
    val receipt: String,
    val status: String = "created",
    val createdAt: Long = System.currentTimeMillis() / 1000
)

data class RazorpayPaymentSuccessCallback(
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String
)

data class RazorpayRefundResponse(
    val id: String,
    val entity: String = "refund",
    val amount: Long,
    val currency: String = "INR",
    val paymentId: String,
    val speed: String = "instant",
    val status: String = "processed",
    val createdAt: Long = System.currentTimeMillis() / 1000
)

object RazorpayCryptoUtils {
    /**
     * Generates HMAC-SHA256 signature according to Razorpay docs:
     * generated_signature = hmac_sha256(order_id + "|" + payment_id, secret)
     */
    fun calculateHmacSha256(data: String, secret: String): String {
        return try {
            val sha256HMAC = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
            sha256HMAC.init(secretKey)
            val bytes = sha256HMAC.doFinal(data.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "error_signature_generation"
        }
    }

    /**
     * Verifies Payment Signature from Client SDK Callback
     */
    fun verifyPaymentSignature(
        orderId: String,
        paymentId: String,
        receivedSignature: String,
        secret: String
    ): Boolean {
        val payload = "$orderId|$paymentId"
        val expectedSignature = calculateHmacSha256(payload, secret)
        return expectedSignature.equals(receivedSignature, ignoreCase = true)
    }

    /**
     * Verifies Webhook Signature header: X-Razorpay-Signature
     */
    fun verifyWebhookSignature(
        webhookBody: String,
        webhookSignatureHeader: String,
        webhookSecret: String
    ): Boolean {
        val expectedSignature = calculateHmacSha256(webhookBody, webhookSecret)
        return expectedSignature.equals(webhookSignatureHeader, ignoreCase = true)
    }
}
