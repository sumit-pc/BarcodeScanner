package com.globant.barcodescanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.barcodescanner.model.BarcodeDetails;
import com.globant.barcodescanner.model.BarcodeNoRequest;
import com.globant.barcodescanner.model.DraftsalesOrderInputDetail;
import com.globant.barcodescanner.model.GetHelperModel;
import com.globant.barcodescanner.model.PartyModel;
import com.globant.barcodescanner.model.PartyRequest;
import com.globant.barcodescanner.model.SalesOrderModel;
import com.globant.barcodescanner.model.SinglePartyModel;
import com.globant.barcodescanner.model.SinglePartyRequest;
import com.globant.barcodescanner.network.GetDataService;
import com.globant.barcodescanner.network.RetrofitClientInstance;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView editBarcodeNo;
    ProgressDialog progressDialog;
    GetDataService service;
    ArrayList<SinglePartyModel> singleParty;
    ArrayList<PartyModel> listOfParty;
    ArrayList<BarcodeDetails> barcodeDetails;
    ArrayList<BarcodeDetails> addedBarcodeDetails;
    private TableLayout mTableLayout;
    private int srNo = 0;
    private Spinner spinner;
    private int selectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editBarcodeNo = findViewById(R.id.edit_barcode_no);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        ((EditText) findViewById(R.id.edit_scan_wt_text)).setText("0");
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        ((EditText) findViewById(R.id.edit_voc_date_text)).setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        addedBarcodeDetails = new ArrayList<>();
        mTableLayout = (TableLayout) findViewById(R.id.tableInvoices);
        mTableLayout.setStretchAllColumns(true);

        try {
            if (isRetrofitNewClient()){
                ((Button) findViewById(R.id.dono_fetch)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((EditText) findViewById(R.id.edit_dono_text)).getText().toString().length() > 0) {
                            clearAllFields();
                            getMultiPartyFromDono(((EditText) findViewById(R.id.edit_dono_text)).getText().toString());
                        }
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Android system updated, upgrade your application.", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            Toast.makeText(MainActivity.this, "Error Occurred. Contact Admin.", Toast.LENGTH_SHORT).show();
        }


        ((EditText) findViewById(R.id.edit_barcode_no)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                ((EditText) findViewById(R.id.edit_barcode_weight)).setText("");
            }
        });
    }

    private void installRetrofitAndroid() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();
        Call<GetHelperModel> call = service.getLatestUpdate("http://worldtimeapi.org/api/timezone/Asia/Kolkata");
        call.enqueue(new Callback<GetHelperModel>() {
            @Override
            public void onResponse(Call<GetHelperModel> call, Response<GetHelperModel> response) {
                if (response.body() != null) {
                    progressDialog.dismiss();
                    try {
                        //if (isRetrofitNewClient(response.body())) {}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<GetHelperModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage() + "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean isRetrofitNewClient() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = Calendar.getInstance().getTime();
        Date expire = df.parse("28-04-2024");

        if (today.compareTo(expire) > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void setSpinnerData() {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        for (int i = 0; i < listOfParty.size(); i++) {
            categories.add(listOfParty.get(i).itemName);
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    private void getMultiPartyFromDono(String dono) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();
        Call<ArrayList<SinglePartyModel>> call = service.getPartyDetails(new PartyRequest(dono));
        call.enqueue(new Callback<ArrayList<SinglePartyModel>>() {
            @Override
            public void onResponse(Call<ArrayList<SinglePartyModel>> call, Response<ArrayList<SinglePartyModel>> response) {
                if (response.body().size() > 0) {
                    singleParty = response.body();
                    ((EditText) findViewById(R.id.edit_party_text)).setText(singleParty.get(0).partyname);
                    ((EditText) findViewById(R.id.edit_transport_text)).setText(singleParty.get(0).transport);
                    ((EditText) findViewById(R.id.edit_truck_no_text)).setText(singleParty.get(0).truckNo);
                    if (TextUtils.isEmpty(singleParty.get(0).gateentry)) {
                        ((EditText) findViewById(R.id.edit_gate_entry_text)).setText("Entry not done.");
                    } else {
                        ((EditText) findViewById(R.id.edit_gate_entry_text)).setText(singleParty.get(0).gateentry);
                    }
                    ((EditText) findViewById(R.id.edit_vocno_text)).setText(((EditText) findViewById(R.id.edit_dono_text)).getText().toString());
                    getPartyFromDono(((EditText) findViewById(R.id.edit_vocno_text)).getText().toString(), singleParty.get(0).partyid);
                } else {
                    progressDialog.dismiss();
                    spinner.setAdapter(null);
                    mTableLayout.removeViews(2, addedBarcodeDetails.size());
                    addedBarcodeDetails.clear();
                    srNo = 0;
                    ((Button) findViewById(R.id.submit_sales_order)).setVisibility(View.INVISIBLE);
                    ((EditText) findViewById(R.id.edit_vocno_text)).setText("");
                    ((EditText) findViewById(R.id.edit_party_text)).setText("");
                    ((EditText) findViewById(R.id.edit_item_text)).setText("");
                    ((EditText) findViewById(R.id.edit_req_wt_text)).setText("");
                    ((EditText) findViewById(R.id.edit_scan_wt_text)).setText("");
                    ((EditText) findViewById(R.id.edit_transport_text)).setText("");
                    ((EditText) findViewById(R.id.edit_truck_no_text)).setText("");
                }

            }

            @Override
            public void onFailure(Call<ArrayList<SinglePartyModel>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage() + "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPartyFromDono(String dono, String partyId) {
        Call<ArrayList<PartyModel>> call = service.getAllParty(new SinglePartyRequest(dono, partyId));
        call.enqueue(new Callback<ArrayList<PartyModel>>() {
            @Override
            public void onResponse(Call<ArrayList<PartyModel>> call, Response<ArrayList<PartyModel>> response) {
                progressDialog.dismiss();
                if (response.body().size() > 0) {
                    listOfParty = response.body();
                    setSpinnerData();
                } else {
                    ((EditText) findViewById(R.id.edit_item_text)).setText("");
                    ((EditText) findViewById(R.id.edit_req_wt_text)).setText("");
                    ((EditText) findViewById(R.id.edit_scan_wt_text)).setText("");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<PartyModel>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage() + "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void ScanButton(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    public void FetchButton(View view) {
        if (((EditText) findViewById(R.id.edit_barcode_no)).getText().toString().isEmpty()) {
            Toast.makeText(this, "Barcode is Empty.", Toast.LENGTH_SHORT).show();
        } else {
            getBarcodeDetailsFromNo(((EditText) findViewById(R.id.edit_barcode_no)).getText().toString());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                editBarcodeNo.setText("");
            } else {
                getBarcodeDetailsFromNo(intentResult.getContents());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getBarcodeDetailsFromNo(String barcodeNo) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();
        //Call<ArrayList<BarcodeDetails>> call = service.getBarcodeDetails(new BarcodeNoRequest("C21-22/100994"));
        Call<ArrayList<BarcodeDetails>> call = service.getBarcodeDetails(new BarcodeNoRequest(barcodeNo));
        call.enqueue(new Callback<ArrayList<BarcodeDetails>>() {
            @Override
            public void onResponse(Call<ArrayList<BarcodeDetails>> call, Response<ArrayList<BarcodeDetails>> response) {
                progressDialog.dismiss();
                barcodeDetails = response.body();
                if (barcodeDetails.size() == 0) {
                    Toast.makeText(MainActivity.this, "Barcode not found", Toast.LENGTH_SHORT).show();
                } else {
                    editBarcodeNo.setText(barcodeNo);
                    ((EditText) findViewById(R.id.edit_barcode_weight)).setText(barcodeDetails.get(0).itemwt);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<BarcodeDetails>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage() + "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AddTableDataButton(View view) {
        if (((EditText) findViewById(R.id.edit_barcode_quantity)).getText().toString().isEmpty() ||
                ((EditText) findViewById(R.id.edit_barcode_weight)).getText().toString().isEmpty() ||
                ((EditText) findViewById(R.id.edit_barcode_no)).getText().toString().isEmpty()) {
            Toast.makeText(this, "Barcode Deatails Cannot be empty.", Toast.LENGTH_SHORT).show();
        } else if (!listOfParty.get(selectedPosition).itemId.equals(barcodeDetails.get(0).itemid)  ||
                !listOfParty.get(selectedPosition).itemName.equals(barcodeDetails.get(0).itemname) ||
                !listOfParty.get(selectedPosition).itemcode.equals(barcodeDetails.get(0).itemcode) ||
                !listOfParty.get(selectedPosition).itemgrade.equals(barcodeDetails.get(0).itemgrade)) {
            Toast.makeText(this, "DoNo and Barcode details mismatched.", Toast.LENGTH_SHORT).show();
        } else {
            if (barcodeDetails.get(0).itemtype.equals("COIL")) {
                addDataInTable();
            } else {
                if ((!listOfParty.get(selectedPosition).itemlength.equals(barcodeDetails.get(0).itemlength))) {
                    Toast.makeText(this, "DoNo and Barcode details mismatched.", Toast.LENGTH_SHORT).show();
                } else {
                    addDataInTable();
                }
            }

        }
    }

    private void addDataInTable() {
        spinner.setEnabled(false);
        spinner.setClickable(false);
        barcodeDetails.get(0).itemqty = ((EditText) findViewById(R.id.edit_barcode_quantity)).getText().toString();
        boolean alreadyExist = false;
        for (int i=0; i < addedBarcodeDetails.size(); i++){
            if (addedBarcodeDetails.get(i).boundleno.equals(barcodeDetails.get(0).boundleno)) {
                alreadyExist = true;
                break;
            }
        }
        if (!alreadyExist) {
            addedBarcodeDetails.add(barcodeDetails.get(0));
            loadData(barcodeDetails);
            ((Button) findViewById(R.id.submit_sales_order)).setVisibility(View.VISIBLE);
            ((TableLayout) findViewById(R.id.tableInvoices)).setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Duplicate Entry.", Toast.LENGTH_SHORT).show();
        }
        ((EditText) findViewById(R.id.edit_barcode_weight)).setText("");
        editBarcodeNo.setText("");
        ((EditText) findViewById(R.id.edit_barcode_quantity)).setText("1");
        barcodeDetails = null;
    }

    public void loadData(ArrayList<BarcodeDetails> barcodeDetails) {
        srNo++;
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        TextView tvName = new TextView(this);
        tvName.setPadding(10, 10, 10, 10);
        tvName.setBackgroundColor(Color.parseColor("#F0F7F7"));
        tvName.setText(String.valueOf(srNo));
        tvName.setGravity(Gravity.CENTER_HORIZONTAL);
        tr.addView(tvName);

        TextView tvAgeL = new TextView(this);
        tvAgeL.setText(barcodeDetails.get(0).boundleno);
        tvAgeL.setPadding(10, 10, 10, 10);
        tvAgeL.setBackgroundColor(Color.parseColor("#F0F7F7"));
        tvAgeL.setGravity(Gravity.CENTER_HORIZONTAL);
        tr.addView(tvAgeL);

        TextView tvAgeValue = new TextView(this);
        tvAgeValue.setBackgroundColor(Color.parseColor("#F0F7F7"));
        tvAgeValue.setPadding(10, 10, 10, 10);
        tvAgeValue.setText(barcodeDetails.get(0).itemwt);
        ((EditText) findViewById(R.id.edit_scan_wt_text)).setText(
                String.valueOf(Float.valueOf(((EditText) findViewById(R.id.edit_scan_wt_text)).getText().toString()) +
                        Float.valueOf(barcodeDetails.get(0).itemwt))
        );
        tvAgeValue.setGravity(Gravity.CENTER_HORIZONTAL);
        tr.addView(tvAgeValue);


        TextView tvAgeValue1 = new TextView(this);
        tvAgeValue1.setBackgroundColor(Color.parseColor("#F0F7F7"));
        tvAgeValue1.setPadding(10, 10, 10, 10);
        tvAgeValue1.setText(barcodeDetails.get(0).itemqty);
        tvAgeValue1.setGravity(Gravity.CENTER_HORIZONTAL);
        tr.addView(tvAgeValue1);

        mTableLayout.addView(tr, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedPosition = i;
        ((EditText) findViewById(R.id.edit_item_text)).setText(" ");
        ((EditText) findViewById(R.id.edit_req_wt_text)).setText(listOfParty.get(i).itemwt);
        ((EditText) findViewById(R.id.edit_grade_text)).setText(listOfParty.get(i).itemgrade);
        ((EditText) findViewById(R.id.edit_length_text)).setText(listOfParty.get(i).itemlength);
        //((EditText)findViewById(R.id.edit_scan_wt_text)).setText(listOfParty.get(i).qty);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        ((EditText) findViewById(R.id.edit_party_text)).setText("");
    }

    public void SubmitFinalSalesOrder(View view) {
        SalesOrderModel salesOrderModel = new SalesOrderModel();
        salesOrderModel.dono = (((EditText) findViewById(R.id.edit_vocno_text)).getText().toString());
        salesOrderModel.dODATE = (singleParty.get(0).dotate);
        salesOrderModel.pARTYID = (singleParty.get(0).partyid);
        salesOrderModel.tOTALWT = (Double.valueOf(((EditText) findViewById(R.id.edit_scan_wt_text)).getText().toString()));
        salesOrderModel.transport = singleParty.get(0).transport;
        salesOrderModel.truckNo = singleParty.get(0).truckNo;
        if (singleParty.get(0).gateentry.isEmpty()) {
            salesOrderModel.geno = 0L;
        } else {
            salesOrderModel.geno = Long.parseLong(singleParty.get(0).gateentry);
        }
        salesOrderModel.msg = "";
        ArrayList<DraftsalesOrderInputDetail> draftsalesOrderInputDetails = new ArrayList<>();
        for (int i = 0; i < addedBarcodeDetails.size(); i++) {
            DraftsalesOrderInputDetail draftsalesOrderInputDetail = new DraftsalesOrderInputDetail();
            draftsalesOrderInputDetail.iTEMID = addedBarcodeDetails.get(i).itemid;
            draftsalesOrderInputDetail.bOUNDLENO = addedBarcodeDetails.get(i).boundleno;
            draftsalesOrderInputDetail.iTEMWT = Double.valueOf(addedBarcodeDetails.get(i).itemwt);
            draftsalesOrderInputDetail.rEQWT = Double.valueOf(listOfParty.get(0).itemwt);
            draftsalesOrderInputDetail.qTY = Integer.valueOf(addedBarcodeDetails.get(i).itemqty);
            draftsalesOrderInputDetail.itemlength = addedBarcodeDetails.get(i).itemlength;
            draftsalesOrderInputDetail.itemgrade = addedBarcodeDetails.get(i).itemgrade;
            draftsalesOrderInputDetails.add(draftsalesOrderInputDetail);
        }
        salesOrderModel.draftsalesOrderInputDetails = draftsalesOrderInputDetails;

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();
        Log.e("SalesOrderRequest", salesOrderModel.toString());
        Call<SalesOrderModel> call = service.submitSalesOrder(salesOrderModel);
        call.enqueue(new Callback<SalesOrderModel>() {
            @Override
            public void onResponse(Call<SalesOrderModel> call, Response<SalesOrderModel> response) {
                if (response.body().msg.equals("success")) {
                    progressDialog.dismiss();
                    clearAllFields();
                    ((EditText) findViewById(R.id.edit_dono_text)).setText("");
                    Toast.makeText(MainActivity.this, "Sales Order Submitted", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Server Failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SalesOrderModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage() + "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearAllFields() {
            ((Button) findViewById(R.id.submit_sales_order)).setVisibility(View.INVISIBLE);
            ((TableLayout) findViewById(R.id.tableInvoices)).setVisibility(View.INVISIBLE);
            srNo = 0;
            mTableLayout.removeViews(2, addedBarcodeDetails.size());
            addedBarcodeDetails = new ArrayList<>();
            singleParty = new ArrayList<>();
            listOfParty = new ArrayList<>();
            barcodeDetails = new ArrayList<>();
            ((EditText) findViewById(R.id.edit_scan_wt_text)).setText("0");
            ((EditText) findViewById(R.id.edit_vocno_text)).setText("");
            ((EditText) findViewById(R.id.edit_party_text)).setText("");
            ((EditText) findViewById(R.id.edit_transport_text)).setText("");
            ((EditText) findViewById(R.id.edit_gate_entry_text)).setText("");
            ((EditText) findViewById(R.id.edit_item_text)).setText("");
            ((EditText) findViewById(R.id.edit_grade_text)).setText("");
            ((EditText) findViewById(R.id.edit_length_text)).setText("");
            ((EditText) findViewById(R.id.edit_req_wt_text)).setText("");
            ((EditText) findViewById(R.id.edit_barcode_weight)).setText("");
            ((EditText) findViewById(R.id.edit_truck_no_text)).setText("");
            editBarcodeNo.setText("");
            ((EditText) findViewById(R.id.edit_barcode_quantity)).setText("1");
            spinner.setEnabled(true);
            spinner.setClickable(true);
            spinner.setAdapter(null);
    }
}