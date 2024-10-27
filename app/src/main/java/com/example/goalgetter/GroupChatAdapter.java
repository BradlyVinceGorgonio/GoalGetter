package com.example.goalgetter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder> {
    private final Context context;
    private final List<GroupChat> groupChats;
    private final OnGroupChatClickListener listener;

    public interface OnGroupChatClickListener {
        void onGroupChatClick(GroupChat groupChat);
    }

    public GroupChatAdapter(Context context, List<GroupChat> groupChats, OnGroupChatClickListener listener) {
        this.context = context;
        this.groupChats = groupChats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_chat, parent, false);
        return new GroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChat groupChat = groupChats.get(position);
        holder.bind(groupChat, listener);
    }

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    public static class GroupChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView groupNameTextView;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.group_name_text_view);
        }

        public void bind(GroupChat groupChat, OnGroupChatClickListener listener) {
            groupNameTextView.setText(groupChat.getGroupName());
            itemView.setOnClickListener(v -> listener.onGroupChatClick(groupChat));
        }
    }
}
