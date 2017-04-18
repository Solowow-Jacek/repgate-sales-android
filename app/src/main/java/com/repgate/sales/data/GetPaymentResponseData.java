package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class GetPaymentResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public String price;
    }

    public static class ErrorModel {
        public String err_msg;
    }
}
