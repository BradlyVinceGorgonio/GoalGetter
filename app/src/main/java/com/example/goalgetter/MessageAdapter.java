package com.example.goalgetter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.userName.setText(message.getSender());

        holder.messageText.setText(message.getText());

        holder.profileImage.setImageResource(R.drawable.profileic);

        if (message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.itemView.setBackgroundResource(R.drawable.my_message_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.other_message_background);
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView messageText;
        ImageView profileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
