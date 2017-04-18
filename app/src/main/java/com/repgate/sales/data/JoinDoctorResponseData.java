package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class JoinDoctorResponseData {

    public boolean success;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }
}
