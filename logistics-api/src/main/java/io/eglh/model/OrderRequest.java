package io.eglh.model;

/**
 * Data Transfer Object for creating a new transport order.
 */
public class OrderRequest {
    /**
     * The starting city.
     */
    public String origin;

    /**
     * The destination city.
     */
    public String destination;
}
