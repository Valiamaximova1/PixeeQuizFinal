package com.example.chipiquizfinal.models;

public class ChatMessage {
        private String senderId;
        private String message;
        private long timestamp;
        private boolean isMine;

        public ChatMessage(String senderId, String message, long timestamp, boolean isMine) {
            this.senderId  = senderId;
            this.message   = message;
            this.timestamp = timestamp;
            this.isMine    = isMine;
        }


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}