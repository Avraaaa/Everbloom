package com.everbloom.state;
import com.everbloom.model.Order;
public class PreparingState implements OrderState {
    public String getStatus() { return "PREPARING"; }
    public void advance(Order order) { order.setState(new ArrangingState()); }
}
