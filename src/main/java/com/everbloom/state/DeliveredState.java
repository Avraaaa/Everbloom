package com.everbloom.state;
import com.everbloom.model.Order;
public class DeliveredState implements OrderState {
    public String getStatus() { return "DELIVERED"; }
    public void advance(Order order) { throw new IllegalStateException("Delivered orders cannot move to another status."); }
}
