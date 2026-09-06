package com.everbloom.state;
import com.everbloom.model.Order;
public class ReadyState implements OrderState {
    public String getStatus() { return "READY"; }
    public void advance(Order order) { order.setState(new DeliveredState()); }
}
