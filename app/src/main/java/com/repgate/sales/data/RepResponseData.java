package com.repgate.sales.data;

import java.util.ArrayList;

/**
 * Created by developer on 3/18/16.
 */
public class RepResponseData {

    public boolean success;
    public ArrayList<RepProfileResponseData.DataModel> data;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }
}
