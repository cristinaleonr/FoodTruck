package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Activity for a customer to view their orders. Orders are stored in two lists, one for "waiting"
 * and one for complete. Customers can click on their orders to view order details, such as the
 * item names and prices.
 * Created by anoshaminai on 4/3/18.
 */

public class CustomerCurrentOrdersActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private String userID;

    private DatabaseReference mDatabase;

    private ArrayList<Order> orders;
    private ArrayList<Order> completedOrders;
    private ArrayList<Order> waitingOrders;

    // lists of string formats of orders for display
    private ArrayList<String> orderStringList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> completedOrderStringList;
    private ArrayAdapter<String> completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout and toolbar
        setContentView(R.layout.activity_current_orders);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // get user ID from the customer home page
        userID = getIntent().getStringExtra("userID");
        Log.v("userID", userID);

        // initialize arrays
        orders = new ArrayList<Order>();
        orderStringList = new ArrayList<String>();
        completedOrderStringList = new ArrayList<String>();

        // attach order list string representations to the appropriate list view + initialize adapter
        ListView listView = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                orderStringList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        ListView completedListView = (ListView) findViewById(R.id.completedList);
        completedAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                completedOrderStringList);
        completedListView.setAdapter(completedAdapter);
        completedListView.setOnItemClickListener(this);

        //instantiate database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        getOrders();

    }

    /**
     * Method to query the database for orders and to find this customer's orders for display.
     * Stores the customer's orders in two lists, one for waiting orders and one for complete.
     */
    private void getOrders() {
        DatabaseReference ref = mDatabase.child("orders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    //if the order's user ID == this users ID then add the order to the orders list
                    String orderUser = child.child("userID").getValue().toString();
                    if (orderUser.equals(userID)) {

                        // Extract order information
                        String orderStatus = child.child("status").getValue().toString();
                        String vendID = child.child("restID").getValue().toString();

                        //Create and store order info in an Order object
                        Order o = new Order();
                        o.setOrderId(child.getKey());
                        o.setStatus(orderStatus);
                        o.setUserId(userID);
                        o.setVendorId(vendID);

                        Iterable<DataSnapshot> orderItems = child.child("items").getChildren();
                        for (DataSnapshot i : orderItems) {
                            o.addItem(i.getValue().toString());
                        }

                        //Store the Order object if the order is waiting - those are the only order types
                        //that can be clicked
                        //if (o.getStatus().equals("waiting")) {
                            orders.add(o);
                        //}

                        //Create and store order display info in an OrderForDisplay object
                        OrderForDisplay d = new OrderForDisplay();
                        d.setOrderId(child.getKey());
                        d.setStatus(orderStatus);

                        // Get the rest of the order information for display by querying the database
                        getOrderInfo(vendID, d);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    /**
     * Query the  database to get the vendor name and store it in the order display object.
     * Once the query is complete, turn the display object into a string for display.
     * @param vendID the ID of the vendor that the order is for
     * @param d OrderForDisplay object that stores the order information
     */
    private void getOrderInfo(final String vendID, final OrderForDisplay d) {
        DatabaseReference ref = mDatabase.child("vendorInfo");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (final DataSnapshot child : children) {

                    // if this is the correct vendor
                    if (child.getKey().equals(vendID)) {

                        //set the name of the order display object
                        d.setUserName(child.child("name").getValue().toString());
                        Log.v("name", d.getUserName());

                        //add the order to the correct list depending on the status
                        if (d.getStatus().equals("waiting")) {
                            addOrderString(d);
                        } else {
                            addCompleteOrderString(d);
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
     * Take an OrderForDisplay object and parse the fields to create a string that displays the
     * information as desired. Save the string in the list of "waiting" orders.
     * @param d OrderForDisplay object that stores the order information
     */
    private void addOrderString(OrderForDisplay d) {
        orderStringList.add(Html.fromHtml("order ID: "+d.getOrderId()) + "\n" +
                "vendor: " + d.getUserName() + "\n" + "status: " + d.getStatus());
        adapter.notifyDataSetChanged();
    }

    /**
     * Take an OrderForDisplay object and parse the fields to create a string that displays the
     * information as desired. Save the string in the list of "completed" orders.
     * @param d OrderForDisplay object that stores the order information
     */
    private void addCompleteOrderString(OrderForDisplay d) {
        completedOrderStringList.add(Html.fromHtml("order ID: "+d.getOrderId()) + "\n" +
                "vendor: " + d.getUserName() + "\n" + "status: " + d.getStatus());
        completedAdapter.notifyDataSetChanged();
    }

    /**
     * If an order in the "waiting" list is clicked, send user to the order details page.
     * Send the selected order object to the next page.
     * @param adapterView
     * @param v
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(final AdapterView<?> adapterView, View v, final int position, long id) {
        String clickedId = adapterView.getItemAtPosition(position).toString().split("\n")[0];
        clickedId = clickedId.substring(10,clickedId.length());


        for (Order o : orders) {
            if (o.getOrderId().equals(clickedId)) {
                String status = o.getStatus();
                 //if clicked order is waiting, go to order page
                 if ("waiting".equals(status)) {
                     Intent orderPage = new Intent(getApplicationContext(), OrderDetailsCustomer.class);
                     orderPage.putExtra("SelectedOrderObj", o);
                     startActivity(orderPage);
                 }
                 // else, give user option to rate
                 else {
                     Intent reviewVendors = new Intent(getApplicationContext(), ReviewVendors.class);
                     reviewVendors.putExtra("vendorID", o.getVendorId());
                     startActivity(reviewVendors);
                 }
            }
        }


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
