package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//import org.jetbrains.annotations.NotNull;


public class ClientRegistrationPage extends AppCompatActivity {

    Button create;
    String email;
    String password1;
    String password2;
    String phone;

    //view for email entry textbox
    EditText emailView;
    //view for phone entry textbox
    EditText phoneView;
    //view for password entry 1 textbox
    EditText password1View;
    //view for password entry 2 textbox
    EditText password2View;

    //firebase reference
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //Intent home;


    /**
     * @param savedInstanceState
     * Displays details once the page is created
     */
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_registration_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        create = (Button) findViewById(R.id.create);
        emailView = findViewById(R.id.emailInput);
        phoneView = findViewById(R.id.phoneInput);
        password1View = findViewById(R.id.passwordFirst);
        password2View = findViewById(R.id.passwordSecond);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
         * handles the login button being clicked and sends toasts accordingly if
         * fields are left empty whilst trying to create an account
         */
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailView.getText().toString();
                phone = phoneView.getText().toString();
                password1 = password1View.getText().toString();
                password2 = password2View.getText().toString();
                //improve if statements and make password more secure

                //user tries to login without enter a password
                if (password1.equals("") || email.equals("") || phone.equals("") || password2.equals("")) {
                    //set toast to display that the user should make a password
                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Please fill out all fields!</b>"), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    return;
                }

                if (password1.equals(password2) && !email.equals("") && !phone.equals("")) {
                    //if passwords match characters and meet criteria set for valid password, add to DB
                    if (isValidPassword(password1)) {
                        //Customer newCustomer = new Customer();
                        //add account to firebase
                        mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(ClientRegistrationPage.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("sucess", "signInWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            String uid = user.getUid();
                                            mDatabase.child("userType").child(uid).setValue("customer");

                                            //TODO: get user name (first and last) when they make acct and store here instead of hardcoded vals
                                            String firstName = "Cristina";
                                            String lastName = "Leon";
                                            Customer c = new Customer(firstName, lastName, phone);
                                            mDatabase.child("customerInfo").child(uid).setValue(c);


                                            //switch activities
                                            Intent home = new Intent(getApplicationContext(), CustomerHomeActivity.class);
                                            home.putExtra("Type","customer");
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
                    //case where password is too weak for contraints
                    //flash message on how to adjust password
                    else {
                        Toast toast = Toast.makeText(getBaseContext(), Html.fromHtml
                                ("<b>Password must have at least 1 digit and 1 capital letter</b>"), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                        toast.show();
                    }
                    return;
                }
                //non matching non null passwords
                if (!password1.equals(password2)) {
                    Toast toast = Toast.makeText(getBaseContext(),
                            Html.fromHtml("<b>Passwords must match</b>"), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    return;
                }

            }
        });



    }


    /** returns true iff password is valid. ie. the one capital
     * letter, one number constraint is met
     * @param password
     * @return whether or not the password has one cap letter and one number
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
