package com.globant.barcodescanner.network;

import com.globant.barcodescanner.model.BarcodeDetails;
import com.globant.barcodescanner.model.BarcodeNoRequest;
import com.globant.barcodescanner.model.GetHelperModel;
import com.globant.barcodescanner.model.PartyModel;
import com.globant.barcodescanner.model.PartyRequest;
import com.globant.barcodescanner.model.SalesOrderModel;
import com.globant.barcodescanner.model.SinglePartyModel;
import com.globant.barcodescanner.model.SinglePartyRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface GetDataService {

    @POST("User/Partyorderdetail")
    Call<ArrayList<PartyModel>> getAllParty(@Body SinglePartyRequest body);

    @POST("User/partydetails")
    Call<ArrayList<SinglePartyModel>> getPartyDetails(@Body PartyRequest body);

    @POST("User/barcodescan")
    Call<ArrayList<BarcodeDetails>> getBarcodeDetails(@Body BarcodeNoRequest body);

    @POST("User/salesorder")
    Call<SalesOrderModel> submitSalesOrder(@Body SalesOrderModel body);

    @GET
    Call<GetHelperModel> getLatestUpdate(@Url String url);
}
