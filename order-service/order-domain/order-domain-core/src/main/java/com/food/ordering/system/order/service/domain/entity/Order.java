
package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAdress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

// Root Aggregate of Order Service Domain Logic. It inherits OrderId UUID.
public class Order extends AggregateRoot<OrderId> {
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAdress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    // These next 3 fields are not final because they can be modified during the creation of Order entity
    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    // This constructor is now private
    // Because of this we need the public method builder() to instantiate a new Order
    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }

    // Initialize a new Order
    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;

        // Should be created in the Order class
        initializerOrderItems();
    }

    // Initialize the Order Items
    private void initializerOrderItems() {
        long itemId = 1;
        for (OrderItem orderItem : items) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }

    // Validate the initial order, it's total price and items prices.
    // Here we start to work with personalized exceptions.
    public void validateOrder() {
        valiateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    private void valiateInitialOrder() {
        // Status and id should be null at this point
        if (orderStatus != null || getId() != null) {
            // This exception should be created in the order.service.domain.exception package
            throw new OrderDomainException("Order is not in correct state for initialization");
        }
    }

    // Check if price is not null and greater than zero
    private void validateTotalPrice() {
        if (price == null || !price.isGreaterThanZero()) {
            throw new OrderDomainException("Total price must be greater than zero!");
        }
    }

    // Validate the item price looking at the Product price.
    private void validateItemPrice(OrderItem orderItem) {
        if(!orderItem.isPriceValid()) {
            throw new OrderDomainException("Order item price: " + orderItem.getPrice().getAmount()
                    + " is not valid for product " + orderItem.getProduct().getName() + "!");
            // TODO there's no getValue method here: orderItem.getProduct().getId().getValue(). CHECK
        }
    }

    // Validate if the total price and items prices sum (subtotal) are the same.
    private void validateItemsPrice() {
        Money orderItemsTotal = items.stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubtotal();
        }).reduce(Money.ZERO, Money::add);

        if (!price.equals(orderItemsTotal)) {
            throw new OrderDomainException("Total price: " + price.getAmount() +
                    " is not equal to Order items total: " + orderItemsTotal.getAmount() + "!");
        }
    }

    // Change order status to PAID
    public void pay() {
        if(orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException("Order is not in correct state for pay operation!");
        }
        orderStatus = OrderStatus.PAID;
    }

    // Change order status to APPROVED
    public void approved(){
        if(orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for approve operation!");
        }
        orderStatus = OrderStatus.APPROVED;
    }

    // Change order status to CANCELLING. This is an example of Compensating Transaction
    // We should include a failureMessage list obteined from the other services
    public void initCancel(List<String> failureMessages){
        if(orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for initCancel operation!");
        }
        orderStatus = OrderStatus.CANCELLING;

        updateFailureMessages(failureMessages);
    }

    // Change order status to CANCEL. This is an example of Compensating Transaction
    // We should include a failureMessage list obteined from the other services
    public void cancel(List<String> failureMessages){
        if(orderStatus != OrderStatus.CANCELLING || orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException("Order is not in correct state for cancel operation!");
        }
        orderStatus = OrderStatus.CANCELLED;

        updateFailureMessages(failureMessages);
    }

    // Update failure messages
    private void updateFailureMessages(List<String> failureMessages) {
        if(this.failureMessages != null && failureMessages != null){
            this.failureMessages.addAll(failureMessages.stream().filter(message -> !message.isEmpty()).toList());
        }
        if (this.failureMessages == null){
            this.failureMessages = failureMessages;
        }
    }


    // Getters methods
    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAdress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    // Builder generated by plugin innerBuilder
    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAdress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        // Renamed from newBuilder to "builder" just to make it simpler
        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAdress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
