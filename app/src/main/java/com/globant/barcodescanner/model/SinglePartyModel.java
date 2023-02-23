package com.globant.barcodescanner.model;

import com.google.gson.annotations.SerializedName;

public class SinglePartyModel {
    public String partyid;
    public String partyname;
    public String dotate;
    @SerializedName("TRANSPORT")
    public String transport;
    @SerializedName("TRUCKNO")
    public String truckNo;
    public String gateentry;
}
