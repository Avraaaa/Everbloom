package com.everbloom.model;

public class Customer {

    private long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;

    public Customer(String fullName, String phone, String email, String address) {
        this(0, fullName, phone, email, address);
    }

    public Customer(long id, String fullName, String phone, String email, String address) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
