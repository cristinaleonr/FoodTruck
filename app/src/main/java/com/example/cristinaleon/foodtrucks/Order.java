package com.example.cristinaleon.foodtrucks;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Order object to store and extract Order information in the DB.
 * Created by michaelplumb on 3/20/18.
 */

@SuppressWarnings("serial")
public class Order implements Serializable{
    private String vendorId;
    private String userId;
    private String orderId;
    //status can be "complete" or "waiting". If cancelled it will be removed from the DB
    private String status;

    private ArrayList<String> item_IDs = new ArrayList<String>();

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    }

    public Order(String orderId, String vendorId, String userId, ArrayList<String> items) {
        this.vendorId = vendorId;
        this.userId = userId;
        this.item_IDs = items;
        this.orderId = orderId;
    }

    /*getters and setters*/

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public ArrayList<String> getItems() {
        return this.item_IDs;
    }

    public void addItem(String id) {
        item_IDs.add(id);
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
