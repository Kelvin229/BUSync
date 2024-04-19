package com.cosc3p97project.busync.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cosc3p97project.busync.model.Contacts;
import com.cosc3p97project.busync.view.LoginActivity;
import com.cosc3p97project.busync.R;
import com.cosc3p97project.busync.view.ChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID="";

    // Adding the onCreateView method to inflate the fragment layout.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Redirect to Login Activity if no user is logged in
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        } else {
            currentUserID = currentUser.getUid();
            ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            setupRecyclerView();
            if (mAuth.getCurrentUser() != null) { // Only set up the adapter if the user is logged in
                setupAdapter();
            }
        }
        return PrivateChatsView;
    }

    // Adding the setupRecyclerView method to initialize the RecyclerView.
    private void setupRecyclerView() {
        chatsList = PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // Adding the onStart method to populate the RecyclerView with user data.
    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) { // Only set up the adapter if the user is logged in
            setupAdapter();
        }
    }

    // Adding the setupAdapter method to populate the RecyclerView with user data.
    private void setupAdapter() {
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String usersIDs = getRef(position).getKey();
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                String profileImage = dataSnapshot.child("image").getValue(String.class);
                                Picasso.get().load(profileImage).into(holder.profileImage);
                            }
                            holder.userName.setText(dataSnapshot.child("name").getValue(String.class));
                            holder.userStatus.setText(dataSnapshot.child("status").getValue(String.class));
                            holder.itemView.setOnClickListener(view -> {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_id", usersIDs);
                                startActivity(chatIntent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                return new ChatsViewHolder(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    // Creating a ChatsViewHolder class to hold the user data.
    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName, userStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}
