package com.example.projectchatapplication.models;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    public String senderId, receiverID, message, dateTime;

    public Date dateObject;
}
