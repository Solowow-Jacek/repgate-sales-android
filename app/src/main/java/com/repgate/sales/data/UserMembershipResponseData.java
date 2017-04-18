package com.repgate.sales.data;

/**
 * Created by developer on 6/20/2016.
 */
public class UserMembershipResponseData {
    public boolean success;
    public String member_ship;
    public String member_ship_plan;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }
}
