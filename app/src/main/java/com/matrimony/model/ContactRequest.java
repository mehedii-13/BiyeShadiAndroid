package com.matrimony.model;

public class ContactRequest {
    private int id;
    private int senderId;
    private int receiverId;
    private String status; // pending, accepted, rejected
    private String message;
    private long requestDate;
    private long responseDate;
    
    public ContactRequest() {
    }
    
    public ContactRequest(int id, int senderId, int receiverId, String status,
                          String message, long requestDate, long responseDate) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.message = message;
        this.requestDate = requestDate;
        this.responseDate = responseDate;
    }
    
    public ContactRequest(int senderId, int receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.status = "pending";
        this.requestDate = System.currentTimeMillis();
        this.responseDate = 0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public long getRequestDate() { return requestDate; }
    public void setRequestDate(long requestDate) { this.requestDate = requestDate; }
    
    public long getResponseDate() { return responseDate; }
    public void setResponseDate(long responseDate) { this.responseDate = responseDate; }
}
