package com.repgate.sales.data;

import java.util.ArrayList;

/**
 * Created by developer on 3/18/16.
 */
public class AllRequestResponseData {

    public boolean success;
    public ArrayList<RequestData> data;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }

    public static class RequestData {
        public String date;
        public ArrayList<RequestResponseData.DataModel> reqs;
    }
}
