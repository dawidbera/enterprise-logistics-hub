package io.eglh.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Entity representing a transport order in the logistics system.
 * Uses Panache for simplified JPA operations.
 */
@Entity
@Table(name = "transport_orders")
public class Order extends PanacheEntity {
    /**
     * The starting city of the transport.
     */
    public String origin;

    /**
     * The destination city of the transport.
     */
    public String destination;

    /**
     * Current status of the order.
     */
    @Enumerated(EnumType.STRING)
    public OrderStatus status;
}
