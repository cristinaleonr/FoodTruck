package com.example.cristinaleon.foodtrucks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by anoshaminai on 3/20/18.
 */

public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Intent i = getIntent();
        String orderInfo = i.getStringExtra("SelectedOrder");
        Log.v("Order", orderInfo);


    }
}
