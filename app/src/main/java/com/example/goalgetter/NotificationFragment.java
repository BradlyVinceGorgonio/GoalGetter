package com.example.goalgetter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NotificationFragment extends Fragment {

    public NotificationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        TextView notificationMessage = view.findViewById(R.id.notificationMessage);
        notificationMessage.setText("You have new notifications!");

        notificationMessage.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        return view;
    }
}
