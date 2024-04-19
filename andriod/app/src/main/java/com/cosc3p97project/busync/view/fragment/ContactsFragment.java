package com.cosc3p97project.busync.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class ContactsFragment extends Fragment {
    private View ContactsView;
    private RecyclerView myContactsList;

    private DatabaseReference ContactsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating the layout for Contacts Fragment.
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        // initializing recycler view for displaying contacts.
        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // initializing firebase instances and current user with id.
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserID = mAuth.getCurrentUser().getUid();
            ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        }

        return ContactsView;
    }

    // Checking if the user is logged in or not.
    @Override
    public void onStart() {
        super.onStart();
        if (currentUserID != null) {
            setupAdapter();
        } else {
            Toast.makeText(getActivity(), "You must be logged in to view this page.", Toast.LENGTH_SHORT).show();
        }
    }

    // Setting up the adapter, using firebase recycler option
    private void setupAdapter() {
        // Initializing firebase recycler options to configure the query
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsRef, Contacts.class)
                        .build();

        // Populating recyclerview with contacts information.
        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();

                // handles data changes for each user
                // displays user name, status, profile image, and online status.
                UsersRef.child(userIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")) {
                                String name = dataSnapshot.child("name").getValue(String.class);
                                String status = dataSnapshot.child("status").getValue(String.class);
                                holder.userName.setText(name);
                                holder.userStatus.setText(status);
                            }
                            if (dataSnapshot.hasChild("image")) {
                                String image = dataSnapshot.child("image").getValue(String.class);
                                Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue(String.class);
                                holder.onlineIcon.setVisibility(state.equals("online") ? View.VISIBLE : View.INVISIBLE);
                            } else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    // handles errors.
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Creates and returns a new view holder for each of the contact item.
            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                // Inflates the layout with new layout "users_display_layout" item.
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                return new ContactsViewHolder(view);
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    // Creating and returning a new view holder for each of the contact item.
    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
