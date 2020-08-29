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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Home screen for customers
 *
 * <p>Displays list of vendors
 *
 * <p>Bugs: none
 *
 * @author cristinaleon
 */

public class CustomerHomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    /** ListView that contains and displays vendor items */
    private ArrayList<String> vendorList;

    /** adapter to populate ListView of vendor items */
    private ArrayAdapter<String> adapter;

    /** reference to firebase database */
    private DatabaseReference mDatabase;

    /** stores list of database keys for displayed vendors */
    private ArrayList<String> vendorKeys;

    private String userID;

    /**
     * Create activity, set up layout, toolbar, and initialize instance variables
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set up layout and toolbar
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        vendorList = new ArrayList<String>();
        vendorKeys = new ArrayList<String>();

        // instantiate vendor ListView and its adapter
        ListView listView = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                vendorList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // initialize database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //get user id of current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        // button for viewing current orders
        Button currentOrders = (Button) findViewById(R.id.currentOrders);
        currentOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewOrders = new Intent(getApplicationContext(), CustomerCurrentOrdersActivity.class);
                viewOrders.putExtra("userID", userID);
                startActivity(viewOrders);
            }
        });

        // populate list with vendor items
        getVendors();


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


    /**
     * Add all vendor entries in database to displayed list
     */
    private void getVendors() {
        // get appropriate database child and list to store items
        LinkedList<Vendor> vendors = new LinkedList<Vendor>();
        DatabaseReference ref = mDatabase.child("vendorInfo");

        // iterate over entries in vendor child
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            // get snapshot of all children under vendor info branch
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // iterate over vendor entries and add them to vendorList
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Vendor vendor = child.getValue(Vendor.class);
                    addVendor(vendor);
                    // add vendor key to vendorKeys
                    addVendorKey(child.getKey());
                    Log.v("Vendor",child.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //return vendors;
    }

    /**
     * Add single vendor key to vendorKeys
     *
     * @param key Vendor key to be added
     */
    private void addVendorKey(String key) {
        vendorKeys.add(key);
    }

    /**
     * Add single vendor to vendorList with appropriate display format
     *
     * @param v Vendor to be added
     */
    private void addVendor(Vendor v) {
        vendorList.add(Html.fromHtml("<b>"+v.name+"</b>") +"\n" + "Address: " + v.address + "\n" +
                "Type: " + v.type + "\t   Phone: " + v.phone + "\nHours: "
                + v.hours);
        adapter.notifyDataSetChanged();
    }

    /**
     * Handle click of item on itemList
     *
     * @param adapterView Adapter for layout's ListView
     * @param v View that contains clicked item
     * @param position Item position in View
     * @param id Item id in itemList
     */
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Intent restPage = new Intent(getApplicationContext(), VendorPage.class);
        // log clicked item information
        Log.v("Clicked ID",id+"");
        Log.v("Clicked",vendorList.get((int)id).toString());
        Log.v("Clicked Key",vendorKeys.get((int)id).toString());

        // go to selected vendor's page
        restPage.putExtra("SelectedVendor",vendorList.get((int)id).toString());
        restPage.putExtra("SelectedVendorKey",vendorKeys.get((int)id).toString());
        startActivity(restPage);
    }

}
