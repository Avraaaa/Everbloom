package com.everbloom.observer;

import com.everbloom.model.Order;

import java.sql.SQLException;

public interface OrderObserver {

    void onOrderStatusChanged(Order order) throws SQLException;
}
