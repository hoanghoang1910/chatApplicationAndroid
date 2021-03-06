package com.example.projectchatapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectchatapplication.databinding.ItemContainerReceivedMessageBinding;
import com.example.projectchatapplication.databinding.ItemContainerSentMessageBinding;
import com.example.projectchatapplication.models.ChatMessage;
import com.example.projectchatapplication.utilities.CommonFunction;
import com.example.projectchatapplication.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final String receiverId;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessageList, String receiverId, String senderId) {
        this.chatMessageList = chatMessageList;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false)
            );
        }
        else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
        }
        else{
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(receiverId)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null){
                                    receiverProfileImage = CommonFunction.getBitmapFromEncoded(
                                            task.getResult().getString(Constants.KEY_IMAGE));
                                    ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position), receiverProfileImage);
                                }
                            });
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else{
            return  VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
    }
}
