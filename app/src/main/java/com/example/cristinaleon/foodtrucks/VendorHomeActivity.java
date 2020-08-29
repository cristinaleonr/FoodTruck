package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

import static java.lang.Thread.sleep;

/*
Home Activity for Vendors - the screen they see upon login. Has a list of current and completed orders,
as well as options to edit menu, edit vendor info, etc.
 */
public class VendorHomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private String vendID;

    private DatabaseReference mDatabase;
    private Button editMenu;

    private ArrayList<Order> orderList;

    // lists of string formats of orders for display
    private ArrayList<String> orderStringList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> completedOrderStringList;
    private ArrayAdapter<String> completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout and toolbar
        setContentView(R.layout.activity_vendor_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // get vendor id from login activity
        vendID = getIntent().getStringExtra("user");

        //initialize lists to hold orders as well as string representations for display
        orderList = new ArrayList<Order>();
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


        mDatabase = FirebaseDatabase.getInstance().getReference();

        // button for editing menu
        editMenu = (Button) findViewById(R.id.editMenu);
        editMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editMenu = new Intent(getApplicationContext(), EditMenu.class);
                editMenu.putExtra("vendID", vendID);
                startActivity(editMenu);
            }
        });

        getOrders();

        displayRating();

    }


    /**
     *  go to ratings activity
     * */
    public void showReviews(View view) {
        Intent showReviews = new Intent(getApplicationContext(), VendorReviewsDisplay.class);
        showReviews.putExtra("vendID",vendID);
        startActivity(showReviews);
    }

    /**
     *  get rating bar view and modify accordingly
     * */
    private void displayRating() {
        final RatingBar ratingBar = (RatingBar) findViewById(R.id.rating);
        ratingBar.setFocusable(false);
        DatabaseReference ref = mDatabase.child("reviewVendors").child(vendID);

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
     * Method to query the database for orders and to find this vendors's orders for display.
     * Stores the vendor's orders in two lists, one for waiting orders and one for complete.
     */
    private void getOrders() {
        DatabaseReference ref = mDatabase.child("orders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    //if the order's vendor ID == this vendors ID then add the order to the orders list
                    String orderVendor = child.child("restID").getValue().toString();
                    String orderStatus = child.child("status").getValue().toString();

                    if (orderVendor.equals(vendID)) {

                        //Extract order information and store in a created Order object
                        Order o = new Order();

                        o.setVendorId(orderVendor);
                        o.setUserId(child.child("userID").getValue().toString());
                        o.setOrderId(child.getKey());
                        o.setStatus(orderStatus);

                        Iterable<DataSnapshot> orderItems = child.child("items").getChildren();
                        for (DataSnapshot i : orderItems) {
                            o.addItem(i.getValue().toString());
                            Log.v("Item", i.getValue().toString());
                        }

                        //Store the Order object if the order is waiting - those are the only order
                        // types that can be clicked
                        if (orderStatus.equals("waiting")) {
                            orderList.add(o);
                        }

                        //Store order information for display in OrderForDisplay object
                        OrderForDisplay d = new OrderForDisplay();
                        d.setOrderId(o.getOrderId());
                        d.setStatus(orderStatus);

                        //Query database for order user and display
                        getOrderUser(o.getUserId(), o.getStatus(), d);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void addOrderString(OrderForDisplay d) {
        orderStringList.add(Html.fromHtml("<b>"+d.getOrderId()+"</b>") + "\n" +
                "customer: " + d.getUserName() );
        adapter.notifyDataSetChanged();
    }

    private void addCompletedOrder(OrderForDisplay o) {
        completedOrderStringList.add(Html.fromHtml("<b>"+o.getOrderId()+"</b>")+ "\n" +
                "customer: " + o.getUserName());
        completedAdapter.notifyDataSetChanged();
    }


    /**
     * Query the  database to get the user name and store it in the order display object.
     * Once the query is complete, turn the display object into a string for display.
     * @param userID id of the user that the order is for
     * @param orderStatus
     * @param displayOrder OrderForDisplay object that stores information
     */
    private void getOrderUser(final String userID, final String orderStatus, final OrderForDisplay displayOrder) {

        DatabaseReference ref = mDatabase.child("customerInfo");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    // if this is the correct user
                    if (child.getKey().equals(userID)) {

                        //create single name string
                        String firstName = child.child("firstName").getValue().toString();
                        String lastName = child.child("lastName").getValue().toString();
                        displayOrder.setUserName(firstName + " " + lastName);
                        Log.v("name", displayOrder.getUserName());

                        //add order to correct list depending on status
                        if (orderStatus.equals("waiting")) {
                            addOrderString(displayOrder);
                        } else {
                            addCompletedOrder(displayOrder);
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
     * When an order that is "waiting" is clicked, take vendor to the OrderDetails page to see
     * more information
     * @param adapterView
     * @param v
     * @param position
     * @param id
     */
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Intent orderPage = new Intent(getApplicationContext(), OrderDetailsVendor.class);
        Log.v("Clicked ID",id+"");
        Log.v("Clicked", orderStringList.get((int)id).toString());

        orderPage.putExtra("SelectedOrderObj", orderList.get((int)id));
        startActivity(orderPage);
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
        getMenuInflater().inflate(R.menu.menu_vendor_home,menu);
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
                return true;

            // if selected item is edit, go to edit info screen
            case R.id.edit:
                Intent editVendor = new Intent(getApplicationContext(), EditVendor.class);
                startActivity(editVendor);
                return true;

            // if selected item is cart, go to cart screen
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
