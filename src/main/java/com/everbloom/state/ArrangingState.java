package com.everbloom.state;
import com.everbloom.model.Order;
public class ArrangingState implements OrderState {
    public String getStatus() { return "ARRANGING"; }
    public void advance(Order order) { order.setState(new ReadyState()); }
}
