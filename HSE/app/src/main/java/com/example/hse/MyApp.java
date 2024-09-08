package com.example.hse;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

