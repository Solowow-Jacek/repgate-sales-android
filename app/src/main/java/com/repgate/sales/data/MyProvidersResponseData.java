package com.repgate.sales.data;

import java.util.ArrayList;

/**
 * Created by developer on 6/20/2016.
 */
public class MyProvidersResponseData {
    public boolean success;
    public ArrayList<ProviderProfileResponseData.DataModel> data;
    public ErrorModel error;

    public static class ErrorModel {
        public String err_msg;
    }
}
