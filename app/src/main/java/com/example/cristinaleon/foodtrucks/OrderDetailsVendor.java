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

public class OrderDetailsVendor extends AppCompatActivity {


    //fields for textboxes to display with data from the DB
    TextView nameDisplay;
    TextView priceView;
    TextView phoneDisplay;
    Button cancelButton;
    Button completeButton;

    //data base variable for class use
    private DatabaseReference mDatabase;
    ArrayList<String> itemList = new ArrayList<String>();
    //adapter for listView
    private ArrayAdapter<String> adapter;
    //total price of the order requested
    public Double totalPrice = 0.0;
    //information about the order for display purposes
    private String userID;
    private String vendID;
    private String orderID;

    Intent home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ListView listView = (ListView) findViewById(R.id.itemView);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                itemList);
        listView.setAdapter(adapter);

        Intent i = getIntent();
        Order orderInfo = (Order)i.getSerializableExtra("SelectedOrderObj");


        orderID = orderInfo.getOrderId();
        vendID = orderInfo.getVendorId();
        userID = orderInfo.getUserId();
        ArrayList<String> items = orderInfo.getItems();

        Log.v("orderid", orderID);
        Log.v("vendorId", vendID);
        Log.v("userId", userID);


        //we have successfully extracted the information we need to display the orders


        //********Details to display********

        //display name of customer, phone number
        nameDisplay = findViewById(R.id.nameDisplay);
        phoneDisplay = findViewById(R.id.phoneDisplay);


        //populates listView with ordered items
        getOrderedItems(vendID, items);

        //set total price functionality
        this.priceView = findViewById(R.id.priceView);
        priceView.setText("$" + totalPrice);

        //populate textbox for customer name and phone
        getUser(userID);

        /*
        * Button Functionality
        * */
        cancelButton = findViewById(R.id.cancelButton);
        completeButton = findViewById(R.id.completeButton);

        /*****Buttons for cancelling and completing orders******/

        //cancel button onClick
        cancelButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view){
            //remove the order in question from the DB entirely and go back to vendorHome activity
            cancelOrder();
            Log.v("order id", orderID);
            Log.v("order status FROM CLICK", "canceled");
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //go back to vendorHomeActivity
            backtoVendorHome();

        }
        });

        //complete button onClick
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //change order status to completed. First we need to find the order object in the DB
                markAsCompleted();
                Log.v("order id", orderID);
                Log.v("order status FROM CLICK", "completed");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //go back to vendorHomeActivity
                Intent i = new Intent(getApplicationContext(), ReviewUsers.class);
                i.putExtra("userID", userID);
                startActivity(i);
      //          backtoVendorHome();
//               finish();

            }
        });
    }

    public void getUser(final String userID) {
        DatabaseReference ref = mDatabase.child("customerInfo");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Customer customer = child.getValue(Customer.class);
                    if (child.getKey().equals(userID)) {
                        nameDisplay.setText(customer.firstName + " " + customer.lastName);
                        phoneDisplay.setText(customer.phoneNumber);

                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });



    }


    /**
     * marks order as completed in the DB and appears on the vendor home page when it returns
     */
    private void markAsCompleted() {
        DatabaseReference ref = mDatabase.child("orders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (checkOrderID(child.getKey())) {
                        //Order o = child.getValue(Order.class);
                        child.getRef().child("status").setValue("complete");
                        finish();
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //backtoVendorHome();
//        finish();
    }

    /**
     * @param orderIDtoCheck
     * @return
     * helper to check order ID
     */
    private boolean checkOrderID(String orderIDtoCheck) {
        return (orderID.trim().equals(orderIDtoCheck.trim()));
    }

    //when the user clicks the complete button, the order must be found and marked as such

    /**
     * Cancels an order and removes it from the database
     */
    private void cancelOrder() {
        DatabaseReference ref = mDatabase.child("orders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (checkOrderID(child.getKey().trim())) {
                        child.getRef().removeValue();
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        finish();
    }




    /*
     * helper function to get items the user orders
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
                        //Log.v("boolean check", "" + s.equals(child.getKey().toString().trim()));
                        //have to trim the strings for white spaces
                        Log.v("item IN ON DC", s);
                        if (s.trim().equals(child.getKey().trim())) {
                            //price += Double.parseDouble(item.price);
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

    /*
     * helper function to send vendor back to the vendor home page after
     * cancel or complete is pressed on an order
     */
    private void backtoVendorHome() {
        home = new Intent(getApplicationContext(), VendorHomeActivity.class);
        home.putExtra("user", vendID.trim());
        //home.putExtra("Type",type);
        startActivity(home);

    }


    /**
     * @param i
     * gets items in an order for display alongside the price
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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        switch(item.getItemId()) {
            // if selected item is home, go to vendor home screen
            case R.id.home:
                Intent homeActivity = new Intent(getApplicationContext(), VendorHomeActivity.class);
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                homeActivity.putExtra("user", uid);
                startActivity(homeActivity );
                return true;

            // if selected item is edit, go to edit info screen
            case R.id.edit:
                Intent editVendor = new Intent(getApplicationContext(), EditVendor.class);
                startActivity(editVendor);
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
