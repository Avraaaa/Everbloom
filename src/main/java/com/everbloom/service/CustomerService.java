package com.everbloom.service;

import com.everbloom.model.Customer;
import com.everbloom.repository.CustomerRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(Customer customer) throws SQLException {
        normalize(customer);
        validate(customer);
        ensurePhoneIsAvailable(customer);
        return customerRepository.create(customer);
    }

    public Optional<Customer> findById(long id) throws SQLException {
        validateId(id);
        return customerRepository.findById(id);
    }

    public List<Customer> findAll() throws SQLException {
        return customerRepository.findAll();
    }

    public List<Customer> search(String searchText) throws SQLException {
        String normalizedSearch = searchText == null ? "" : searchText.trim();
        if (normalizedSearch.isEmpty()) {
            return findAll();
        }
        return customerRepository.search(normalizedSearch);
    }

    public boolean update(Customer customer) throws SQLException {
        normalize(customer);
        validate(customer);
        validateId(customer.getId());
        ensurePhoneIsAvailable(customer);
        return customerRepository.update(customer);
    }

    public boolean delete(long id) throws SQLException {
        validateId(id);
        return customerRepository.delete(id);
    }

    private void ensurePhoneIsAvailable(Customer customer) throws SQLException {
        Optional<Customer> existingCustomer = customerRepository.findByPhone(customer.getPhone());
        if (existingCustomer.isPresent() && existingCustomer.get().getId() != customer.getId()) {
            throw new IllegalArgumentException("A customer with this phone number already exists.");
        }
    }

    private void validate(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (!customer.getPhone().matches("[+0-9]{6,20}")) {
            throw new IllegalArgumentException("Enter a valid phone number.");
        }
        if (customer.getEmail() != null && !customer.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
    }

    private void normalize(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        customer.setFullName(normalizeRequiredText(customer.getFullName()));
        customer.setPhone(normalizePhone(customer.getPhone()));
        customer.setEmail(normalizeOptionalText(customer.getEmail()));
        customer.setAddress(normalizeOptionalText(customer.getAddress()));
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replace(" ", "").replace("-", "");
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
