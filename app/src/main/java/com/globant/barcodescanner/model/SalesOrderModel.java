package com.globant.barcodescanner.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SalesOrderModel{
    public String dono;
    @SerializedName("DODATE")
    public String dODATE;
    @SerializedName("PARTYID")
    public String pARTYID;
    @SerializedName("TOTALWT")
    public double tOTALWT;
    @SerializedName("TRANSPORT")
    public String transport;
    @SerializedName("TRUCKNO")
    public String truckNo;
    public Long geno;
    @SerializedName("Msg")
    public String msg;
    @SerializedName("DraftsalesOrderInputDetails")
    public ArrayList<DraftsalesOrderInputDetail> draftsalesOrderInputDetails;
}

