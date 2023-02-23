package com.globant.barcodescanner.model;

import com.google.gson.annotations.SerializedName;

public class PartyModel {
    @SerializedName("ITEMNAME")
    public String itemName;
    @SerializedName("ITEMID")
    public String itemId;
    public String itemwt;
    @SerializedName("QTY")
    public String qty;
    public String itemcode;
    public String itemgrade;
    public String itemlength;
}
