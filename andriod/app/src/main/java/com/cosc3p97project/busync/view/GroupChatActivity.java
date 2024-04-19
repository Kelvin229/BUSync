package com.cosc3p97project.busync.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cosc3p97project.busync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import android.view.LayoutInflater;
import android.widget.LinearLayout;


public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages, messageSenderName, messageContent, messageTime;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef;
    private String currentGroupName, currentUserID, currentUserName;
    private ChildEventListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // gets the group name from the intent.
        currentGroupName = getIntent().getExtras().get("groupName").toString();

        // Firebase authentication instance.
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid(); // gets current user based on user id.
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // reference to Users node in the db.

        InitializeFields(); // initializes component fields.
        GetUserInfo(); // gets user info.
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupMessageSendingAndListening();
    }
    private void setupMessageSendingAndListening() {
        if (GroupNameRef != null) {
            attachChildEventListener(); // listener for group chat.
        }
    }

    private void GetUserInfo() {

        // retrieves current users information
        UsersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                    currentUserName = dataSnapshot.child("name").getValue(String.class);
                    GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName); // fetches groups node from db, enables access to child nodes as well.
                    setupMessageSendingAndListening();
                } else {
                    Toast.makeText(GroupChatActivity.this, "User name not found.", Toast.LENGTH_SHORT).show();
                }
            }

            // throws error.
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GroupChatActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Event listener
    private void attachChildEventListener() {
        if (messageListener == null) {
            messageListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    DisplayMessages(dataSnapshot);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(GroupChatActivity.this, "Failed to load messages: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            GroupNameRef.addChildEventListener(messageListener);
        }
    }

    // Displays messages of group chat.
    private void DisplayMessages(DataSnapshot dataSnapshot) {
        LayoutInflater inflater = LayoutInflater.from(this);

        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
//            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");

            View messageView = inflater.inflate(R.layout.custom_group_chat_layout, null);

            messageSenderName = messageView.findViewById(R.id.message_sender_name);
            messageContent = messageView.findViewById(R.id.message_content);
            messageTime = messageView.findViewById(R.id.message_time);

            // sets the username, message content, and time to the components field.
            messageSenderName.setText(chatName);
            messageContent.setText(chatMessage);
            messageTime.setText(chatTime + " " + chatDate);

            LinearLayout messageContainer = findViewById(R.id.message_container);
            messageContainer.addView(messageView);
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (GroupNameRef != null && messageListener != null) {
            GroupNameRef.removeEventListener(messageListener);
            messageListener = null;
        }
    }

    // UI Component initializations.
    private void InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentGroupName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.my_scroll_view);
//        displayTextMessages = findViewById(R.id.group_chat_text_display);
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This handles the back operation
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Saves messages information to the database.
    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageKey = GroupNameRef.push().getKey();
        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currentUserName);
        messageInfoMap.put("message", message);
        messageInfoMap.put("date", new SimpleDateFormat("MMM dd, yyyy").format(Calendar.getInstance().getTime()));
        messageInfoMap.put("time", new SimpleDateFormat("hh:mm a").format(Calendar.getInstance().getTime()));
        if (messageKey != null) {
            GroupNameRef.child(messageKey).updateChildren(messageInfoMap);
        }
    }
}
