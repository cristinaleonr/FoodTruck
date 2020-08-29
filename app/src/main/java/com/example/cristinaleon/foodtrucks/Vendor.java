package com.example.cristinaleon.foodtrucks;

/**
 * Vendor object to store and extract Vendor information in the DB.
 * Created by anoshaminai on 2/16/18.
 */

public class Vendor {

    String name;
    String email;
    String phone;
    String type;
    String address;
    String bankAccountNumber;
    String hours;
    String waitTime = "";
    String balance;

    public Vendor() {
        // Default constructor required for calls to DataSnapshot.getValue(Vendor.class)
    }

    public Vendor(String name, String phone, String email, String location, String cuisine, String bankAccountNumber, String hours) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.type =  cuisine;
        this.address = location;
        this.bankAccountNumber = bankAccountNumber;
        this.hours = hours;
        this.balance = "0";
    }


}
