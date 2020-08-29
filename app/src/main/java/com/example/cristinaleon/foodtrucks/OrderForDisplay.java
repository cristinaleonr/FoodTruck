package com.example.cristinaleon.foodtrucks;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * This class is similar to the Order object, however instead of storing IDs it stores names.
 * This object is only used to display order information.
 *
 * Created by anoshaminai on 4/3/18.
 */

public class OrderForDisplay {

    private String orderId;
    private String userName;
    private ArrayList<String> items;
    private String status;

    public OrderForDisplay() {
        items = new ArrayList<String>();
    }

    public OrderForDisplay(String orderId, String userName, String status) {
        this.orderId = orderId;
        this.userName = userName;
        items = new ArrayList<String>();
        this.status = status;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserName() {
        return userName;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public String getStatus() {
        return status;
    }

    public void addItem(String item) {
        items.add(item);
    }

    public String getItemsAsString() {
        StringBuilder itemString = new StringBuilder();
        for (String i : items) {
            itemString.append(i);
            itemString.append(", ");
        }
//        itemString.deleteCharAt(itemString.length() -1);
        return itemString.toString();
    }
}
