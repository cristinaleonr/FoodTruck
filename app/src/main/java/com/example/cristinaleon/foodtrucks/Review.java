package com.example.cristinaleon.foodtrucks;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vikramkhemlani on 4/11/18.
 */

public class Review {
    String rating;
    String numberOfRates;
    HashMap<String,String> comments;

    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    public Review(String rating, String numberOfRates,HashMap<String,String> comments) {
        this.rating = rating;
        this.numberOfRates = numberOfRates;
        this.comments = comments;
    }
}
