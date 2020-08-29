package com.example.cristinaleon.foodtrucks;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;

/**
 * Created by vikramkhemlani on 4/11/18.
 */

public class ReviewUsers extends AppCompatActivity {
    double rating;
    String numberOfRates;
    String userID;
    String currentRating;
    boolean exists;
    int commentNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_users);
        ImageView imv = findViewById(R.id.imageView8);
        exists = false;
        rating = 0.0;
        commentNumber = 1;
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8};
                highlight(stars);
                int[] unstars = {9, 10, 11, 12};
                unhighlight(unstars);
                rating = 1;
            }
        });
        imv = findViewById(R.id.imageView9);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9};
                highlight(stars);
                int[] unstars = {10, 11, 12};
                unhighlight(unstars);
                rating = 2;
            }
        });
        imv = findViewById(R.id.imageView10);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9, 10};
                highlight(stars);
                int[] unstars = {11, 12};
                unhighlight(unstars);
                rating = 3;
            }
        });
        imv = findViewById(R.id.imageView11);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9, 10, 11};
                highlight(stars);
                int[] unstars = {12};
                unhighlight(unstars);
                rating = 4;
            }
        });
        imv = findViewById(R.id.imageView12);
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] stars = {8, 9, 10, 11, 12};
                highlight(stars);
                rating = 5;
            }
        });
        Intent i = getIntent();
        userID = i.getStringExtra("userID");
        Button submit = findViewById(R.id.button3);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submit(v);
            }
        });
    }


    public void highlight(int[] stars) {
        for (int i : stars) {
            switch (i) {
                case 8:
                    ImageView imv = findViewById(R.id.imageView8);
                    imv.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 9:
                    ImageView imv1 = findViewById(R.id.imageView9);
                    imv1.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 10:
                    ImageView imv2 = findViewById(R.id.imageView10);
                    imv2.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 11:
                    ImageView imv3 = findViewById(R.id.imageView11);
                    imv3.setImageResource(android.R.drawable.star_big_on);
                    break;
                case 12:
                    ImageView imv4 = findViewById(R.id.imageView12);
                    imv4.setImageResource(android.R.drawable.star_big_on);
                    break;

            }
        }
    }

    public void unhighlight(int[] stars) {
        for (int i : stars) {
            switch (i) {
                case 8:
                    ImageView imv = findViewById(R.id.imageView8);
                    imv.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 9:
                    ImageView imv1 = findViewById(R.id.imageView9);
                    imv1.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 10:
                    ImageView imv2 = findViewById(R.id.imageView10);
                    imv2.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 11:
                    ImageView imv3 = findViewById(R.id.imageView11);
                    imv3.setImageResource(android.R.drawable.star_big_off);
                    break;
                case 12:
                    ImageView imv4 = findViewById(R.id.imageView12);
                    imv4.setImageResource(android.R.drawable.star_big_off);
                    break;

            }
        }
    }

    public void submit(View view) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child("reviewUsers");
        if (rating == 0.0) {
            Toast toast =
                    Toast.makeText(getBaseContext(),
                            Html.fromHtml("Please click a star to rate the User"),
                            Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
            return;
        }
        //Access the firebase
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(userID)) {
                        exists = true;
                        Iterable<DataSnapshot> information = child.getChildren();
                        for (DataSnapshot info : information) {
                            if (info.getKey().equals("rating")) {
                                currentRating = info.getValue().toString();
                            }
                            if (info.getKey().equals("numberOfRates")) {
                                numberOfRates = info.getValue().toString();
                            }
                            if (info.getKey().equals("comments")) {
                                Iterable<DataSnapshot> allComments = info.getChildren();
                                for (DataSnapshot comment : allComments) {
                                    commentNumber++;
                                }
                            }
                        }
                        Double dCurrentRating = Double.parseDouble(currentRating);
                        int iNumberOfRates = Integer.parseInt(numberOfRates);
                        double newRating  = (dCurrentRating * iNumberOfRates);
                        iNumberOfRates += 1;
                        newRating = (newRating + rating)/iNumberOfRates;
                        String snewRating = String.valueOf(newRating);
                        EditText editText = findViewById(R.id.editText10);
                        String comment = editText.getText().toString();
                        numberOfRates = String.valueOf(iNumberOfRates);
                        mDatabase.child("reviewUsers").child(userID).child("rating").setValue(snewRating);
                        mDatabase.child("reviewUsers").child(userID).child("numberOfRates").setValue(numberOfRates);
                        mDatabase.child("reviewUsers").child(userID).child("comments").child("c_" + commentNumber)
                                .setValue(comment);
                    }
                }
                if (!exists) {
                    mDatabase.child("reviewUsers").child(userID).child("rating").setValue(String.valueOf(rating));
                    mDatabase.child("reviewUsers").child(userID).child("numberOfRates").setValue("1");
                    EditText editText = findViewById(R.id.editText10);
                    String comment = editText.getText().toString();
                    mDatabase.child("reviewUsers").child(userID).child("comments").child("c_" + 1)
                            .setValue(comment);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        finish();
    }
}
