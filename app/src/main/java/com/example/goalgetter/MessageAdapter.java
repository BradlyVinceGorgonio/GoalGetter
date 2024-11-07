package com.example.goalgetter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.messageList = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new RightMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new LeftMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof RightMessageViewHolder) {
            ((RightMessageViewHolder) holder).bind(message);
        } else {
            ((LeftMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView userNameText;
        TextView timestampText;
        ImageView messageImage;

        LeftMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            userNameText = itemView.findViewById(R.id.user_name);
            timestampText = itemView.findViewById(R.id.timestamp);
            messageImage = itemView.findViewById(R.id.image_message);
        }

        void bind(Message message) {
            userNameText.setText(message.getUserName());
            messageText.setText(message.getMessageText());
            timestampText.setText(message.getFormattedTimestamp());
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getImageUrl()).into(messageImage);
            } else {
                messageImage.setVisibility(View.GONE);
            }
        }
    }

    static class RightMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView userNameText;
        TextView timestampText;
        ImageView messageImage;

        RightMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            userNameText = itemView.findViewById(R.id.user_name);
            timestampText = itemView.findViewById(R.id.timestamp);
            messageImage = itemView.findViewById(R.id.image_message);
        }

        void bind(Message message) {
            userNameText.setText(message.getUserName());
            messageText.setText(message.getMessageText());
            timestampText.setText(message.getFormattedTimestamp());
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getImageUrl()).into(messageImage);
            } else {
                messageImage.setVisibility(View.GONE);
            }
        }
    }
}
