package com.example.projectchatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.projectchatapplication.adapter.ChatAdapter;
import com.example.projectchatapplication.databinding.ActivityChatBinding;
import com.example.projectchatapplication.models.ChatMessage;
import com.example.projectchatapplication.models.User;
import com.example.projectchatapplication.utilities.Constants;
import com.example.projectchatapplication.utilities.Preference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receivedUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private Preference preferenceManger;
    private FirebaseFirestore database;
    private String conversationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();
    }

    private void init(){
        preferenceManger = new Preference(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,getBitmapFromEncoded(receivedUser.image),
                preferenceManger.getString(Constants.KEY_USER_ID));
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null){
            updateConversation(binding.inputMessage.getText().toString());
        }
        else{
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManger.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManger.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        binding.inputMessage.setText(null);
    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManger.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    //Định nghĩa 1 event listenner theo kiểu snapshotListenner
   private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
      if(error != null){
          return;
      }
        // value ở đây là dạng 1 query snapshot trong đó chứa 1 hoặc nhiều document snapshot
        //  trong trường hợp này mình sử dụng để loop qua tất cả các thay đổi vừa đc nghe thấy trong database
      if(value != null){
          int count = chatMessages.size();
          // loop qua tất cả các bản ghi có sự thay đổi
          for(DocumentChange documentChange : value.getDocumentChanges()){
              // Nếu bản ghi mới đc add vào thì thực hiện thêm mới vào list chat
              // ngoài ra kiểu documentChange còn có các type như modified hay removed tùy vào điều kiện mọi người muốn bắt
             if(documentChange.getType() == DocumentChange.Type.ADDED){
                 // đoạn này là lấy ra đoạn chat vừa đc thêm mới rồi lưu vào object thôi
                 ChatMessage chatMessage = new ChatMessage();
                 chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                 chatMessage.receiverID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                 chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                 chatMessage.dateTime = getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                 chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                 //add vào list này
                 chatMessages.add(chatMessage);
             }
          }
          Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
          if(count == 0){
              chatAdapter.notifyDataSetChanged();
          }
          else{
              chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
              binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
          }
          binding.chatRecycleView.setVisibility(View.VISIBLE);
      }
      binding.progressBar.setVisibility(View.GONE);
      if(conversationId == null){
          checkForConversation();
      }
   });

    private Bitmap getBitmapFromEncoded(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0 ,bytes.length);
    }

    private void loadReceiverDetails(){
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receivedUser.name);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDate(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversation(){
        if(chatMessages.size() != 0){
            checkForConversationRemotely(preferenceManger.getString(Constants.KEY_USER_ID), receivedUser.id);
            checkForConversationRemotely(receivedUser.id, preferenceManger.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkForConversationRemotely(String senderId , String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };
}