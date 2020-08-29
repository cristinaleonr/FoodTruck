package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
 * Allows the user to edit customer details
 *
 * <p>Bugs: none
 *
 * @author cristinaleon/michaelplumb
 */

public class EditCustomer extends AppCompatActivity {

    /**
     * Create activity, set up layout and toolbar
     *
     * @param savedInstanceState
     *
     */

    //button at the bottom of the screen
    private Button finalizeEdits;
    //text field for the first entry of the password
    private EditText firstpw;
    //text field for the re entry of the password
    private EditText secondpw;
    //text field for the changing of the phone number
    private EditText newPhone;
    //text field for old password
    private EditText oldPass;

    //firebase reference
    private DatabaseReference mDatabase;
    /*
     * will handle the button click for finalizing edits to customer accounts
     * and will take note of what is in the edit texts when the button is clicked
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //assign each of the global variables to a physical area of screen
        finalizeEdits = findViewById(R.id.finalizeEditsCust);
        firstpw = findViewById(R.id.editPw1Cust);
        secondpw = findViewById(R.id.editPw2Cust);
        newPhone = findViewById(R.id.editPhoneCust);
        oldPass = findViewById(R.id.oldPwCust);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        final String email = mAuth.getCurrentUser().getEmail();
        final FirebaseUser user = mAuth.getCurrentUser();



        /*
         * handle the on click of the finalize edit button
         * if the passwords don't match, nothing should be done,
         * if the passwords match but the phone field is blank, we should edit the password but not
         * the phone and vice versa. If both are filled in and valid, we edit both. A Toast will
         * appear on screen marking what has happened
         */
        finalizeEdits.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                final String password1 = firstpw.getText().toString();
                final String password2 =  secondpw.getText().toString();
                final String phone = newPhone.getText().toString();
                final String oldpw = oldPass.getText().toString();

                //data base ref to customerInfo
                DatabaseReference ref = mDatabase.child("customerInfo");

                //private DatabaseReference mDatabase;

                //no fields are filled and button is pressed, do nothing and display a toast
                if (password1.equals("") && password1.equals("") && phone.equals("")) {
                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Please fill out at least one field to edit!</b>"),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    return;
                }

                /*
                 *just edit the phone number and display a toast that the phone number
                 * for the account has been changed
                 */
                else if (password1.equals("") && password1.equals("") && !phone.equals("")) {
                    //change the phone number in the DB
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                if (uid.equals(child.getKey())) {
                                    child.getRef().child("phoneNumber").setValue(phone);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //toast that the phone number only has been changed
                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Phone number for this account has been changed</b>"),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();

                }

                //one of the password fields is not filled in, display toast
                else if ((!password1.equals("") && password2.equals("")) || (password1.equals("") && !password2.equals(""))) {

                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Password must be entered twice for a valid change!</b>"),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    return;

                }



                //password fields are both filled in, phone number is not
                else if (!password1.equals("") && !password2.equals("") && phone.equals("") && !oldpw.equals("")) {
                    if (password1.equals(password2)) {
                        if (isValidPassword(password1)) {
                            //passwords are equal, we can change them in the DB
                            //handle Firebase login password change

                            //check new password has 1 number and 3 caps

                            //we need to reauthenticate the user and change them
                            AuthCredential cred = EmailAuthProvider.getCredential(email, oldpw);
                            user.reauthenticate(cred).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(password1).addOnCompleteListener(new OnCompleteListener<Void>() {
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


                            Toast toast = Toast.makeText(getBaseContext(),
                                    Html.fromHtml("<b>Password has been changed for this account!</b>"),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        } else {
                            //password is not valid, does not have at least one cap and one letter
                            Toast toast = Toast.makeText(getBaseContext(),
                                    Html.fromHtml("<b>Passwords needs at least 1 capital letter " +
                                            "and one number!</b>"),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                            return;

                        }

                    }
                    else {

                        //passwords are not equal, we need to display a toast
                        Toast toast = Toast.makeText(getBaseContext(),
                                Html.fromHtml("<b>Passwords do not match!</b>"),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                        return;

                    }
                }

                //phone and password are non null and possibly need to be changed
                else if (!password1.equals("") && !password2.equals("") && !phone.equals("")) {

                    if (password1.equals(password2)) {
                        if (isValidPassword(password1)) {
                            //change both password and phone numbers in the firebase and DB

                            //handle Firebase login password change

                            //we need to reauthenticate the user and change them
                            AuthCredential cred = EmailAuthProvider.getCredential(email, oldpw);
                            user.reauthenticate(cred).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(password1).addOnCompleteListener(new OnCompleteListener<Void>() {
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


                            //phone number change in DB
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                    for (DataSnapshot child : children) {
                                        if (uid.equals(child.getKey())) {
                                            child.getRef().child("phoneNumber").setValue(phone);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            Toast toast = Toast.makeText(getBaseContext(),
                                    Html.fromHtml("<b>Password and phone numbers have been changed for this account!</b>"),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                        else {
                            Toast toast = Toast.makeText(getBaseContext(),
                                    Html.fromHtml("<b>Password Needs at least one " +
                                            "capital letter and one number!</b>"),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }

                    else {
                        //passwords are not equal, we need to display a toast
                        Toast toast = Toast.makeText(getBaseContext(),
                                Html.fromHtml("<b>Passwords do not match!</b>"),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                        toast.show();
                        return;
                    }
                }


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
        switch(item.getItemId()) {
            // if selected item is home, go to home screen
            case R.id.home:
                Intent homeActivity = new Intent(getApplicationContext(), CustomerHomeActivity.class);
                startActivity(homeActivity );
                return true;

            // if selected item is edit, stay in this screen
            case R.id.edit:
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

}
