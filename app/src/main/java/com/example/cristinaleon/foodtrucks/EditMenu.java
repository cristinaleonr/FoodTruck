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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Allows vendor to view and edit its menu
 *
 * <p>Bugs: none
 *
 * @author cristinaleon
 */

public class EditMenu extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /** reference to firebase database */
    private DatabaseReference mDatabase;

    /** adapter to populate ListView of menu items */
    private ArrayAdapter<String> adapter;

    /** ListView that contains and displays menu items */
    private ArrayList<String> itemList;
    private ArrayList<String> itemKeyList;

    /** reference to database id of logged in vendor */
    private String vendId;

    /**
     * Create activity, set up layout, toolbar, and initialize instance variables
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set up layout and toolbar
        setContentView(R.layout.activity_edit_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        itemList = new ArrayList<String>();
        itemKeyList = new ArrayList<String>();

        // get vendor id passed in from previous activity
        vendId = getIntent().getStringExtra("vendID");

        // initialize database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // instantiate menu item ListView and its adapter
        ListView listView = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                itemList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // populate list with menu items
        getItems();

        // instantiate button to add items
        final Button addItem = (Button) findViewById(R.id.addItem);

        addItem.setOnClickListener(new View.OnClickListener() {
            EditText itemName = (EditText) findViewById(R.id.itemNameField);
            EditText itemDescription = (EditText) findViewById(R.id.itemDescriptionField);
            EditText itemPrice = (EditText) findViewById(R.id.itemPriceField);

            // handle click of addItem button
            @Override
            public void onClick(View view) {
                // when add item button is clicked, get local database and user id referenc
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                String vID = user.getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference();

                // get database child
                DatabaseReference ref= mDatabase.child("vendorMenu").child(vID);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        // iterate over exiting items in database to get next unique id
                        int max = 1;
                        for (DataSnapshot child : children) {
                            String substring = child.getKey().toString().substring(child.getKey().toString().length() - 1);
                            if ( Integer.parseInt(substring) > max) {
                                max = Integer.parseInt(substring);
                            }
                        }
                        max++;
                        // make Item object with new item info and add to displayed list
                        String name = itemName.getText().toString();
                        String description = itemDescription.getText().toString();
                        String price = itemPrice.getText().toString();
                        Item item = new Item(name, price, description);
                        addItem(item);

                        // add new item to database
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        String vID = user.getUid();
                        DatabaseReference ref= mDatabase.child("vendorMenu").child(vID);
                        ref.child("item_"+max).setValue(item);
                        itemKeyList.add("item_"+max);

                        // clear new item fields
                        itemName.setText("");
                        itemDescription.setText("");
                        itemPrice.setText("");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });



    }

    /**
     * Add all menu items for the current vendor in database to displayed list
     */
    private void getItems() {
        // get appropriate database child
        DatabaseReference ref = mDatabase.child("vendorMenu").child(vendId);

        // iterate over entries in menu child
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            // get snapshot of all children under menu branch
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // iterate over menu items and add them to itemList
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Item item = child.getValue(Item.class);
                    Log.v("Item",child.getValue().toString());
                    addItem(item);
                    itemKeyList.add(child.getKey());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Add single item to itemList with appropriate format
     *
     * @param i Item to be added
     */
    private void addItem(Item i) {
        itemList.add(i.name +"                      " + i.description +
                "                      " + i.price);
        adapter.notifyDataSetChanged();
    }


    /**
     * When an item in itemList is clicked, display a popup that asks if you would like to delete
     * the item
     *
     * @param adapterView Adapter for layout's ListView
     * @param v View that contains clicked item
     * @param position Item position in View
     * @param id Item id in itemList
     */
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        // log clicked item information
        Log.v("Clicked ID",id+"");
        Log.v("Clicked Item",itemList.get((int)id));
        Log.v("Clicked Key",itemKeyList.get((int)id));

        AlertDialog.Builder adb=new AlertDialog.Builder(EditMenu.this);
        adb.setTitle("Delete?");
        adb.setMessage("Are you sure you want to delete this item?");
        final int positionToRemove = position;
        final String itemID = itemKeyList.get((int)id);
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                itemList.remove(positionToRemove);
                adapter.notifyDataSetChanged();
                deleteMenuItem(itemID);
            }});
        adb.show();

    }

    public void deleteMenuItem(final String itemID) {
        final DatabaseReference ref = mDatabase.child("vendorMenu").child(vendId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Item item = child.getValue(Item.class);
                    if (child.getKey().equals(itemID)) {
                        Log.v("item", item.name);
                        ref.child(itemID).removeValue();

                    }
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
            // if selected item is home, go to home screen
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
