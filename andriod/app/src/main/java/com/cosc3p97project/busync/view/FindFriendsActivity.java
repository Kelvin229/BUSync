package com.cosc3p97project.busync.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.cosc3p97project.busync.model.Contacts;
import com.cosc3p97project.busync.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        // gets a child reference from the database.
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // enables UI tool bar.
        mToolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        setupFirebaseRecyclerAdapter();
    }

    // tool bar navigation
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // configuring firebase recycler adapter options.
    private void setupFirebaseRecyclerAdapter() {
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            // In FindFriendsActivity, update the onBindViewHolder for the RecyclerView adapter.
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                final String visit_user_id = getRef(position).getKey();
                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (visit_user_id.equals(currentUserID)) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    // updates profile name, status, and images.
                    holder.userName.setText(model.getName());
                    holder.userStatus.setText(model.getStatus());
                    Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                    // handles item click for user profile.
                    holder.itemView.setOnClickListener(view -> {
                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    });
                }
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new FindFriendViewHolder(view);
            }
        };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    // Initilization of components.
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
