package com.everbloom.state;

import com.everbloom.model.Order;

public interface OrderState {
    String getStatus();
    void advance(Order order);
}
