package com.food.ordering.system.orderservice.domain.dto.create;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrderResponse {
    @NotNull
    private final UUID orderTrackingId;

    @NotNull
    private final OrderStatus orderStatus;

    @NotNull
    private final String message;
}
