package com.repgate.sales.data;

import java.util.ArrayList;

/**
 * Created by developer on 3/18/16.
 */
public class SpecialtyResponseData {

    public boolean success;
    public ArrayList<DataModel> data;
    public ErrorModel error;

    public static class DataModel {
        public String id;
        public String name;
        public String slug;
    }

    public static class ErrorModel {
        public String err_msg;
    }
}
