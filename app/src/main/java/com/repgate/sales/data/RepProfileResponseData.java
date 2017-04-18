package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class RepProfileResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public String ID;
        public String user_email;
        public String display_name;
        public String avatar;
        public String logo;
        public String birthday;
        public String gender;
        public String phone;
        public String officeAddr;
        public String productInfo;
        public int hasNew;
    }

    public static class ErrorModel {
        public String err_msg;
    }
}
