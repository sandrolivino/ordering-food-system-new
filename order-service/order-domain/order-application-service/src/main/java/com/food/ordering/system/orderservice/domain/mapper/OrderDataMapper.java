package com.food.ordering.system.orderservice.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.StreetAdress;
import com.food.ordering.system.orderservice.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.orderservice.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.orderservice.domain.dto.create.OrderAdress;
import com.food.ordering.system.orderservice.domain.dto.track.TrackOrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderDataMapper {
    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
        return new Restaurant.Builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(createOrderCommand.getItems().stream().map(orderItem ->
                                new Product(new ProductId(orderItem.getProductId())))
                        .collect(Collectors.toList()))
                .build();
    }

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return new Order.Builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .items(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
                .build();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
                .orderTrackingId(order.getTrackingId())
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages())
                .build();

    }

    private List<OrderItem> orderItemsToOrderItemEntities(
            List<com.food.ordering.system.orderservice.domain.dto.create.OrderItem> orderItems) {

        return orderItems.stream()
                .map(orderItem ->
                        new OrderItem.Builder()
                                .product(new Product(new ProductId(orderItem.getProductId())))
                                .price(new Money(orderItem.getPrice()))
                                .quantity(orderItem.getQuantity())
                                .subtotal(new Money(orderItem.getSubTotal()))
                                .build()).collect(Collectors.toList());
    }

    private StreetAdress orderAddressToStreetAddress(OrderAdress orderAdress) {
        return new StreetAdress(
                UUID.randomUUID(),
                orderAdress.getStreet(),
                orderAdress.getPostalCode(),
                orderAdress.getCity()
        );
    }
}
