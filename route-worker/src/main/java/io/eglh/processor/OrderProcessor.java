package io.eglh.processor;

import io.eglh.model.Order;
import io.eglh.model.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Component responsible for asynchronous processing of transport orders.
 * Listens to the messaging queue, performs heavy calculations, and updates order status.
 */
@ApplicationScoped
public class OrderProcessor {

    private static final Logger LOG = Logger.getLogger(OrderProcessor.class);

    /**
     * Processes an incoming order message.
     * Updates the status to COMPLETED after simulation of heavy work.
     *
     * @param orderId the ID of the order to process
     * @throws InterruptedException if the processing is interrupted
     */
    @Incoming("orders-in")
    @Transactional
    public void process(Long orderId) throws InterruptedException {
        LOG.infof("Processing order: %d", orderId);

        Order order = Order.findById(orderId);
        if (order == null) {
            LOG.errorf("Order %d not found", orderId);
            return;
        }

        // Simulate heavy work
        simulateHeavyWork();

        order.status = OrderStatus.COMPLETED;
        LOG.infof("Order %d completed", orderId);
    }

    /**
     * Simulates CPU-intensive route optimization work.
     *
     * @throws InterruptedException if sleep is interrupted
     */
    private void simulateHeavyWork() throws InterruptedException {
        // Sleep for 5 seconds as per spec
        Thread.sleep(5000);
        
        // CPU-intensive loop simulation
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 1000) {
            Math.sqrt(Math.random());
        }
    }
}
