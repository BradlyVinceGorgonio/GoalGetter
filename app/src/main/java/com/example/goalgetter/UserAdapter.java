package com.example.goalgetter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private List<User> filteredUserList;
    private OnUserSelectedListener listener;

    public UserAdapter(Context context, List<User> userList, OnUserSelectedListener listener) {
        this.context = context;
        this.userList = userList;
        this.filteredUserList = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());

        holder.itemView.setOnClickListener(v -> {
            if (user.isSelected()) {
                user.setSelected(false);
                listener.onUserDeselected(user);
                holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            } else {
                user.setSelected(true);
                listener.onUserSelected(user);
                holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnUserSelectedListener {
        void onUserSelected(User user);
        void onUserDeselected(User user);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
        }
    }

    public void updateData(List<User> newUsers) {
        this.userList = newUsers; // Update the main user list
        this.filteredUserList = new ArrayList<>(newUsers); // Update the filtered list
        notifyDataSetChanged(); // Notify the adapter about data changes
    }
    public void filterUsers(String query) {
        if (query.isEmpty()) {
            filteredUserList = new ArrayList<>(userList);
        } else {
            filteredUserList = new ArrayList<>();
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

}
