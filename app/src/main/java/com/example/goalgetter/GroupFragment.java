package com.example.goalgetter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class GroupFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        ImageButton groupCreateButton = view.findViewById(R.id.group_create);
        groupCreateButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatGroupActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
