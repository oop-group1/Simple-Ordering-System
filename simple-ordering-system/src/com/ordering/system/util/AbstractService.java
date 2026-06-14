package com.ordering.system.util;

import java.util.List;

/**
 * Abstraction: This class defines the contract for all services.
 * It ensures every service has a validation and calculation method.
 */
public abstract class AbstractService {
    public abstract boolean validateInput(Object data);
    public abstract double calculateTotal(List<?> items);
}