package com.cosc3p97project.busync;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.cosc3p97project.busync.controller.TabsAccessorAdapter;
import com.cosc3p97project.busync.view.FindFriendsActivity;
import com.cosc3p97project.busync.view.LoginActivity;
import com.cosc3p97project.busync.view.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("BrockSync");


        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            currentUserID = currentUser.getUid();
            VerifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUserID != null) {
            updateUserStatus("offline");
        }
    }

    // Add the onDestroy method to update the user's status.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUserID != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        if (currentUserID == null) {
            Log.e("MainActivity", "currentUserID is null in VerifyUserExistence.");
            return;
        }
        RootRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("name")) {
                    SendUserToSettingsActivity();
                } else {
                    Toast.makeText(MainActivity.this, "Welcome " + dataSnapshot.child("name").getValue(String.class), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Database error in VerifyUserExistence: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    // On menu item selection, the following functions activates conditionally.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_logout_option) {
            if (currentUserID != null) {
                updateUserStatus("offline"); // updates user status to offline.
            }
            mAuth.signOut(); // signs out the user.
            SendUserToLoginActivity(); // sends the user back to login activity.
            return true;
        } else if (id == R.id.main_settings_option) {
            SendUserToSettingsActivity(); // sends the user to settings activity
            return true;
        } else if (id == R.id.main_find_friends_option) {
            SendUserToFindFriendsActivity(); // sends the user to find friends activity intent.
            return true;
        } else if (id == R.id.main_create_group_option) {
            RequestNewGroup();// creates a new group. pops up a window to put a name on it.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // creates a new group with just a name.
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g., Coding Wizards");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString().trim();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Please write the group name.", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Creates a new group.
    private void CreateNewGroup(final String groupName) {
        // Gets the current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        String saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        String saveCurrentTime = currentTime.format(calendar.getTime());

        // Creating a map to hold the initial message
        Map<String, Object> initialMessage = new HashMap<>();
        initialMessage.put("message", "Welcome to the " + groupName + " group!");
        initialMessage.put("type", "text");
        initialMessage.put("from", currentUserID);
        initialMessage.put("time", saveCurrentTime);
        initialMessage.put("date", saveCurrentDate);

        RootRef.child("Groups").child(groupName).setValue(initialMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, groupName + " group created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Sends user to login activity intent
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    // sends user to Settings Activity intent
    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    // sends user to find friends activity intent
    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    // updates user status.
    private void updateUserStatus(String state) {
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", new SimpleDateFormat("hh:mm a").format(Calendar.getInstance().getTime()));
        onlineStateMap.put("date", new SimpleDateFormat("MMM dd, yyyy").format(Calendar.getInstance().getTime()));
        onlineStateMap.put("state", state);

        RootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);
    }
}
