package com.cosc3p97project.busync.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cosc3p97project.busync.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        // Get the user ID of the user whose profile is being viewed
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        // Initialize the UI elements
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);

        Current_State = "new";

        RetrieveUserInfo();
    }

    // Add the onOptionsItemSelected method to handle the back button in the toolbar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Add the RetrieveUserInfo method to fetch the user's details from the database.
    private void RetrieveUserInfo() {
        ValueEventListener userListener = new ValueEventListener() {
            // onDataChange is called when the data is first read and again whenever the data changes.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())  &&  (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userstatus);
                    ManageChatRequests();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userstatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        };

        UserRef.child(receiverUserID).addValueEventListener(userListener);
    }

    // Add the ManageChatRequests method to handle chat requests.
    private void ManageChatRequests(){
        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
              if(dataSnapshot.hasChild(receiverUserID)){
                  String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                  if(request_type.equals("sent")){
                      Current_State = "request_sent";
                      SendMessageRequestButton.setText("Cancel Chat Request");
                  }
                  else if(request_type.equals("received")){
                      Current_State = "request_received";
                      SendMessageRequestButton.setText("Accept Chat Request");

                      DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                      DeclineMessageRequestButton.setEnabled(true);

                      DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              CancelChatRequest();
                          }
                      });
                  }
              }
              else{
                  ContactsRef.child(senderUserID).addValueEventListener(new ValueEventListener(){
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                          if(dataSnapshot.hasChild(receiverUserID)){
                              Current_State = "friends";
                              SendMessageRequestButton.setText("Remove This Contact");
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });
              }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
       if(!senderUserID.equals(receiverUserID)){
           SendMessageRequestButton.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view){
                   SendMessageRequestButton.setEnabled(false);
                   if(Current_State.equals("new")){
                       SendChatRequest();
                   }
                   if(Current_State.equals("request_send")){
                       CancelChatRequest();
                   }
                   if(Current_State.equals("request_received")){
                       AcceptChatRequest();
                   }
                   if(Current_State.equals("friends")){
                       RemoveSpecificContact();
                   }

               }
           });
       }

    }

    // Add the RemoveSpecificContact method to remove a specific contact.
    private void RemoveSpecificContact(){
        ContactsRef.child(senderUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task){
                if(task.isSuccessful()){
                    ContactsRef.child(receiverUserID).child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>(){
                        @Override
                        public void onComplete(@NonNull Task<Void> task){
                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                SendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    // Add the AcceptChatRequest method to accept a chat request.
    private void AcceptChatRequest() {
        DatabaseReference senderRef = ContactsRef.child(senderUserID).child(receiverUserID);
        DatabaseReference receiverRef = ContactsRef.child(receiverUserID).child(senderUserID);

        // Fetch receiver details and set them under sender's contacts
        UserRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot receiverSnapshot) {
                if (receiverSnapshot.exists()) {
                    String receiverName = receiverSnapshot.child("name").getValue(String.class);
                    String receiverStatus = receiverSnapshot.child("status").getValue(String.class);

                    HashMap<String, Object> receiverDetails = new HashMap<>();
                    receiverDetails.put("name", receiverName);
                    receiverDetails.put("status", receiverStatus);

                    Log.d("ProfileActivity", "Setting receiver details under sender: " + receiverDetails);
                    senderRef.setValue(receiverDetails).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            fetchAndSetSenderDetails(receiverRef);
                        }
                    });
                } else {
                    Log.e("ProfileActivity", "Receiver data not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ProfileActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Add the fetchAndSetSenderDetails method to fetch sender details and set them under receiver's contacts.
    private void fetchAndSetSenderDetails(DatabaseReference receiverRef) {
        UserRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot senderSnapshot) {
                if (senderSnapshot.exists()) {
                    String senderName = senderSnapshot.child("name").getValue(String.class);
                    String senderStatus = senderSnapshot.child("status").getValue(String.class);

                    HashMap<String, Object> senderDetails = new HashMap<>();
                    senderDetails.put("name", senderName);
                    senderDetails.put("status", senderStatus);

                    Log.d("ProfileActivity", "Setting sender details under receiver: " + senderDetails);
                    receiverRef.setValue(senderDetails).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("ProfileActivity", "Contacts updated successfully for both users.");
                            removeChatRequests(senderUserID, receiverUserID);
                        }
                    });
                } else {
                    Log.e("ProfileActivity", "Sender data not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ProfileActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Add the removeChatRequests method to remove chat requests after they have been accepted.
    private void removeChatRequests(String senderId, String receiverId) {
        ChatRequestRef.child(senderId).child(receiverId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRequestRef.child(receiverId).child(senderId).removeValue().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        SendMessageRequestButton.setEnabled(true);
                        Current_State = "friends";
                        SendMessageRequestButton.setText("Remove This Contact");
                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                        DeclineMessageRequestButton.setEnabled(false);
                    }
                });
            }
        });
    }

    // Add the SendChatRequest method to send a chat request.
    private void SendChatRequest(){
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task){
                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>(){
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task){
                                            if (task.isSuccessful()){
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task){
                                                                if (task.isSuccessful()){
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // Add the CancelChatRequest method to cancel a chat request.
    private void CancelChatRequest(){

        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task){
                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task){
                                            if (task.isSuccessful()){
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
