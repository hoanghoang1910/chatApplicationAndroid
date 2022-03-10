package com.example.projectchatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.projectchatapplication.R;
import com.example.projectchatapplication.databinding.ActivityEditProfileBinding;

import java.util.zip.Inflater;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}