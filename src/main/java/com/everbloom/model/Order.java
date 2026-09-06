package com.everbloom.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private long id;
    private String orderNumber;
    private Customer customer;
    private Bouquet bouquet;
    private List<Extra> extras;
    private String fulfillmentType;
    private String deliveryAddress;
    private String pricingPolicy;
    private long subtotal;
    private long discount;
    private long total;
    private String status;
    private LocalDateTime placedAt;

    public Order(String orderNumber, Customer customer, Bouquet bouquet, List<Extra> extras,
                 String fulfillmentType, String deliveryAddress, String pricingPolicy,
                 long subtotal, long discount, long total) {
        this(0, orderNumber, customer, bouquet, extras, fulfillmentType, deliveryAddress,
                pricingPolicy, subtotal, discount, total, "ORDERED", null);
    }

    public Order(long id, String orderNumber, Customer customer, Bouquet bouquet, List<Extra> extras,
                 String fulfillmentType, String deliveryAddress, String pricingPolicy,
                 long subtotal, long discount, long total, String status, LocalDateTime placedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.bouquet = bouquet;
        this.extras = extras == null ? List.of() : List.copyOf(extras);
        this.fulfillmentType = fulfillmentType;
        this.deliveryAddress = deliveryAddress;
        this.pricingPolicy = pricingPolicy;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.status = status;
        this.placedAt = placedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Bouquet getBouquet() {
        return bouquet;
    }

    public List<Extra> getExtras() {
        return extras;
    }

    public String getFulfillmentType() {
        return fulfillmentType;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getPricingPolicy() {
        return pricingPolicy;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public long getDiscount() {
        return discount;
    }

    public long getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }
}
