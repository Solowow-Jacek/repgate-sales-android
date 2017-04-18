package com.repgate.sales.data;

/**
 * Created by developer on 3/18/16.
 */
public class DoctorProfileResponseData {

    public boolean success;
    public DataModel data;
    public ErrorModel error;

    public static class DataModel {
        public String ID;
        public String email;
        public String password;
        public String displayName;
        public String role;
        public String userCode;
        public String deviceId;
        public String deviceType;
        public String logoUrl;
        public String birthday;
        public String gender;
        public String phone;
        public String officeAddr;
//        public SpecialtyResponseData.DataModel parentSpecialty;
//        public SpecialtyResponseData.DataModel childSpecialty;
        public String pSpecialty;
        public String pSpecialty_name;
        public String cSpecialty;
        public String education;
        public String certifications;
        public String company;
        public String awards;
        public String block_allow_request;
        public String block_allow_message;
        public String products;
        public String area_of_interest;
        public int messageNew;
        public int requestNew;
    }

    public static class ErrorModel {
        public String err_msg;
    }
}
