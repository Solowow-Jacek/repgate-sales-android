package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class RequestResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public String ID;
        public String title;
        public String content;
        public String senderId;
        public String changerId;
        public String senderName;
        public String senderAvatar;
        public String receiverId;
        public String receiverName;
        public String receiverAvatar;
        public String requestType;
        public String requestDateTime;
        public String handleStatus;
        public String createdAt;
        public boolean isNew;
        public boolean office_schedule;
        public String senderImageUrl;
    }
    public static class ErrorModel {
        public String err_msg;
    }
}
