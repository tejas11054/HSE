package com.example.hse;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private LinearLayout mLinearLayoutContent;
    private String lastTimestamp = null; // Variable to store the last fetched timestamp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the LinearLayout for dynamic content
        mLinearLayoutContent = findViewById(R.id.linearLayoutContent);

        // Check and request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
        }

        // Reference to the Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("alerts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if data exists
                        if (dataSnapshot.exists()) {
                            Log.d("FirebaseData", "Data exists");

                            // Loop through each child in the "alerts" node
                            for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                                String title = alertSnapshot.child("title").getValue(String.class);
                                String body = alertSnapshot.child("body").getValue(String.class);
                                String imageurl = alertSnapshot.child("image_url").getValue(String.class);
                                String timestamp = alertSnapshot.child("timestamp").getValue(String.class);

                                // Skip alerts with null timestamp or already processed ones
                                if (timestamp == null || (lastTimestamp != null && timestamp.compareTo(lastTimestamp) <= 0)) {
                                    Log.d("FirebaseData", "Skipping old or invalid alert: " + title);
                                    continue;
                                }

                                // Update the lastTimestamp with the latest one
                                lastTimestamp = timestamp;

                                // Log the values for debugging
                                Log.d("FirebaseData", "Title: " + title);
                                Log.d("FirebaseData", "Body: " + body);
                                Log.d("FirebaseData", "ImageUrl: " + imageurl);
                                Log.d("FirebaseData", "Timestamp: " + timestamp);

                                // Create a CardView for each alert
                                CardView cardView = new CardView(MainActivity.this);
                                CardView.LayoutParams cardParams = new CardView.LayoutParams(
                                        CardView.LayoutParams.MATCH_PARENT,
                                        CardView.LayoutParams.WRAP_CONTENT
                                );
                                cardParams.setMargins(0, 0, 0, 16);
                                cardView.setLayoutParams(cardParams);
                                cardView.setRadius(10);
                                cardView.setCardElevation(8);
                                cardView.setBackgroundColor(getResources().getColor(R.color.card_background)); // Light grey background
                                cardView.setContentPadding(16, 16, 16, 16);

                                // Create a LinearLayout to hold the content inside the CardView
                                LinearLayout cardContentLayout = new LinearLayout(MainActivity.this);
                                cardContentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                cardContentLayout.setOrientation(LinearLayout.VERTICAL);

                                // Create TextViews for title, body, and timestamp
                                TextView textViewTitle = new TextView(MainActivity.this);
                                textViewTitle.setText(title);
                                textViewTitle.setTextSize(20);
                                textViewTitle.setTextColor(getResources().getColor(R.color.label_color));
                                textViewTitle.setGravity(Gravity.START);
                                textViewTitle.setPadding(0, 0, 0, 8);

                                TextView textViewBody = new TextView(MainActivity.this);
                                textViewBody.setText("Missing Safety Equipments:\n" + body);
                                textViewBody.setTextSize(16);
                                textViewBody.setTextColor(getResources().getColor(R.color.body_color));
                                textViewBody.setGravity(Gravity.START);
                                textViewBody.setPadding(0, 0, 0, 8);

                                ImageView imageView = new ImageView(MainActivity.this);
                                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                imageParams.setMargins(0, 0, 0, 8);
                                imageView.setLayoutParams(imageParams);

                                TextView textViewTimestamp = new TextView(MainActivity.this);
                                textViewTimestamp.setText("Timestamp: " + timestamp);
                                textViewTimestamp.setTextSize(14);
                                textViewTimestamp.setTextColor(getResources().getColor(R.color.timestamp_color));
                                textViewTimestamp.setGravity(Gravity.END);

                                // Load the image using Glide
                                Glide.with(MainActivity.this)
                                        .load(imageurl)
                                        .into(imageView);

                                // Add TextViews and ImageView to the LinearLayout
                                cardContentLayout.addView(textViewTitle);
                                cardContentLayout.addView(textViewBody);
                                cardContentLayout.addView(imageView);
                                cardContentLayout.addView(textViewTimestamp);

                                // Add LinearLayout to the CardView
                                cardView.addView(cardContentLayout);

                                // Add CardView to the main LinearLayout
                                mLinearLayoutContent.addView(cardView);

                                // Send SMS with alert details
                                sendSMS("7710991549", "Alert: " + title + "\n" + body + "\nTimestamp: " + timestamp);
                            }
                        } else {
                            Log.d("FirebaseData", "No data found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseData", "Failed to read value.", error.toException());
                    }
                });
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent Successfully!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("SMS", "Failed to send SMS", e);
            Toast.makeText(getApplicationContext(), "SMS Failed to Send!", Toast.LENGTH_LONG).show();
        }
    }
}
