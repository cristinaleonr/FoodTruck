package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReviewVendors extends AppCompatActivity {
    double rating;
    String numberOfRates;
    String vendorID;
    String currentRating;
    boolean exists;
    int commentNumber;

    /**
     * Create activity, set up layout, toolbar, and initialize instance variables
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_vendors);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        ImageView imv = findViewById(R.id.imageView13);
        exists = false;
        commentNumber = 1;
        rating = 0.0;
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8};
                highlight(stars);
                int[] unstars = {9, 10, 11, 12};
                unhighlight(unstars);
                rating = 1;
            }
        });
        imv = findViewById(R.id.imageView14);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9};
                highlight(stars);
                int[] unstars = {10, 11, 12};
                unhighlight(unstars);
                rating = 2;
            }
        });
        imv = findViewById(R.id.imageView15);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9, 10};
                highlight(stars);
                int[] unstars = {11, 12};
                unhighlight(unstars);
                rating = 3;
            }
        });
        imv = findViewById(R.id.imageView16);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9, 10, 11};
                highlight(stars);
                int[] unstars = {12};
                unhighlight(unstars);
                rating = 4;
            }
        });
        imv = findViewById(R.id.imageView17);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9, 10, 11, 12};
                highlight(stars);
                rating = 5;
            }
        });
        Intent i = getIntent();
        vendorID = i.getStringExtra("vendorID");
        Button submit = findViewById(R.id.button4);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submit(v);
            }
        });
    }


    public void highlight(int[] stars) {
        for (int i : stars) {
            switch (i) {
                case 8:
                    ImageView imv = findViewById(R.id.imageView13);
                    imv.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 9:
                    ImageView imv1 = findViewById(R.id.imageView14);
                    imv1.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 10:
                    ImageView imv2 = findViewById(R.id.imageView15);
                    imv2.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 11:
                    ImageView imv3 = findViewById(R.id.imageView16);
                    imv3.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 12:
                    ImageView imv4 = findViewById(R.id.imageView17);
                    imv4.setImageResource(android.R.drawable.star_big_on);
                    break;

            }
        }
    }

    public void unhighlight(int[] stars) {
        for (int i : stars) {
            switch (i) {
                case 8:
                    ImageView imv = findViewById(R.id.imageView13);
                    imv.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 9:
                    ImageView imv1 = findViewById(R.id.imageView14);
                    imv1.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 10:
                    ImageView imv2 = findViewById(R.id.imageView15);
                    imv2.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 11:
                    ImageView imv3 = findViewById(R.id.imageView16);
                    imv3.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 12:
                    ImageView imv4 = findViewById(R.id.imageView17);
                    imv4.setImageResource(android.R.drawable.star_big_off);
                    break;

            }
        }
    }

    public void submit(View view) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child("reviewVendors");
        if (rating == 0.0) {
            Toast toast =
                    Toast.makeText(getBaseContext(),
                            Html.fromHtml("Please click a star to rate the Vendor"),
                            Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }
        //Access the firebase
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(vendorID)) {
                        exists = true;
                        Iterable<DataSnapshot> information = child.getChildren();
                        for (DataSnapshot info : information) {
                            if (info.getKey().equals("rating")) {
                                currentRating = info.getValue().toString();
                            }
                            if (info.getKey().equals("numberOfRates")) {
                                numberOfRates = info.getValue().toString();
                            }
                            if (info.getKey().equals("comments")) {
                                Iterable<DataSnapshot> allComments = info.getChildren();
                                for (DataSnapshot comment : allComments) {
                                    commentNumber++;
                                }
                            }
                        }
                        Double dCurrentRating = Double.parseDouble(currentRating);
                        int iNumberOfRates = Integer.parseInt(numberOfRates);
                        double newRating  = (dCurrentRating * iNumberOfRates);
                        iNumberOfRates += 1;
                        newRating = (newRating + rating)/iNumberOfRates;
                        String snewRating = String.valueOf(newRating);
                        EditText editText = findViewById(R.id.editText11);
                        String comment = editText.getText().toString();
                        numberOfRates = String.valueOf(iNumberOfRates);
                        mDatabase.child("reviewVendors").child(vendorID).child("rating").setValue(snewRating);
                        mDatabase.child("reviewVendors").child(vendorID).child("numberOfRates").setValue(numberOfRates);
                        if (!comment.isEmpty()) {
                            mDatabase.child("reviewVendors").child(vendorID).child("comments").child("c_" + commentNumber)
                                    .setValue(comment);
                        }
                    }
                }
                if (!exists) {
                    mDatabase.child("reviewVendors").child(vendorID).child("rating").setValue(String.valueOf(rating));
                    mDatabase.child("reviewVendors").child(vendorID).child("numberOfRates").setValue("1");
                    EditText editText = findViewById(R.id.editText11);
                    String comment = editText.getText().toString();
                    if (!comment.isEmpty()) {
                        mDatabase.child("reviewVendors").child(vendorID).child("comments").child("c_" + 1)
                                .setValue(comment);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        finish();
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
        switch(item.getItemId()) {
            // if selected item is home, go to home screen
            case R.id.home:
                Intent homeActivity = new Intent(getApplicationContext(), CustomerHomeActivity.class);
                startActivity(homeActivity );
                return true;

            // if selected item is edit, go to edit info screen
            case R.id.edit:
                Intent editCustomer = new Intent(getApplicationContext(), EditCustomer.class);
                startActivity(editCustomer);
                return true;

            // if selected item is cart, go to cart screen
            case R.id.cart:
                Intent cartPage = new Intent(getApplicationContext(), CartPage.class);
                startActivity(cartPage);
                return true;

            // if selected item is logout, log user out and go back to log in screen
            case R.id.logout:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
