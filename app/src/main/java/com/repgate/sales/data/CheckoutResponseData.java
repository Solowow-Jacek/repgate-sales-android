package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class CheckoutResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public String charge_id;
        public float charge_amount;
        public String charge_currency;
//        public Date charge_created;
        public boolean charge_livemode;
        public boolean charge_paid;

    }

    public static class ErrorModel {
        public String err_msg;
    }
}
