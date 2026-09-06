package com.everbloom.observer;

import com.everbloom.model.Order;

public class OrderViewRefreshObserver implements OrderObserver {

    private final Runnable refreshAction;

    public OrderViewRefreshObserver(Runnable refreshAction) {
        this.refreshAction = refreshAction;
    }

    @Override
    public void onOrderStatusChanged(Order order) {
        refreshAction.run();
    }
}
