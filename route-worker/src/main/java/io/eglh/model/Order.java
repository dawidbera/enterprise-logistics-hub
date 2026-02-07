package io.eglh.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Entity representing a transport order in the logistics system.
 * Shared with the API for consistent data mapping.
 */
@Entity
@Table(name = "transport_orders")
public class Order extends PanacheEntity {
    /**
     * The starting city.
     */
    public String origin;

    /**
     * The destination city.
     */
    public String destination;

    /**
     * Current status of the order.
     */
    @Enumerated(EnumType.STRING)
    public OrderStatus status;
}
