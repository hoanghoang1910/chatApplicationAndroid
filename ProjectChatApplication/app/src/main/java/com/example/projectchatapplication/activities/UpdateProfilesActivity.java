package com.example.projectchatapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projectchatapplication.R;
import com.example.projectchatapplication.databinding.ActivitySignUpBinding;
import com.example.projectchatapplication.databinding.ActivityUserProfilesBinding;
import com.example.projectchatapplication.utilities.CommonFunction;
import com.example.projectchatapplication.utilities.Constants;
import com.example.projectchatapplication.utilities.Preference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class UpdateProfilesActivity extends AppCompatActivity {
    private ActivityUserProfilesBinding binding;
    private Preference preferenceManager;
    private String encodeImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new Preference(getApplicationContext());
        binding = ActivityUserProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadUserDetail();
        setListenerForChangeImage();
        setListenerForUpdateProfiles();
        setListenerForUpdatePassword();
    }

    private void loadUserDetail(){
        binding.inputUpdateEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.inputUpdateName.setText(preferenceManager.getString(Constants.KEY_NAME));
        encodeImage = preferenceManager.getString(Constants.KEY_IMAGE);
        binding.imageNewProfile.setImageBitmap(
                CommonFunction.getBitmapFromEncoded(encodeImage)
        );
    }

    private void setListenerForUpdateProfiles(){
        binding.buttonCfmUpdateProfile.setOnClickListener(v -> {
            if (checkNotChangeEmail())
                updateProfiles();
            else {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, binding.inputUpdateEmail.getText().toString())
                        .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()  && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                                showToast("This email is used!");
                            }
                            else {
                                updateProfiles();
                            }
                        });
            }
        });
    }

    private void setListenerForUpdatePassword(){
        binding.buttonChangePassword.setOnClickListener(v -> {
            if (!checkValidInputPassword()) {
                showToast("Password must not empty!");
                return;
            }
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
                    .whereEqualTo(Constants.KEY_PASSWORD, binding.inputOldPassword.getText().toString())
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()  && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                            if (checkMatchNewPassword()){
                                updatePassword();
                                showToast("Successfully Update");
                            }
                            else {
                                showToast("Password does not match!");
                            }
                        }
                        else {
                            showToast("Incorrect Password!");
                        }
                    });
        });
    }

    private void setListenerForChangeImage(){
        binding.imageNewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageNewProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage = CommonFunction.encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean checkMatchNewPassword(){
        if (binding.inputNewPassword.getText().toString()
                .equals(binding.inputConfirmNewPassword.getText().toString())){
            Log.i("Not match password", "true");
            return true;
        }
        return false;
    }

    private boolean checkValidInputPassword(){
        if (binding.inputNewPassword.getText().toString().trim().isEmpty()
                || binding.inputConfirmNewPassword.getText().toString().trim().isEmpty()
                || binding.inputOldPassword.getText().toString().trim().isEmpty())
            return false;
        else return true;
    }

    private boolean checkValidInputProfiles(){
        if (binding.inputUpdateName.getText().toString().trim().isEmpty()
        || binding.inputUpdateEmail.getText().toString().trim().isEmpty())
            return false;
        else return true;
    }

    private boolean checkNotChangeEmail(){
        if (binding.inputUpdateEmail.getText().toString().trim().equals(preferenceManager.getString(Constants.KEY_EMAIL)))
            return true;
        else return false;
    }

    private void updateProfiles(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputUpdateName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputUpdateEmail.getText().toString());
        user.put(Constants.KEY_IMAGE, encodeImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString((Constants.KEY_USER_ID)))
                .update(user)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        updateUserDetail();
                        loadUserDetail();
                        showToast("Successfully Update");
                    }
                    else{
                        showToast("Fail");
                    }
                });
    }

    private void updatePassword(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_PASSWORD, binding.inputNewPassword.getText().toString());
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString((Constants.KEY_USER_ID)))
                .update(user)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        showToast("Successfully Update");
                    }
                    else{
                        showToast("Fail");
                    }
                });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateUserDetail(){
        preferenceManager.putString(Constants.KEY_NAME, binding.inputUpdateName.getText().toString());
        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputUpdateEmail.getText().toString());
        preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
    }
}