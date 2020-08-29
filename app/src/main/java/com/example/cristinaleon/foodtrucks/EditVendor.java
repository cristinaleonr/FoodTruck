package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Allows the user to edit vendor details
 *
 * <p>Bugs: none
 *
 * @author cristinaleon/michaelplumb
 */

public class EditVendor extends AppCompatActivity {

    //fields for each of the buttons and textEdits on the screen
    private Button finalizeEdits;
    private EditText firstpw;
    private EditText secondpw;
    private EditText newPhone;
    private EditText oldPass;
    private EditText newName;
    private EditText newHours;
    private EditText newBankNo;
    private EditText newType;
    private EditText newAddress;


    //firebase reference
    private DatabaseReference mDatabase;


    /**
     * Create activity, set up layout and toolbar
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vendor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //assign each of the global variables to a physical area of screen
        finalizeEdits = findViewById(R.id.finalizeEditsVend);
        firstpw = findViewById(R.id.editPasswordVend);
        secondpw = findViewById(R.id.reEnterEditedPwVend);
        newPhone = findViewById(R.id.editPhoneVend);
        oldPass = findViewById(R.id.oldPWVend);
        newBankNo = findViewById(R.id.changeBankInfoVend);
        newHours = findViewById(R.id.changeHours);
        newType = findViewById(R.id.changeTypeVend);
        newName = findViewById(R.id.changeNameVend);
        newAddress = findViewById(R.id.changeAddress);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String email = mAuth.getCurrentUser().getEmail();
        final FirebaseUser user = mAuth.getCurrentUser();


        //handling what happens when the finalize edits button is clicked
        finalizeEdits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get what's in each textbox at the time of the button click
                String password1 = firstpw.getText().toString();
                String password2 =  secondpw.getText().toString();
                String phone = newPhone.getText().toString();
                String oldpw = oldPass.getText().toString();
                String address = newAddress.getText().toString();
                String bankInfo = newBankNo.getText().toString();
                String hours = newHours.getText().toString();
                String type = newType.getText().toString();
                String name = newName.getText().toString();
                //tells you what is changed in a toast at the end
                String outputFinal = "Changed Items: ";

                //check each text field for non empty strings. Treat password carefully
                if (!phone.equals("")) {
                    changePhone(phone);
                    outputFinal+= "phone, ";
                }

                if (!name.equals("")) {
                    changeName(name);
                    outputFinal+= "name, ";
                }

                if(!bankInfo.equals("")) {
                    changeBankInfo(bankInfo);
                    outputFinal+= "bank no., ";
                }

                if (!address.equals("")) {
                    changeAddress(address);
                    outputFinal+= "address, ";
                }

                if(!hours.equals("")) {
                    changeHours(hours);
                    outputFinal+= "hours, ";
                }

                if(!type.equals("")) {
                    changeType(type);
                    outputFinal+= "vendor type, ";
                }

                if (!password1.equals("") && !password2.equals("")) {
                    if (password1.equals(password2)) {
                        if (isValidPassword(password1)) {
                            if (!oldpw.equals("")) {
                                changePassword(oldpw, password1);
                                outputFinal += "password, ";
                            }
                            else {
                                Toast toast = Toast.makeText(getBaseContext(),
                                        Html.fromHtml("<b>Must re type old password in order to change!</b>"),
                                        Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                                toast.show();
                                return;
                            }
                        }

                        else {
                            Toast toast = Toast.makeText(getBaseContext(),
                                    Html.fromHtml("<b>Password must contain at least one capital letter" +
                                            "and one number!</b>"),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                            toast.show();
                            return;

                        }
                    }

                    else {
                        Toast toast = Toast.makeText(getBaseContext(),
                                Html.fromHtml("<b>Password must match!</b>"),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                        toast.show();
                        return;

                    }

                }

                //do nothing if password fields are blank
                else if (password1.equals("") && password2.equals("")) {

                }

                //toast that one of the password fields is blank if it is
                else {
                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Password must be entered twice for a valid change!</b>"),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    return;

                }
                Toast toast = Toast.makeText(getBaseContext(),
                        Html.fromHtml("<b>" + outputFinal + "</b>"),
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                toast.show();
                return;

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

            // if selected item is edit, stay in this screen
            case R.id.edit:
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



    //We define those passwords that have at least 1 capital letter and 1 numeric digit as valid
    /*
     *checks for valid passwords
     */
    public boolean isValidPassword(String password) {
        int numCaps = 0;
        int numDigits = 0;

        for (int i = 0; i < password.length(); i++) {
            //check if character is a capital letter, if so, add to caps count
            if (Character.isUpperCase(password.charAt(i))) {
                numCaps++;
            }

            //check if character is a digit 0-9
            if (Character.isDigit(password.charAt(i))) {
                numDigits++;
            }
        }
        Log.d("caps", Integer.toString(numCaps));
        Log.d("digits", Integer.toString(numDigits));
        return (numCaps >= 1 && numDigits >= 1);
    }


    /**
     * @param oldpw
     * @param newPass
     *
     * Reauthenticates user and changes password if two passwords equal and if new
     * password meets criteria of one capital letter and one number. Notifies user if successful
     *
     */
    public void changePassword(String oldpw, String newPass) {

        //change both password and phone numbers in the firebase and DB

        //handle Firebase login password change

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String uid = mAuth.getCurrentUser().getUid();
        final String email = user.getEmail();
        final String toChange = newPass;

        //we need to reauthenticate the user and change them
        AuthCredential cred = EmailAuthProvider.getCredential(email, oldpw);
        user.reauthenticate(cred).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(toChange).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast toast = Toast.makeText(getBaseContext(),
                                        Html.fromHtml("<b>Password changed!</b>"),
                                        Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(getBaseContext(),
                                        Html.fromHtml("<b>error, password not changed!</b>"),
                                        Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                            }

                        }
                    });

                } else {
                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Account Authentication Error</b>"),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
            }
        });


    }


    /**
     * @param phone
     * changes phone number of vendor in DB
     */
    public void changePhone(String phone) {
        //data base ref to customerInfo
        DatabaseReference ref = mDatabase.child("vendorInfo");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String phoneChange = phone;

        //phone number change in DB
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (uid.equals(child.getKey())) {
                        child.getRef().child("phone").setValue(phoneChange);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * @param address
     * Changes addresss of vendor in DB
     */
    public void changeAddress(String address) {
        //data base ref to customerInfo
        DatabaseReference ref = mDatabase.child("vendorInfo");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String addressChange = address;

        //phone number change in DB
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (uid.equals(child.getKey())) {
                        child.getRef().child("address").setValue(addressChange);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * changes name of vendor in DB
     * @param name
     */
    public void changeName(String name) {
        //data base ref to customerInfo
        DatabaseReference ref = mDatabase.child("vendorInfo");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String nameChange = name;

        //phone number change in DB
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (uid.equals(child.getKey())) {
                        child.getRef().child("name").setValue(nameChange);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * @param type
     * changes restaurant type in database
     */
    public void changeType(String type) {
        //data base ref to customerInfo
        DatabaseReference ref = mDatabase.child("vendorInfo");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String dataChange = type;

        //phone number change in DB
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (uid.equals(child.getKey())) {
                        child.getRef().child("type").setValue(dataChange);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * @param newHours
     * changes hours in database
     */
    public void changeHours(String newHours) {
        //data base ref to customerInfo
        DatabaseReference ref = mDatabase.child("vendorInfo");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String dataChange = newHours;

        //phone number change in DB
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (uid.equals(child.getKey())) {
                        child.getRef().child("hours").setValue(dataChange);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /*
     * changes bank info in database
     */
    public void changeBankInfo(String bankInfo) {
        //data base ref to customerInfo
        DatabaseReference ref = mDatabase.child("vendorInfo");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String dataChange = bankInfo;

        //phone number change in DB
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (uid.equals(child.getKey())) {
                        child.getRef().child("bankAccountNumber").setValue(dataChange);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }






}
