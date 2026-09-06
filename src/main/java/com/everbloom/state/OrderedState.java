package com.everbloom.state;
import com.everbloom.model.Order;
public class OrderedState implements OrderState {
    public String getStatus() { return "ORDERED"; }
    public void advance(Order order) { order.setState(new PreparingState()); }
}
