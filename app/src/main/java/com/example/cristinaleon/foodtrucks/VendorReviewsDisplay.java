package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VendorReviewsDisplay extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private ArrayList<String> reviewList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_reviews_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ListView reviewListView = findViewById(R.id.reviews);
        reviewList = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                reviewList);
        reviewListView.setAdapter(adapter);

        getReviews();

    }

    private void getReviews() {
        Intent i = getIntent();
        String vendorKey = i.getStringExtra("vendID");

        DatabaseReference ref = mDatabase.child("reviewVendors").child(vendorKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Review review = dataSnapshot.getValue(Review.class);

                if (review != null) {
                    if (review.comments != null) {
                        for (String value : review.comments.values()) {
                            addComment(value);
                            Log.v("c",value);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void addComment(String c) {
        reviewList.add(c);
        adapter.notifyDataSetChanged();
    }


    /**
     * Handle user returning to activity
     */
    @Override
    public void onResume() {
        // get current user
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // if user is not null, proceed as normal
        if (user != null) {
            Log.v("User",user.toString());
        }
        // else, go back to main screen to make user log in
        else {
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);
            Log.v("User","null");
        }

    }

    /**
     * Sets up the top navigation bar with the provided menu
     *
     * @param menu Menu to add to nav bar
     * @return true when options menu has been created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home,menu);
        return true;
    }

    /**
     * Handles the different cases for the options on the navigation bar
     *
     * @param item Clicked item
     * @return true if an option case has been handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        switch(item.getItemId()) {
            // if selected item is home, go to vendor home screen
            case R.id.home:
                Intent homeActivity = new Intent(getApplicationContext(), CustomerHomeActivity.class);
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                homeActivity.putExtra("user", uid);
                startActivity(homeActivity );
                return true;

            // if selected item is edit, go to edit info screen
            case R.id.edit:
                Intent editVendor = new Intent(getApplicationContext(), EditCustomer.class);
                startActivity(editVendor);
                return true;

            // if selected item is cart, go to cart screen
            case R.id.cart:
                Intent cartPage = new Intent(getApplicationContext(), CartPage.class);
                startActivity(cartPage);
                return true;

            // if selected item is logout, log user out and go back to log in screen
            case R.id.logout:
                mAuth.signOut();
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
