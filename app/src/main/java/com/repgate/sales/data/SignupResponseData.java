package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class SignupResponseData {

    public boolean success;
    public DoctorProfileResponseData.DataModel data;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }
}
