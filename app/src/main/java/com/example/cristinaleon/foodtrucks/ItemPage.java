package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;

/**
 * Created by vikramkhemlani on 3/20/18.
 * ItemPage is a class used to get and display the details of an item when
 * it is clicked on from the menu of the restaurant
 */


public class ItemPage extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String itemID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        String item = i.getStringExtra("SelectedItem");
        TextView title = findViewById(R.id.textView);
        String[] name = item.split("\n");
        title.setText(name[0]);
        TextView price = findViewById(R.id.textView2);
        price.setText(name[1]);
        TextView description= findViewById(R.id.textView3);
        description.setText(name[2]);
        itemID = i.getStringExtra("SelectedItemKey");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    /*addToCart is the method linked to the Add to Cart button
    that adds the current item listed on the page to the cart
    of the user currently logged in. Only the id of the item is
    added to the cart and not the specific details
    */

    public void addToCart(View view) {
            Intent i = getIntent();
            final String vID = i.getStringExtra("VendorID");
            final LinkedList<String> cart = new LinkedList<>();
            DatabaseReference ref = mDatabase.child("cartInfo");
            FirebaseUser user = mAuth.getCurrentUser();
            final String uid = user.getUid();
            //Access the firebase
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    boolean exists = false;
                    for (DataSnapshot child : children) {
                        if (child.getKey().equals(uid)) {
                            exists = true;
                            Iterable<DataSnapshot> restID = child.getChildren();
                            for (DataSnapshot vendor : restID) {
                                // Make sure only one restaurant can be ordered from at a time

                                if (vendor.getKey().equals(vID)) {
                                    vendor.child(itemID);
                                    Iterable<DataSnapshot> items = vendor.getChildren();
                                    int i = 0;
                                    for (DataSnapshot item : items) {
                                        i++;
                                    }
                                    mDatabase.child("cartInfo").child(uid).child(vID).
                                            child("item" + i).setValue(itemID);
                                    finish();
                                }
                                else {
                                    //If a pending order from another vendor is in the cart
                                    Toast toast =
                                            Toast.makeText(getBaseContext(),
                                                    Html.fromHtml("Can only order from one Vendor at a time"),
                                                    Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                                    toast.show();
                                    return;
                                }
                            }

                        }
                    }
                    /* if the user does not have a cart currently, add a new cart
                     */
                    if (!exists) {
                        mDatabase.child("cartInfo").child(uid).child(vID).
                                child("item0").setValue(itemID);
                        finish();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
            // if selected item is home, go to home screen
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
