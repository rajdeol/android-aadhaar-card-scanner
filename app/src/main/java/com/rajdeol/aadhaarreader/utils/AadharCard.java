package com.rajdeol.aadhaarreader.utils;

import android.graphics.Bitmap;

public class AadharCard {
    private String name;
    private String dateOfBirth;
    private String gender;
    private String careOf;
    private String district;
    private String landmark;
    private String house;
    private String location;
    private String pinCode;
    private String postOffice;
    private String state;
    private String street;
    private String subDistrict;
    private String vtc;
    private Bitmap image;
    private String email;
    private String mobile;
    private String signature;

    public void setName(String aName){ name = aName; }
    public void setDateOfBirth(String aDateOfBirth){
        dateOfBirth = aDateOfBirth;
    }
    public void setGender(String aGender){
        gender = aGender;
    }
    public void setCareOf(String aCareOf){
        careOf = aCareOf;
    }
    public void setDistrict(String aDistrict){
        district = aDistrict;
    }
    public void setLandmark(String aLandmark){
        landmark = aLandmark;
    }
    public void setHouse(String aHouse){
        house = aHouse;
    }
    public void setLocation(String aLocation){ location = aLocation; }
    public void setPinCode(String aPinCode){
        pinCode = aPinCode;
    }
    public void setPostOffice(String aPostOffice){
        postOffice = aPostOffice;
    }
    public void setState(String aState){
        state = aState;
    }
    public void setStreet(String aStreet){
        street = aStreet;
    }
    public void setSubDistrict(String aSubDistrict){
        subDistrict = aSubDistrict;
    }
    public void setVtc(String aVtc){
        vtc = aVtc;
    }
    public void setImage(Bitmap aImage){
        image = aImage;
    }
    public void setEmail(String aEmail){
        email = aEmail;
    }
    public void setMobile(String aMobile){
        mobile = aMobile;
    }
    public void setSignature(String aSignature){
        signature = aSignature;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getCareOf() {
        return careOf;
    }

    public String getDistrict() {
        return district;
    }

    public String getLandmark() {
        return landmark;
    }

    public String getHouse() {
        return house;
    }

    public String getLocation() {
        return location;
    }

    public String getPinCode() {
        return pinCode;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public String getState() {
        return state;
    }

    public String getStreet() {
        return street;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public String getVtc() {
        return vtc;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getSignature() {
        return signature;
    }
}
