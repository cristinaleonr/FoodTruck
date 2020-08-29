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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Cart Page for users - the screen they see when they click the cart icon in the top left.
 * Has a list of all items in the cart along with the price and total of the cart
 */
public class CartPage extends AppCompatActivity {
    private static DecimalFormat dc2 = new DecimalFormat(".##");
    LinkedList<String> itemIDs = new LinkedList<String>();
    ArrayList<String> itemList = new ArrayList<String>();
    String cartVendor;
    ArrayAdapter<String> adapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    double total;
    String userBalance;
    String vendorBalance;

    /**
     * @param savedInstanceState
     * Displays the activity_cart_page layout which shows a list of items
     * along with a checkout button
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        ListView listView = (ListView) findViewById(R.id.clist);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                itemList);
        listView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
        //CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        total = 0.0;
        getItems();
        Button checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkout(v);
            }
        });
    }


    /**
     * getItems accesses the user's cart in the firebase
     * to get all of the IDs of the items in the cart
     */
    private void getItems() {
        DatabaseReference ref = mDatabase.child("cartInfo");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(uid)) {
                        Iterable<DataSnapshot> vendorID = child.getChildren();
                        for (DataSnapshot vendor : vendorID) {
                            cartVendor = vendor.getKey().toString();
                            Iterable<DataSnapshot> items = vendor.getChildren();
                            for (DataSnapshot item : items) {
                                Log.v("HERE", item.getValue().toString());
                                itemIDs.push(item.getValue().toString());
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /**The items in the cart are stored as ids from the vendor menu
         * so we have to get the relevant information using the items' IDs
         * from the restaurant's menu stored in the firebase
        */

        DatabaseReference ref2 = mDatabase.child("vendorMenu");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(cartVendor)) {
                        for (String item : itemIDs) {
                           addItem(child.child(item).getValue(Item.class));

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
     * @param v
     * checkout() is the function connected to the checkout button that
     * adds all current items in the cart to the Firebase under the
     * specific restaurant's order page and sets the order status to waiting.
     * It simultaneously removes all the data from the user's cart.
     */
    private void checkout(View v) {
        if (itemIDs.isEmpty()) {
            Toast toast = Toast.makeText(getBaseContext(), Html.fromHtml("No items in cart"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();
        DatabaseReference ref = mDatabase.child("customerInfo");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(uid)) {
                        Iterable<DataSnapshot> userchild = child.getChildren();
                        for (DataSnapshot info : userchild) {
                            if (info.getKey().equals("balance")) {
                                userBalance = info.getValue().toString();
                                if (Double.parseDouble(userBalance) < total) {
                                    Toast toast =
                                            Toast.makeText(getBaseContext(),
                                                    Html.fromHtml("User balance too low for purchase"),
                                                    Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                                    toast.show();
                                    return;
                                }
                                else {
                                    Double newBalance = Double.parseDouble(userBalance) - total;
                                    mDatabase.child("customerInfo").child(uid).child("balance").setValue(String.valueOf(newBalance));
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference ref1 = mDatabase.child("orders");
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                /*
                We get the highest current order number in the cart and add 1 to the number
                to put the new order under.
                 */
                int i = 1;
                for (DataSnapshot child : children) {
                    String substring = child.getKey().toString().substring(2, child.getKey().toString().length());
                  if ( Integer.parseInt(substring) > i) {
                      i = Integer.parseInt(substring);
                  }
                }
                i++;
                mDatabase.child("orders").child("o_" + i).child("restID").setValue(cartVendor);
                mDatabase.child("orders").child("o_" + i).child("userID").setValue(uid);
                mDatabase.child("orders").child("o_" + i).child("status").setValue("waiting");
                int j = 1;

                /*
                Put all items in the cart into the order stored under their item ID
                 */
                for (String item : itemIDs) {
                    mDatabase.child("orders").child("o_" + i).child("items").child("i" + j).
                            setValue(item);
                    j++;
                }
                mDatabase.child("cartInfo").child(uid).removeValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference ref2 = mDatabase.child("vendorInfo");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(cartVendor)) {
                        Iterable<DataSnapshot> userchild = child.getChildren();
                        for (DataSnapshot info : userchild) {
                            if (info.getKey().equals("balance")) {
                                vendorBalance = info.getValue().toString();
                                Double newBalance = Double.parseDouble(vendorBalance) + total;
                                mDatabase.child("vendorInfo").child(cartVendor).child("balance").setValue(String.valueOf(newBalance));
                            }
                        }
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
     * @param v
     * addItem is the function used to populate the list view
     * in the activity with the various cart items by adding the
     * Firebase cart items to itemList. It simultaneously gets the total
     * price of the cart items
     */
    private void addItem(Item v) {
        total += Double.parseDouble(v.price);
        itemList.add(Html.fromHtml("<b>"+v.name+"</b>") +"\n" + "Price: " + v.price + "\n");
        adapter.notifyDataSetChanged();
        TextView totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText("Total: " + dc2.format(total));
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
