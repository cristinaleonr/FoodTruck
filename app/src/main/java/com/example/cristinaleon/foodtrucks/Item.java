package com.example.cristinaleon.foodtrucks;

/**
 * Created by anoshaminai on 3/20/18.
 */

public class Item {

    public String name;
    public String price;
    public String description;

    public Item() {
        // Default constructor required for calls to DataSnapshot.getValue(Item.class)
    }

    public Item(String name, String price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }
}
