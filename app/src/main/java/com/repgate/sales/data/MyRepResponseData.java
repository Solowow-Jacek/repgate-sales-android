package com.repgate.sales.data;

import java.util.ArrayList;

/**
 * Created by developer on 3/18/16.
 */
public class MyRepResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public ArrayList<RepProfileResponseData.DataModel> reps;
        public ArrayList<DoctorProfileResponseData.DataModel> doctors;
    }

    public static class ErrorModel {
        public String err_msg;
    }
}
