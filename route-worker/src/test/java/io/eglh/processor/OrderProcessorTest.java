package io.eglh.processor;

import io.eglh.model.Order;
import io.eglh.model.OrderStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class OrderProcessorTest {

    @Inject
    OrderProcessor orderProcessor;

    @Test
    @Transactional
    public void testProcessOrderUpdatesStatusToCompleted() throws Exception {
        // Given: Create a new order and persist it. Hibernate will generate the ID.
        Order order = new Order();
        order.origin = "Paris";
        order.destination = "Berlin";
        order.status = OrderStatus.PENDING;
        order.persist(); // Persist a new order. 'order' object is now managed by Hibernate.

        // Retrieve the persisted order to ensure it was saved correctly and get its assigned ID.
        // This also ensures we are working with a managed entity from the test's context.
        Order persistedOrder = Order.findById(order.id);
        assertNotNull(persistedOrder, "Order should be persisted and found in the database.");
        assertEquals(OrderStatus.PENDING, persistedOrder.status, "Order status should initially be PENDING.");

        // When: Call the processor method with the ID of the persisted order.
        orderProcessor.process(persistedOrder.id);

        // Then: Fetch the order again after processing and verify its status.
        Order updatedOrder = Order.findById(persistedOrder.id);
        assertNotNull(updatedOrder, "Order should still be found after processing.");
        assertEquals(OrderStatus.COMPLETED, updatedOrder.status, "Order status should be updated to COMPLETED.");
    }

    @Test
    @Transactional // Added @Transactional for the test method that needs it for persistence
    public void testProcessNonExistentOrder() throws Exception {
        // Given
        Long nonExistentOrderId = 999L;

        // When: Attempt to process an order ID that does not exist.
        // The orderProcessor.process method should log an error but not crash.
        orderProcessor.process(nonExistentOrderId);

        // Then: Assert that the method completes without throwing an unhandled exception.
        // We are primarily testing that it handles the "not found" case gracefully.
        assertTrue(true, "Processing a non-existent order should not throw an unhandled exception.");
    }
}