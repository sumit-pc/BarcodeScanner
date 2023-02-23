package com.globant.barcodescanner.model;

import com.google.gson.annotations.SerializedName;

public class DraftsalesOrderInputDetail{
    @SerializedName("ITEMID")
    public String iTEMID;
    @SerializedName("BOUNDLENO")
    public String bOUNDLENO;
    @SerializedName("ITEMWT")
    public double iTEMWT;
    @SerializedName("REQWT")
    public double rEQWT;
    @SerializedName("QTY")
    public int qTY;
    public String itemlength;
    public String itemgrade;
}
