package com.globant.barcodescanner.model;

public class SinglePartyRequest {
    public String dono;
    public String partyid;

    public SinglePartyRequest(String dono, String partyid) {
        this.dono = dono;
        this.partyid = partyid;
    }
}
