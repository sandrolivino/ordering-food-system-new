package com.food.ordering.system.order.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

// We need to create a base exception class in common-domain package
// With this exception we can create specific exceptions for our business logic
// When we get an exception from this, we know that this exception comes from domain logic
public class OrderDomainException extends DomainException {
    public OrderDomainException(String message) {
        super(message);
    }

    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
