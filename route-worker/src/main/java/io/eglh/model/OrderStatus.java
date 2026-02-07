package io.eglh.model;

/**
 * Represents the current processing state of a transport order.
 */
public enum OrderStatus {
    /**
     * The order has been received and is waiting for processing.
     */
    PENDING,
    /**
     * The order has been successfully processed by the route worker.
     */
    COMPLETED
}
