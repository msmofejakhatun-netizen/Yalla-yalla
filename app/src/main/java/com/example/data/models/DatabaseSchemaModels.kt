package com.example.data.models

data class TableSchema(
    val tableName: String,
    val description: String,
    val primaryKey: String,
    val foreignKeys: List<String>,
    val fields: List<FieldSchema>,
    val sqlDdl: String
)

data class FieldSchema(
    val name: String,
    val dataType: String,
    val isNullable: Boolean,
    val description: String
)

object DatabaseSchemas {
    val allTables = listOf(
        TableSchema(
            tableName = "restaurants",
            description = "Stores merchant profiles, coordinates, operational status, and commission rates.",
            primaryKey = "id (UUID)",
            foreignKeys = emptyList(),
            fields = listOf(
                FieldSchema("id", "UUID", false, "Primary Key"),
                FieldSchema("name", "VARCHAR(255)", false, "Restaurant Name"),
                FieldSchema("lat", "NUMERIC(10, 8)", false, "Latitude for hyperlocal radius calculations"),
                FieldSchema("lng", "NUMERIC(11, 8)", false, "Longitude for hyperlocal radius calculations"),
                FieldSchema("address_text", "TEXT", false, "Full address for rider pickup"),
                FieldSchema("phone", "VARCHAR(20)", false, "Merchant contact number"),
                FieldSchema("is_active", "BOOLEAN", false, "Accepting orders toggle"),
                FieldSchema("commission_pct", "NUMERIC(4, 2)", false, "Platform commission percentage"),
                FieldSchema("created_at", "TIMESTAMPTZ", false, "Creation timestamp")
            ),
            sqlDdl = """
                CREATE TABLE restaurants (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(255) NOT NULL,
                    lat NUMERIC(10, 8) NOT NULL,
                    lng NUMERIC(11, 8) NOT NULL,
                    address_text TEXT NOT NULL,
                    phone VARCHAR(20) NOT NULL,
                    is_active BOOLEAN DEFAULT TRUE,
                    commission_pct NUMERIC(4, 2) DEFAULT 18.50,
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
                CREATE INDEX idx_restaurants_geo ON restaurants (lat, lng);
            """.trimIndent()
        ),
        TableSchema(
            tableName = "orders",
            description = "Main order state record tracking financial amounts, statuses, and delivery quotes.",
            primaryKey = "id (UUID)",
            foreignKeys = listOf("user_id -> users(id)", "restaurant_id -> restaurants(id)"),
            fields = listOf(
                FieldSchema("id", "UUID", false, "Primary Key"),
                FieldSchema("user_id", "UUID", false, "Customer User ID"),
                FieldSchema("restaurant_id", "UUID", false, "Merchant Restaurant ID"),
                FieldSchema("status", "VARCHAR(50)", false, "ORDER_CREATED | PAYMENT_CONFIRMED | PREPARING | DISPATCHED | DELIVERED | CANCELLED"),
                FieldSchema("subtotal_amount", "NUMERIC(10, 2)", false, "Food subtotal in INR"),
                FieldSchema("delivery_fee", "NUMERIC(10, 2)", false, "Delivery fee charged to user"),
                FieldSchema("platform_fee", "NUMERIC(10, 2)", false, "Platform service fee"),
                FieldSchema("total_amount", "NUMERIC(10, 2)", false, "Final total charged to customer"),
                FieldSchema("delivery_address_json", "JSONB", false, "Snapshot of delivery location & coordinates"),
                FieldSchema("idempotency_key", "VARCHAR(128)", false, "Unique key preventing duplicate orders"),
                FieldSchema("created_at", "TIMESTAMPTZ", false, "Order timestamp")
            ),
            sqlDdl = """
                CREATE TABLE orders (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL REFERENCES users(id),
                    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
                    status VARCHAR(50) NOT NULL DEFAULT 'ORDER_CREATED',
                    subtotal_amount NUMERIC(10, 2) NOT NULL,
                    delivery_fee NUMERIC(10, 2) NOT NULL,
                    platform_fee NUMERIC(10, 2) NOT NULL DEFAULT 10.00,
                    total_amount NUMERIC(10, 2) NOT NULL,
                    delivery_address_json JSONB NOT NULL,
                    idempotency_key VARCHAR(128) UNIQUE NOT NULL,
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
                CREATE INDEX idx_orders_user_id ON orders (user_id);
                CREATE INDEX idx_orders_restaurant_status ON orders (restaurant_id, status);
            """.trimIndent()
        ),
        TableSchema(
            tableName = "payments",
            description = "Tracks Razorpay payment intents, authorized transactions, signatures, and payment methods.",
            primaryKey = "id (UUID)",
            foreignKeys = listOf("order_id -> orders(id)"),
            fields = listOf(
                FieldSchema("id", "UUID", false, "Primary Key"),
                FieldSchema("order_id", "UUID", false, "Foreign Key to orders table"),
                FieldSchema("razorpay_order_id", "VARCHAR(64)", false, "Razorpay Gateway Order ID (order_xyz)"),
                FieldSchema("razorpay_payment_id", "VARCHAR(64)", true, "Razorpay Payment ID (pay_xyz)"),
                FieldSchema("razorpay_signature", "VARCHAR(256)", true, "HMAC-SHA256 signature for verification"),
                FieldSchema("payment_status", "VARCHAR(30)", false, "INITIATED | AUTHORIZED | CAPTURED | FAILED | REFUNDED"),
                FieldSchema("payment_method", "VARCHAR(30)", true, "UPI | CARD | NETBANKING | WALLET"),
                FieldSchema("amount_in_paise", "BIGINT", false, "Total amount in lowest denomination (paise)"),
                FieldSchema("error_code", "VARCHAR(50)", true, "Razorpay failure error code"),
                FieldSchema("created_at", "TIMESTAMPTZ", false, "Payment intent creation time")
            ),
            sqlDdl = """
                CREATE TABLE payments (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                    razorpay_order_id VARCHAR(64) UNIQUE NOT NULL,
                    razorpay_payment_id VARCHAR(64) UNIQUE,
                    razorpay_signature VARCHAR(256),
                    payment_status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
                    payment_method VARCHAR(30),
                    amount_in_paise BIGINT NOT NULL,
                    error_code VARCHAR(50),
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
                CREATE INDEX idx_payments_rzp_order ON payments (razorpay_order_id);
            """.trimIndent()
        ),
        TableSchema(
            tableName = "payment_refunds",
            description = "Stores Razorpay instant refund attempts, refund IDs, amounts, and settlement statuses.",
            primaryKey = "id (UUID)",
            foreignKeys = listOf("payment_id -> payments(id)", "order_id -> orders(id)"),
            fields = listOf(
                FieldSchema("id", "UUID", false, "Primary Key"),
                FieldSchema("payment_id", "UUID", false, "Original payment ID"),
                FieldSchema("order_id", "UUID", false, "Associated order ID"),
                FieldSchema("razorpay_refund_id", "VARCHAR(64)", false, "Razorpay Refund ID (rfnd_xyz)"),
                FieldSchema("amount_in_paise", "BIGINT", false, "Refund amount in paise"),
                FieldSchema("refund_status", "VARCHAR(30)", false, "PENDING | PROCESSED | FAILED"),
                FieldSchema("speed", "VARCHAR(20)", false, "INSTANT | NORMAL"),
                FieldSchema("reason", "VARCHAR(255)", false, "Merchant rejection / Customer cancellation reason"),
                FieldSchema("created_at", "TIMESTAMPTZ", false, "Refund initiation time")
            ),
            sqlDdl = """
                CREATE TABLE payment_refunds (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    payment_id UUID NOT NULL REFERENCES payments(id),
                    order_id UUID NOT NULL REFERENCES orders(id),
                    razorpay_refund_id VARCHAR(64) UNIQUE NOT NULL,
                    amount_in_paise BIGINT NOT NULL,
                    refund_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                    speed VARCHAR(20) DEFAULT 'INSTANT',
                    reason VARCHAR(255) NOT NULL,
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
            """.trimIndent()
        ),
        TableSchema(
            tableName = "delivery_assignments",
            description = "Tracks hyperlocal logistics partner dispatch (Dunzo / Porter), tracking task IDs, and rider details.",
            primaryKey = "id (UUID)",
            foreignKeys = listOf("order_id -> orders(id)"),
            fields = listOf(
                FieldSchema("id", "UUID", false, "Primary Key"),
                FieldSchema("order_id", "UUID", false, "Associated Order ID"),
                FieldSchema("provider_name", "VARCHAR(30)", false, "DUNZO | PORTER | FLASH_EXPRESS"),
                FieldSchema("external_task_id", "VARCHAR(128)", true, "Logistics provider booking task ID"),
                FieldSchema("assignment_status", "VARCHAR(40)", false, "QUOTED | REQUESTED | RIDER_ASSIGNED | ARRIVED_PICKUP | DISPATCHED | COMPLETED | UNASSIGNED_FAILED"),
                FieldSchema("delivery_cost", "NUMERIC(10, 2)", false, "Quoted cost by logistics partner"),
                FieldSchema("rider_name", "VARCHAR(100)", true, "Assigned rider name"),
                FieldSchema("rider_phone", "VARCHAR(20)", true, "Assigned rider phone number"),
                FieldSchema("fallback_attempt_count", "INT", false, "Number of aggregator failover attempts"),
                FieldSchema("updated_at", "TIMESTAMPTZ", false, "Last status update timestamp")
            ),
            sqlDdl = """
                CREATE TABLE delivery_assignments (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    order_id UUID NOT NULL REFERENCES orders(id) UNIQUE,
                    provider_name VARCHAR(30) NOT NULL,
                    external_task_id VARCHAR(128),
                    assignment_status VARCHAR(40) NOT NULL DEFAULT 'QUOTED',
                    delivery_cost NUMERIC(10, 2) NOT NULL,
                    rider_name VARCHAR(100),
                    rider_phone VARCHAR(20),
                    fallback_attempt_count INT DEFAULT 0,
                    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
            """.trimIndent()
        ),
        TableSchema(
            tableName = "delivery_tracking_logs",
            description = "High-frequency time-series log of live GPS coordinates and state transitions sent via rider webhooks.",
            primaryKey = "id (BIGSERIAL)",
            foreignKeys = listOf("delivery_assignment_id -> delivery_assignments(id)"),
            fields = listOf(
                FieldSchema("id", "BIGSERIAL", false, "Primary Key"),
                FieldSchema("delivery_assignment_id", "UUID", false, "Delivery assignment FK"),
                FieldSchema("current_status", "VARCHAR(40)", false, "Current partner status string"),
                FieldSchema("rider_lat", "NUMERIC(10, 8)", true, "Current rider latitude"),
                FieldSchema("rider_lng", "NUMERIC(11, 8)", true, "Current rider longitude"),
                FieldSchema("eta_minutes", "INT", true, "Estimated time of arrival in minutes"),
                FieldSchema("raw_webhook_payload", "JSONB", true, "Raw body received from logistics webhook"),
                FieldSchema("logged_at", "TIMESTAMPTZ", false, "Log creation timestamp")
            ),
            sqlDdl = """
                CREATE TABLE delivery_tracking_logs (
                    id BIGSERIAL PRIMARY KEY,
                    delivery_assignment_id UUID NOT NULL REFERENCES delivery_assignments(id) ON DELETE CASCADE,
                    current_status VARCHAR(40) NOT NULL,
                    rider_lat NUMERIC(10, 8),
                    rider_lng NUMERIC(11, 8),
                    eta_minutes INT,
                    raw_webhook_payload JSONB,
                    logged_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
                CREATE INDEX idx_delivery_logs_assignment ON delivery_tracking_logs (delivery_assignment_id, logged_at DESC);
            """.trimIndent()
        ),
        TableSchema(
            tableName = "webhook_events",
            description = "Audit trail for incoming Webhooks (Razorpay & Logistics) ensuring strict idempotency & replay protection.",
            primaryKey = "id (UUID)",
            foreignKeys = emptyList(),
            fields = listOf(
                FieldSchema("id", "UUID", false, "Primary Key"),
                FieldSchema("event_id", "VARCHAR(128)", false, "Unique Provider Event ID (e.g., event_xyz)"),
                FieldSchema("source_provider", "VARCHAR(30)", false, "RAZORPAY | DUNZO | PORTER"),
                FieldSchema("event_type", "VARCHAR(100)", false, "payment.captured | order.status_update"),
                FieldSchema("payload_json", "JSONB", false, "Received JSON body"),
                FieldSchema("processed", "BOOLEAN", false, "Processing state flag"),
                FieldSchema("processed_at", "TIMESTAMPTZ", true, "Timestamp when processed")
            ),
            sqlDdl = """
                CREATE TABLE webhook_events (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    event_id VARCHAR(128) UNIQUE NOT NULL,
                    source_provider VARCHAR(30) NOT NULL,
                    event_type VARCHAR(100) NOT NULL,
                    payload_json JSONB NOT NULL,
                    processed BOOLEAN DEFAULT FALSE,
                    processed_at TIMESTAMPTZ,
                    received_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
            """.trimIndent()
        )
    )
}
