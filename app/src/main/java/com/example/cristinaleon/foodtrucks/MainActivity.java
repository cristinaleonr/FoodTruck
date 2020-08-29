package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * User landing page
 *
 * <p>Allows user to log in or make an account
 *
 * <p>Bugs: none
 *
 * @author cristinaleon
 */

public class MainActivity extends AppCompatActivity {

    /** reference to buttons for customer and vendor sign up */
    TextView customerAct, vendorAct;

    /** reference to login button */
    Button login;

    /** firebase database reference */
    private FirebaseAuth mAuth;

    /** email and password input fields */
    EditText emailView;
    EditText passwordView;

    /** email and password input field strings */
    String email;
    String password;

    /** firebase database reference */
    private DatabaseReference mDatabase;

    /** intent for next activity */
    Intent home;

    /** reference to user id */
    String uid;


    /**
     * Create activity, set up layout, toolbar, and initialize instance variables
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set up layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        //initialize references to buttons, input fields, and database
        customerAct = findViewById(R.id.customerAct_tv);
        vendorAct = findViewById(R.id.vendorAct_tv);
        login = (Button) findViewById(R.id.loginBtn);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();




        //when the user clicks on the textbox that says "create customer account"...
        customerAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to client registration screen
                Intent client = new Intent(getApplicationContext(), ClientRegistrationPage.class);
                startActivity(client);

            }
        });

        //when the user clicks on the textbox that says "create vendor account"...
        vendorAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vendor = new Intent(getApplicationContext(), VendorRegistration.class);
                startActivity(vendor);
            }
        });



        //logic for when the user clicks login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get contents of input fields and store them as trings
                emailView = findViewById(R.id.email_et);
                passwordView = findViewById(R.id.password_et);
                email = emailView.getText().toString();
                password = passwordView.getText().toString();


                // show error message if user does not fill out all authentication fields
                if (email.equals("") || password.equals("")) {
                    Toast toast = Toast.makeText(getBaseContext(), Html.fromHtml("<b>Please fill out all fields!</b>"), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                }
                else {
                    // try to authenticate user
                    mAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("sucess", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        uid = user.getUid();

                                        // get appropriate database child reference to add user
                                        DatabaseReference ref = mDatabase.child("userType").child(user.getUid());
                                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Log.v("DATA",dataSnapshot.toString());
                                                String type = dataSnapshot.getValue(String.class);
                                                // go on to next activity
                                                nextActivity(type);

                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });



                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("fail", "signInWithEmail:failure", task.getException());
                                        Toast toast = Toast.makeText(MainActivity.this, Html.fromHtml("<b>Invalid email or password!</b>"),
                                                Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                                        toast.show();


                                    }


                                }
                            });
                }
            }
        });



    }

    /**
     * Calls the appropriate activity when the user is authenticated
     *
     * @param type Type of user logging in
     */
    private void nextActivity(String type) {

        // check if user is of type customer or vendor, and make corresponding intent
        if (type.equals("customer")) {
            home = new Intent(getApplicationContext(), CustomerHomeActivity.class);
        } else if (type.equals("vendor")) {
            home = new Intent(getApplicationContext(), VendorHomeActivity.class);
        }
        // call next activity
        home.putExtra("user", uid);
        home.putExtra("Type",type);
        startActivity(home);

    }

}