package com.example.goalgetter;

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.goalgetter.adapters.UserAdapter;
import com.example.goalgetter.models.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    private List<User> selectedUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        ImageButton createGroupButton = view.findViewById(R.id.group_create);
        createGroupButton.setOnClickListener(v -> showUserSelectionPopup());

        return view;
    }

    private void showUserSelectionPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_select_profiles, null);
        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        Button selectButton = popupView.findViewById(R.id.select_button);

        List<User> users = new ArrayList<>();
        UserAdapter adapter = new UserAdapter(getContext(), users, selectedUsers -> {
            this.selectedUsers = selectedUsers;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(popupView);
        builder.setCancelable(true);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        selectButton.setOnClickListener(v -> {

            Intent intent = new Intent(getContext(), ChatGroupActivity.class);
            // intent.putParcelableArrayListExtra("SELECTED_USERS", new ArrayList<>(selectedUsers));
            startActivity(intent); // Start the new activity
            dialog.dismiss();
        });
    }

    private void createGroupWithSelectedUsers(List<User> selectedUsers) {
    }
}
