package com.example.cristinaleon.foodtrucks;

import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

/**
 * Page with vendor information, menu, etc that a Customer views when they click on a Vendor in
 * the home page. Allows customer to select items from the menu to add to their cart.
 */
public class VendorPage extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<String> menuList;
    private ArrayList<String> menuKeys;

    private ArrayAdapter<String> adapter;
    private DatabaseReference mDatabase;
    private String vendorKey;

    ArrayList<String> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set content and toolbar
        setContentView(R.layout.activity_restaurant_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //initialize lists to store menu information
        menuList = new ArrayList<String>();
        menuKeys = new ArrayList<String>();

        ListView menuListView = findViewById(R.id.menu);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                menuList);
        menuListView.setAdapter(adapter);
        menuListView.setOnItemClickListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent i = getIntent();
        vendorKey = i.getStringExtra("SelectedVendorKey");

        populateVendorInfo();
        getMenuItems();


        displayRating();




    }

    /**
     *  go to ratings activity
     * */
    public void showReviews(View view) {
        Intent showReviews = new Intent(getApplicationContext(), VendorReviewsDisplay.class);
        showReviews.putExtra("vendID",vendorKey);

        startActivity(showReviews);
    }

    /**
     *  get rating bar view and modify accordingly
     * */
    private void displayRating() {
        final RatingBar ratingBar = (RatingBar) findViewById(R.id.rating);
        ratingBar.setFocusable(false);
        DatabaseReference ref = mDatabase.child("reviewVendors").child(vendorKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Review review = dataSnapshot.getValue(Review.class);
                if (review != null) {
                    if (review.rating != null) {
                        ratingBar.setRating(Float.parseFloat(review.rating));
                    }
                    if (review.numberOfRates != null) {
                        TextView numRate = (TextView) findViewById(R.id.numRatings);
                        if (Float.parseFloat(review.numberOfRates) == 1) {
                            numRate.setText("1 Rating");
                        }
                        else {
                            numRate.setText(review.numberOfRates + " Ratings");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Take vendor info from extra and parse it into appropriate text views for display
     */
    private void populateVendorInfo() {

        Intent i = getIntent();
        String[] vendorInfo = i.getStringExtra("SelectedVendor").split("\n");
        String[] typePhoneInfo = vendorInfo[2].split("\t");
        String[] addressInfo = vendorInfo[1].split(",");

        TextView nameText = findViewById(R.id.name);
        TextView addressText = findViewById(R.id.address);
        TextView hoursText = findViewById(R.id.hours);
        TextView typeText = findViewById(R.id.type);
        TextView phoneText = findViewById(R.id.phone);

        nameText.setText(vendorInfo[0]);
        addressText.setText(vendorInfo[1]);
        hoursText.setText(vendorInfo[3]);
        typeText.setText(typePhoneInfo[0]);
        phoneText.setText(typePhoneInfo[1]);

        Log.v("Name", vendorInfo[0]);
        Log.v("Address", vendorInfo[1]);
        Log.v("Hours", vendorInfo[3]);
        Log.v("Type", typePhoneInfo[0]);
        Log.v("Phone", typePhoneInfo[1]);
    }

    /**
     * Query database for menu items for this vendor
     */
    private void getMenuItems() {

        DatabaseReference ref = mDatabase.child("vendorMenu").child(vendorKey);


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Item item = child.getValue(Item.class);
                    addItem(item);
                    menuKeys.add(child.getKey());
                    Log.v("Item",child.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Create the string display of an item and add to the list of string representations of items
     * @param i
     */
    private void addItem(Item i) {
        menuList.add(Html.fromHtml("<b>"+i.name+"</b>") +"\n" + "Price: " + i.price + "\n" +
                "Description: " + i.description);
        adapter.notifyDataSetChanged();
    }

    /**
     * When an item is clicked, take the user to the item page and display the full information
     * and allow them to add it to their cart
     * @param adapterView
     * @param v
     * @param position
     * @param id
     */
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {

        Intent itemPage = new Intent(getApplicationContext(), ItemPage.class);
        Log.v("Clicked ID",id+"");
        Log.v("Clicked",menuList.get((int)id).toString());
        Log.v("Clicked Key",menuKeys.get((int)id).toString());
        itemPage.putExtra("SelectedItem",menuList.get((int)id).toString());
        itemPage.putExtra("SelectedItemKey",menuKeys.get((int)id).toString());
        itemPage.putExtra("VendorID", vendorKey);
        startActivity(itemPage);


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
