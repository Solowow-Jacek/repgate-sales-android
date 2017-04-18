package com.repgate.sales.data;

import java.util.ArrayList;

/**
 * Created by developer on 3/18/16.
 */
public class AllMessageResponseData {

    public boolean success;
    public ArrayList<MessageResponseData.DataModel> data;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }
}
