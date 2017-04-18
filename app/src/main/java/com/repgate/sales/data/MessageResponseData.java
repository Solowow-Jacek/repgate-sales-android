package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class MessageResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public String ID;
        public String title;
        public String senderId;
        public String senderName;
        //public String senderAvatar;
        public String receiverId;
        public String receiverName;
        //public String receiverAvatar;
        public String attachs;
        public String pdfLink;
        public String videoLink;
        public String productInfo;
        public String content;
        public String createdAt;
        public boolean isNew;
        public String senderImageUrl;
    }

    public static class ErrorModel {
        public String err_msg;
    }
}
