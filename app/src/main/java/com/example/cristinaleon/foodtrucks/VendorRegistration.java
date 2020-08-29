package com.example.cristinaleon.foodtrucks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by vikramkhemlani on 2/19/18.
 */

public class VendorRegistration extends AppCompatActivity {

    private String location;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_registration_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //create is the name of the function that is linked to create account button.
    //create takes the input from all the fields, checking to make sure they are
    //valid and not empty, and subsequently stores them in the database.

    public void create(View view) {
        EditText nameField = findViewById(R.id.rNameInput);
        EditText emailField = findViewById(R.id.rEmailAddress);
        EditText phoneField = findViewById(R.id.rPhoneInput);
        EditText password1Field = findViewById(R.id.rPasswordFirst);
        EditText password2Field = findViewById(R.id.rPasswordSecond);
        EditText cuisineField = findViewById(R.id.rCuisineType);
        EditText address1Field = findViewById(R.id.address1Type);
        EditText address2Field = findViewById(R.id.address2Type);
        EditText cityField = findViewById(R.id.cityType);
        EditText stateField = findViewById(R.id.stateType);
        EditText zipCodeField = findViewById(R.id.zipCodeType);
        EditText bankAccountField = findViewById(R.id.bankingInformationType);
        EditText hoursField = findViewById(R.id.hoursType);

        final String email = emailField.getText().toString();
        final String phone = phoneField.getText().toString();
        final String name = nameField.getText().toString();
        location = address1Field.getText().toString();
        if (!address2Field.getText().toString().isEmpty()) {
            location += ";" + address2Field.getText().toString();
        }
        location += ";" + cityField.getText().toString();
        location += ";" + stateField.getText().toString();
        location += ";" + zipCodeField.getText().toString();
        final String password1 = password1Field.getText().toString();
        final String password2 = password2Field.getText().toString();
        final String cuisine = cuisineField.getText().toString();
        final String bankAccountNumber = bankAccountField.getText().toString();
        final String hours = hoursField.getText().toString();
        if (email.isEmpty() || phone.isEmpty() || name.isEmpty() || address1Field.toString().isEmpty()
                || cityField.toString().isEmpty() || stateField.toString().isEmpty() || password1.isEmpty() ||
                password2.isEmpty() || cuisine.isEmpty() || bankAccountNumber.isEmpty() || hours.isEmpty()) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Please fill out all fields!</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }

        if (!isValidString(cityField.getText().toString())) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Please enter a valid city</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }
        if (!isValidString(stateField.getText().toString())) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Please enter a valid state</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }

        if (!isValidString(cuisine)) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Please enter a valid cuisine</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }
        if (!isValidNumber(bankAccountNumber)) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Please enter a valid bank account number</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }

        if (!isValidPassword(password1) || !isValidPassword(password2)) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Password must have at least 1 digit and 1 capital letter</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            password1Field.getText().clear();
            password2Field.getText().clear();
            return;
        }
        if (!password1.equals(password2)) {
            Toast toast = Toast.makeText(getBaseContext(),
                    Html.fromHtml("<b>Passwords must match</b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            password1Field.getText().clear();
            password2Field.getText().clear();
            return;
        }

        //If passwords match, store data in firebase
        if (password1.equals(password2)) {
            mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(VendorRegistration.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("sucess", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        mDatabase.child("userType").child(uid).setValue("vendor");

                        Vendor c = new Vendor(name, phone, email, location, cuisine, bankAccountNumber, hours);
                        mDatabase.child("vendorInfo").child(uid).setValue(c);

                        //switch activities
                        Intent home = new Intent(getApplicationContext(), CustomerHomeActivity.class);
                        home.putExtra("Type","vendor");
                        startActivity(home);

                    } else {
                        // If sign in fails, display a message to the user.
                        String error = task.getException().getMessage();
                        Log.w("fail", "signInWithEmail:failure", task.getException());
                        Toast toast = Toast.makeText(getBaseContext(), Html.fromHtml("<b>" + error + "</b>"), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                        toast.show();
                        
                    }
                }
            });
        }
    }


    @SuppressLint("NewApi")
    //A valid string is one without numbers
    public boolean isValidString(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isAlphabetic(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //Make sure there are no letters
    public boolean isValidNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) >= '0' && s.charAt(i) <= '9')) {
                return false;
            }
        }
        return true;
    }

    //We define those passwords that have at least 1 capital letter and 1 numeric digit as valid
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
