package com.cosc3p97project.busync.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosc3p97project.busync.model.Contacts;
import com.cosc3p97project.busync.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference UsersRef, ChatRequestsRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        initializeFirebase();
        setupRecyclerView();

        return RequestsFragmentView;
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    private void setupRecyclerView() {
        myRequestsList = RequestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef.child(currentUserID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = createAdapter(options);
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    private FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> createAdapter(FirebaseRecyclerOptions<Contacts> options) {
        return new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model) {
                String listUserId = getRef(position).getKey();
                configureUserDisplay(holder, listUserId);
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new RequestsViewHolder(view);
            }
        };
    }

    private void configureUserDisplay(RequestsViewHolder holder, String userId) {
        ChatRequestsRef.child(currentUserID).child(userId).child("request_type")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue(String.class);
                            if ("received".equals(type)) {
                                handleReceivedRequest(holder, userId);
                            } else if ("sent".equals(type)) {
                                handleSentRequest(holder, userId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleReceivedRequest(RequestsViewHolder holder, String userId) {
        UsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("name").getValue(String.class);
                String userStatus = dataSnapshot.child("status").getValue(String.class);
                String imageUrl = dataSnapshot.hasChild("image") ? dataSnapshot.child("image").getValue(String.class) : null;

                if (imageUrl != null) {
                    Picasso.get().load(imageUrl).into(holder.profileImage);
                }

                holder.userName.setText(userName);
                holder.userStatus.setText("wants to connect with you.");
                setupInteractionButtons(holder, userId, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSentRequest(RequestsViewHolder holder, String userId) {
        UsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("name").getValue(String.class);
                String userStatus = dataSnapshot.child("status").getValue(String.class);
                String imageUrl = dataSnapshot.hasChild("image") ? dataSnapshot.child("image").getValue(String.class) : null;

                if (imageUrl != null) {
                    Picasso.get().load(imageUrl).into(holder.profileImage);
                }

                holder.userName.setText(userName);
                holder.userStatus.setText("you have sent a request to " + userName);
                setupInteractionButtons(holder, userId, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInteractionButtons(RequestsViewHolder holder, String userId, boolean isReceived) {
        if (isReceived) {
            holder.AcceptButton.setVisibility(View.VISIBLE);
            holder.CancelButton.setVisibility(View.VISIBLE);
            holder.AcceptButton.setOnClickListener(v -> acceptRequest(userId));
            holder.CancelButton.setOnClickListener(v -> cancelRequest(userId));
        } else {
            holder.AcceptButton.setVisibility(View.GONE);
            holder.CancelButton.setVisibility(View.VISIBLE);
            holder.CancelButton.setOnClickListener(v -> cancelSentRequest(userId));
        }
    }

    private void acceptRequest(String userId) {
        UsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    String userStatus = dataSnapshot.child("status").getValue(String.class);
                    String imageUrl = dataSnapshot.hasChild("image") ? dataSnapshot.child("image").getValue(String.class) : null;

                    Contacts contact = new Contacts();
                    contact.setName(userName);
                    contact.setStatus(userStatus);
                    contact.setImage(imageUrl);
                    contact.setRequestType("Accepted");

                    ContactsRef.child(currentUserID).child(userId).child(currentUserID)
                            .setValue(contact)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Remove the request from the receiver's Chat Requests node
                                    ChatRequestsRef.child(currentUserID).child(userId).removeValue()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    // Remove the request from the sender's Chat Requests node
                                                    ChatRequestsRef.child(userId).child(currentUserID).removeValue()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    // Add the current user to the contact's Contacts node
                                                                    UsersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                            if (dataSnapshot.exists()) {
                                                                                String currentUserName = dataSnapshot.child("name").getValue(String.class);
                                                                                String currentUserStatus = dataSnapshot.child("status").getValue(String.class);
                                                                                String currentUserImage = dataSnapshot.hasChild("image") ? dataSnapshot.child("image").getValue(String.class) : null;

                                                                                Contacts currentUserContact = new Contacts();
                                                                                currentUserContact.setName(currentUserName);
                                                                                currentUserContact.setStatus(currentUserStatus);
                                                                                currentUserContact.setImage(currentUserImage);
                                                                                currentUserContact.setRequestType("Accepted");

                                                                                ContactsRef.child(userId).child(currentUserID).child(userId)
                                                                                        .setValue(currentUserContact)
                                                                                        .addOnCompleteListener(task3 -> {
                                                                                            if (task3.isSuccessful()) {
                                                                                                Toast.makeText(getContext(), "Request Accepted", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRequest(String userId) {
        ChatRequestsRef.child(currentUserID).child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelSentRequest(String userId) {
        ChatRequestsRef.child(currentUserID).child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Sent Request Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
