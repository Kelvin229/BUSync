package com.cosc3p97project.busync.controller;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cosc3p97project.busync.R;
import com.cosc3p97project.busync.model.Messages;
import com.cosc3p97project.busync.view.ImageViewerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    // Add the MessageAdapter constructor to initialize the fields.
    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    // Add the MessageViewHolder class to define the view holder for the RecyclerView.
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        // Add the MessageViewHolder constructor to initialize the fields.
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    // Add the onCreateViewHolder method to inflate the layout for the view holder.
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int position) {
        String messageSenderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log or handle the cancellation
            }
        });

        // Initial visibility setup
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);

        // Set message details based on type
        if (fromMessageType.equals("text")) {
            setupTextMessage(fromUserID, messageSenderId, messages, messageViewHolder);
        } else if (fromMessageType.equals("image")) {
            setupImageMessage(fromUserID, messageSenderId, messages, messageViewHolder);
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            setupDocumentMessage(fromUserID, messageSenderId, messages, messageViewHolder);
        }

        // Click listeners for message types
        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = messageViewHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Messages currentMessage = userMessagesList.get(adapterPosition);
                    handleItemClick(currentMessage, messageViewHolder);
                }
            }
        });
    }

    // Add the setupTextMessage method to display text messages.
    private void setupTextMessage(String fromUserID, String senderID, Messages messages, MessageViewHolder holder) {
        if (fromUserID.equals(senderID)) {
            holder.senderMessageText.setVisibility(View.VISIBLE);
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
            holder.senderMessageText.setTextColor(Color.BLACK);
            holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
        } else {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
            holder.receiverMessageText.setTextColor(Color.BLACK);
            holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
        }
    }

    // Add the setupImageMessage method to display image messages.
    private void setupImageMessage(String fromUserID, String senderID, Messages messages, MessageViewHolder holder) {
        ImageView targetView = fromUserID.equals(senderID) ? holder.messageSenderPicture : holder.messageReceiverPicture;
        targetView.setVisibility(View.VISIBLE);
        Picasso.get().load(messages.getMessage()).into(targetView);
    }

    // Add the setupDocumentMessage method to display document messages.
    private void setupDocumentMessage(String fromUserID, String senderID, Messages messages, MessageViewHolder holder) {
        ImageView targetView = fromUserID.equals(senderID) ? holder.messageSenderPicture : holder.messageReceiverPicture;
        targetView.setVisibility(View.VISIBLE);
        targetView.setBackgroundResource(R.drawable.file);
    }

    // Add the handleItemClick method to handle item clicks.
    private void handleItemClick(Messages message, MessageViewHolder holder) {
        switch (message.getType()) {
            case "pdf", "docx" -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMessage()));
                holder.itemView.getContext().startActivity(intent);
            }
            case "image" -> showImageOptions(message, holder);
            case "text" -> showTextOptions(message, holder);
            default -> showGeneralOptions(message, holder);
        }
    }

    private void showImageOptions(Messages message, MessageViewHolder holder) {
        CharSequence[] options = new CharSequence[]{"View This Image", "Delete Options", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Image Options");
        builder.setItems(options, (dialogInterface, i) -> {
            switch (i) {
                case 0 -> { // View
                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                    intent.putExtra("url", message.getMessage());
                    holder.itemView.getContext().startActivity(intent);
                }
                case 1 -> // Delete Options
                        showDeleteOptions(message, holder);
                default -> // Cancel
                        dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showTextOptions(Messages message, MessageViewHolder holder) {
        CharSequence[] options = new CharSequence[]{"Copy Text", "Delete Options", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Text Options");
        builder.setItems(options, (dialogInterface, i) -> {
            switch (i) {
                case 0 -> { // Copy
                    ClipboardManager clipboard = (ClipboardManager) holder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("copied text", message.getMessage());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(holder.itemView.getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                }
                case 1 -> // Delete Options
                        showDeleteOptions(message, holder);
                default -> // Cancel
                        dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showGeneralOptions(Messages message, MessageViewHolder holder) {
        CharSequence[] options = new CharSequence[]{"Delete Options", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Choose an option");
        builder.setItems(options, (dialogInterface, i) -> {
            if (i == 0) {
                showDeleteOptions(message, holder);
            } else {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showDeleteOptions(Messages message, MessageViewHolder holder) {
        CharSequence[] deleteOptions = new CharSequence[]{"Delete for Me", "Delete for Other", "Delete for Everyone", "Cancel"};
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(holder.itemView.getContext());
        deleteBuilder.setTitle("Delete Message");
        deleteBuilder.setItems(deleteOptions, (dialogInterface, which) -> {
            int position = holder.getAdapterPosition();
            switch (which) {
                case 0 -> deleteSentMessage(position, holder);
                case 1 -> deleteReceiverMessage(position, holder);
                case 2 -> deleteMessageForEveryOne(position, holder);
                default -> dialogInterface.dismiss();
            }
        });
        deleteBuilder.show();
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    // Delete message sent by the sender only
    private void deleteSentMessage(final int position , MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Messages message = userMessagesList.get(position);
        rootRef.child("Messages")
                .child(message.getFrom())
                .child(message.getTo())
                .child(message.getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            userMessagesList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(holder.itemView.getContext(), "Deleted your message Successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), " Sender Error Occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Delete message received by the receiver only
    private void deleteReceiverMessage(final int position , MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Messages message = userMessagesList.get(position);
        rootRef.child("Messages")
                .child(message.getTo())
                .child(message.getFrom())
                .child(message.getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            userMessagesList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(holder.itemView.getContext(), "Deleted receiver message Successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Receiver Error Occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Delete message for everyone involved
    private void deleteMessageForEveryOne(final int position , MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Messages message = userMessagesList.get(position);
        DatabaseReference messageRef = rootRef.child("Messages")
                .child(message.getFrom())
                .child(message.getTo())
                .child(message.getMessageID());

        messageRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(holder.itemView.getContext(), "Deleted for Everyone Successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Everyone Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
