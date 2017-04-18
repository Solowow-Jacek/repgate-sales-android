package com.repgate.sales.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.repgate.sales.data.DoctorProfileResponseData;
import com.repgate.sales.data.ProviderProfileResponseData;
import com.repgate.sales.data.SpecialtyResponseData;

import java.util.ArrayList;

public class AppPreferences {
    private final static String TAG = "AppPreferences";

    private static String APP_SHARED_PREFS;

    private SharedPreferences mPrefs;
    private Editor mPrefsEditor;

    public static final String USER_ID      = "doctor_id";
    public static final String USER_NAME    = "doctor_name";
    public static final String USER_EMAIL   = "doctor_email";
    public static final String USER_AVATAR  = "doctor_avatar";
    public static final String USER_ROLE = "doctor_role";
    public static final String USER_LOGO    = "doctor_logo";
    public static final String USER_BIRTH   = "doctor_birth";
    public static final String USER_GENDER  = "doctor_gender";
    public static final String USER_PHONE   = "doctor_phone";
    public static final String USER_ADDRESS = "doctor_address";
    public static final String USER_CODE    = "doctor_code";
    public static final String USER_PSPEC_ID = "doctor_pspec_id";
    public static final String USER_PSPEC_NAME = "doctor_pspec_name";
    public static final String USER_CSPEC_ID   = "doctor_cspec_id";
    public static final String USER_CSPEC_NAME   = "doctor_cspec_name";
    public static final String USER_FIRST_NAME = "doctor_first_name";
    public static final String USER_LAST_NAME = "doctor_last_name";
    public static final String USER_EDUCATION = "doctor_education";
    public static final String USER_COMPANY = "doctor_company";
    public static final String USER_PRODUCTS = "doctor_products";
    public static final String USER_CETIFICATIONS = "doctor_certifications";
    public static final String USER_AWARDS = "doctor_awards";
    public static final String USER_BLOCK_ALLOW_REQUEST = "doctor_block_allow_request";
    public static final String USER_BLOCK_ALLOW_MESSAGE = "doctor_block_allow_message";
    public static final String USER_PASSWORD = "doctor_password";
    public static final String USER_MESSAGE_NEW = "doctor_message_new";
    public static final String USER_REQUEST_NEW = "doctor_request_new";

    public static final String AUTO_LOGIN = "auto_login";


    public AppPreferences(Context context) {
        APP_SHARED_PREFS = context.getApplicationContext().getPackageName();
        mPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        mPrefsEditor = mPrefs.edit();
    }

    public void saveUserInformation(DoctorProfileResponseData.DataModel responseData) {
        String id = responseData.ID;
        String name = responseData.displayName;
        String email = responseData.email;
        String avatar = responseData.logoUrl;
        String birthday = responseData.birthday;
        String gender = responseData.gender;
        String phone = responseData.phone;
        String address = responseData.officeAddr;
        String code = responseData.userCode;
        String parent_spec = responseData.pSpecialty;
        String parent_spec_name = responseData.pSpecialty_name;
        String child_spec = responseData.cSpecialty;
        String education = responseData.education;
        String products = responseData.products;
        String certificates = responseData.certifications;
        String company = responseData.company;
        String awards = responseData.awards;
        String role = responseData.role;
        String block_allow_request = responseData.block_allow_request;
        String block_allow_message = responseData.block_allow_message;
        String password = responseData.password;
        String messageNew = String.valueOf(responseData.messageNew);
        String requestNew = String.valueOf(responseData.requestNew);

        mPrefsEditor.putString(USER_ID, id);
        mPrefsEditor.putString(USER_NAME, name);
        mPrefsEditor.putString(USER_EMAIL, email);
        mPrefsEditor.putString(USER_ROLE, role);
        mPrefsEditor.putString(USER_AVATAR, avatar);
        mPrefsEditor.putString(USER_BIRTH, birthday);
        mPrefsEditor.putString(USER_GENDER, gender);
        mPrefsEditor.putString(USER_PHONE, phone);
        mPrefsEditor.putString(USER_ADDRESS, address);
        mPrefsEditor.putString(USER_CODE, code);
        mPrefsEditor.putString(USER_PSPEC_ID, parent_spec);
        mPrefsEditor.putString(USER_PSPEC_NAME, parent_spec_name);
        mPrefsEditor.putString(USER_CSPEC_ID, child_spec);
        //mPrefsEditor.putString(USER_CSPEC_NAME, child_spec.name);

        mPrefsEditor.putString(USER_COMPANY, company);
        mPrefsEditor.putString(USER_PRODUCTS, products);
        mPrefsEditor.putString(USER_EDUCATION, education);
        mPrefsEditor.putString(USER_CETIFICATIONS, certificates);
        mPrefsEditor.putString(USER_AWARDS, awards);
        mPrefsEditor.putString(USER_BLOCK_ALLOW_REQUEST, block_allow_request);
        mPrefsEditor.putString(USER_BLOCK_ALLOW_MESSAGE, block_allow_message);
        mPrefsEditor.putString(USER_PASSWORD, password);
        mPrefsEditor.putString(USER_MESSAGE_NEW, messageNew);
        mPrefsEditor.putString(USER_REQUEST_NEW, requestNew);

        mPrefsEditor.commit();
    }

    public String getUserId() {
        return mPrefs.getString(USER_ID, "");
    }
    public String getUserName() {
        return mPrefs.getString(USER_NAME, "");
    }

    public String getUserEmail() {
        return mPrefs.getString(USER_EMAIL, "");
    }
    public String getUserRole() {
        return mPrefs.getString(USER_ROLE, "");
    }

    public String getUserAvatar() {
        return mPrefs.getString(USER_AVATAR, "");
    }
    public void setUserAvatar(String url) {
        mPrefsEditor.putString(USER_AVATAR, url);
        mPrefsEditor.commit();
    }

    public String getUserLogo() { return mPrefs.getString(USER_LOGO, ""); }
    public void setUserLogo(String logo) {
        mPrefsEditor.putString(USER_LOGO, logo);
        mPrefsEditor.commit();
    }

    public String getUserBirth() {
        return mPrefs.getString(USER_BIRTH, "");
    }

    public String getUserGender() {
        return mPrefs.getString(USER_GENDER, "");
    }

    public String getUserPhone() {
        return mPrefs.getString(USER_PHONE, "");
    }

    public String getUserAddress() {
        return mPrefs.getString(USER_ADDRESS, "");
    }

    public void clearUserInformation() {
        mPrefsEditor.clear().commit();
    }

    public boolean getAutoLogin() { return mPrefs.getBoolean(AUTO_LOGIN, false); }
    public void setAutoLogin(boolean enable) {
        mPrefsEditor.putBoolean(AUTO_LOGIN, enable);
        mPrefsEditor.commit();
    }

    public String getUserCode() { return mPrefs.getString(USER_CODE, ""); }
    public void setUserCode(String code) {
        mPrefsEditor.putString(USER_CODE, code);
        mPrefsEditor.commit();
    }

    public String getUserParentSpecID() { return mPrefs.getString(USER_PSPEC_ID, ""); }
    public void setUserParentSpecID(String specID) {
        mPrefsEditor.putString(USER_PSPEC_ID, specID);
        mPrefsEditor.commit();
    }

    public String getUserParentSpecName() { return mPrefs.getString(USER_PSPEC_NAME, ""); }
    public void setUserParentSpecName(String name) {
        mPrefsEditor.putString(USER_PSPEC_NAME, name);
        mPrefsEditor.commit();
    }

    public String getUserChildSpecID() { return mPrefs.getString(USER_CSPEC_ID, ""); }
    public void setUserChildSpecID(String specId) {
        mPrefsEditor.putString(USER_CSPEC_ID, specId);
        mPrefsEditor.commit();
    }

    public String getUserChildSpecName() { return mPrefs.getString(USER_CSPEC_NAME, ""); }
    public void setUserChildSpecName(String name) {
        mPrefsEditor.putString(USER_CSPEC_NAME, name);
        mPrefsEditor.commit();
    }

    public String getUserFirstName() { return mPrefs.getString(USER_FIRST_NAME, ""); }
    public void setUserFirstName(String firstname) {
        mPrefsEditor.putString(USER_FIRST_NAME, firstname);
        mPrefsEditor.commit();
    }

    public String getUserLastName() { return mPrefs.getString(USER_LAST_NAME, "") ; }
    public void setUserLastName(String lastName) {
        mPrefsEditor.putString(USER_LAST_NAME, lastName);
        mPrefsEditor.commit();
    }

    public String getUserEducation() { return mPrefs.getString(USER_EDUCATION, ""); }
    public void setUserEducation(String education) {
        mPrefsEditor.putString(USER_EDUCATION, education);
        mPrefsEditor.commit();
    }

    public String getUserCompany() { return mPrefs.getString(USER_COMPANY, ""); }
    public void setUserCompany(String company) {
        mPrefsEditor.putString(USER_COMPANY, company);
        mPrefsEditor.commit();
    }

    public String getUserCetifications() { return mPrefs.getString(USER_CETIFICATIONS, ""); }
    public String getUserAwards() { return mPrefs.getString(USER_AWARDS, "");}

    public String getUserBlockAllowRequest() { return mPrefs.getString(USER_BLOCK_ALLOW_REQUEST, ""); }
    public void setUserBlockAllowRequest(String block_allow) {
        mPrefsEditor.putString(USER_BLOCK_ALLOW_REQUEST, block_allow);
        mPrefsEditor.commit();
    }

    public String getUserBlockAllowMessage() { return mPrefs.getString(USER_BLOCK_ALLOW_MESSAGE, ""); }
    public void setUserBlockAllowMessage(String block_allow) {
        mPrefsEditor.putString(USER_BLOCK_ALLOW_MESSAGE, block_allow);
        mPrefsEditor.commit();
    }

    public String getUserPassword() { return mPrefs.getString(USER_PASSWORD, ""); }
    public void setUserPassword(String password) {
        mPrefsEditor.putString(USER_PASSWORD, password);
        mPrefsEditor.commit();
    }

    public String getUserMessageNew() { return mPrefs.getString(USER_MESSAGE_NEW, ""); }
    public void setUserMessageNew(String messageNew) {
        mPrefsEditor.putString(USER_MESSAGE_NEW, messageNew);
        mPrefsEditor.commit();
    }

    public String getUserRequestNew() { return mPrefs.getString(USER_REQUEST_NEW, ""); }
    public void setUserRequestNew(String requestNew) {
        mPrefsEditor.putString(USER_REQUEST_NEW, requestNew);
        mPrefsEditor.commit();
    }
}
