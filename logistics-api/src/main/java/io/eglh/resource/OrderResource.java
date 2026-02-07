package io.eglh.resource;

import io.eglh.model.Order;
import io.eglh.model.OrderRequest;
import io.eglh.model.OrderStatus;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.Optional;

/**
 * REST endpoint for managing transport orders.
 * Provides APIs for creating orders and checking their status.
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Channel("orders-out")
    Emitter<Long> orderEmitter;

    /**
     * Creates a new transport order, persists it to the database,
     * and sends the ID to the messaging queue for processing.
     *
     * @param request the order details
     * @return the created order with status 202 Accepted
     */
    @POST
    @Transactional
    public Response createOrder(OrderRequest request) {
        Order order = new Order();
        order.origin = request.origin;
        order.destination = request.destination;
        order.status = OrderStatus.PENDING;
        order.persist();

        orderEmitter.send(order.id);

        return Response.status(Response.Status.ACCEPTED).entity(order).build();
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the unique identifier of the order
     * @return the order if found, or 404 Not Found
     */
    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") Long id) {
        Optional<Order> order = Order.findByIdOptional(id);
        return order.map(o -> Response.ok(o).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
