package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

/**
 * Order Details page for a customer. If a customer clicks on an order that is "waiting", they
 * can see the items ordered, price, and the estimated wait time.
 */
public class OrderDetailsCustomer extends AppCompatActivity {


    // TextViews that are updated based on Order information
    private TextView nameDisplay;
    private TextView priceView;
    TextView waitTime;

    private DatabaseReference mDatabase;

    //list of item information for display
    private ArrayList<String> itemList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    public Double totalPrice = 0.0;
    private String userID;
    private String vendID;
    private String orderID;

    Intent home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set conten tview and toolbar
        setContentView(R.layout.activity_order_details_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //attach item list string representations to the appropriate list view + initialize adapter
        ListView listView = (ListView) findViewById(R.id.itemView);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                itemList);
        listView.setAdapter(adapter);

        //Get selected order object that was clicked in the previous activity. Extract the order
        //info and save.
        Intent i = getIntent();
        Order orderInfo = (Order)i.getSerializableExtra("SelectedOrderObj");

        orderID = orderInfo.getOrderId();
        vendID = orderInfo.getVendorId();
        userID = orderInfo.getUserId();
        ArrayList<String> items = orderInfo.getItems();

        Log.v("orderid", orderID);
        Log.v("vendorId", vendID);
        Log.v("userId", userID);


        //********Details to display********

        //display name of customer, phone number
        nameDisplay = findViewById(R.id.nameDisplay);


        //populates listView with ordered items
        getOrderedItems(vendID, items);

        //set total price functionality
        this.priceView = findViewById(R.id.priceView);
        priceView.setText("$" + totalPrice);

        //populate textbox for vendor name
        getVendor(userID);

        this.waitTime = findViewById(R.id.timeView);

        getWaitTime();




    }

    /**
     * gets vendor name with ID and displays it in textbox on screen
     * @param userID
     */
    public void getVendor(final String userID) {
        DatabaseReference ref = mDatabase.child("vendorInfo");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Vendor v = child.getValue(Vendor.class);
                    if (child.getKey().equals(vendID)) {
                        nameDisplay.setText(v.name);

                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });



    }

    /**
     * gets wait time of resturant and displays it on screen
     */
    public void getWaitTime() {
        DatabaseReference ref = mDatabase.child("vendorInfo");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Vendor v = child.getValue(Vendor.class);
                    if (child.getKey().equals(vendID)) {
                        waitTime.setText(v.waitTime);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

    }


    /**
     * Query database to get item informatio of ordered items
     * @param restID vendor id for the restaurant
     * @param itemsOrdered list of item keys
     */
    private void getOrderedItems(String restID, final ArrayList<String> itemsOrdered) {

        DatabaseReference ref = mDatabase.child("vendorMenu").child(restID);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    Item item = child.getValue(Item.class);
                    Log.v("item", child.getKey().trim());

                    for (String s : itemsOrdered) {
                        //have to trim the strings for white spaces
                        if (s.trim().equals(child.getKey().trim())) {
                            addItem(item);

                            Log.v("price added:", "" + Double.parseDouble(item.price));
                            Log.v("itemAdded", s.trim());
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
     * Extract item information and store it in the list that contains item strings for display
     * @param i
     */
    private void addItem(Item i) {
        this.totalPrice += Double.parseDouble(i.price);
        priceView.setText("$"+totalPrice);
        Log.v("current price", ""+totalPrice);
        Log.v("price added:", "" + Double.parseDouble(i.price));
        itemList.add(Html.fromHtml("<b>"+i.name+"</b>") +"\n" + "Price: " + i.price);
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
        switch(item.getItemId()) {
            // if selected item is home, stay in current screen
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
