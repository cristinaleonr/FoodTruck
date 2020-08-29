package com.example.cristinaleon.foodtrucks;

/**
 * Customer object to store and extract Customer information in the DB.
 * Created by anoshaminai on 2/16/18.
 */

public class Customer {

    public String firstName;
    public String lastName;
    public String phoneNumber;
    public String balance;

    public Customer() {
        // Default constructor required for calls to DataSnapshot.getValue(Customer.class)
    }

    public Customer(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.balance = "100";
    }
}
