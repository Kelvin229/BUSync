package com.cosc3p97project.busync;

import android.app.Application;

import com.google.firebase.BuildConfig;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // Firestore
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);

            // Firebase Auth
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);

            // Realtime Database
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000);

            // Firebase Storage
            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
        }
    }
}