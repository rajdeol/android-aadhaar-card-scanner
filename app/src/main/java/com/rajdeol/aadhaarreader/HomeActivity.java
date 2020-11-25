package com.rajdeol.aadhaarreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kinda.alert.KAlertDialog;
import com.rajdeol.aadhaarreader.utils.AadharCard;
import com.rajdeol.aadhaarreader.utils.DataAttributes;
import com.rajdeol.aadhaarreader.utils.QrCodeException;
import com.rajdeol.aadhaarreader.utils.SecureQrCode;
import com.rajdeol.aadhaarreader.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

public class HomeActivity extends AppCompatActivity {

    // variables to store extracted xml data
    String uid,name,gender,yearOfBirth,careOf,villageTehsil,postOffice,district,state,postCode;
    AadharCard aadharData;

    // UI Elements
    TextView tv_sd_uid,tv_sd_name,tv_sd_gender,tv_sd_yob,tv_sd_co,tv_sd_vtc,tv_sd_po,tv_sd_dist,
            tv_sd_state,tv_sd_pc,tv_cancel_action;
    LinearLayout ll_scanned_data_wrapper,ll_data_wrapper,ll_action_button_wrapper;

    // Storage
    Storage storage;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide the default action bar
        getSupportActionBar().hide();
        setContentView(R.layout.activity_home);

        // init the UI Elements
        tv_sd_uid = (TextView)findViewById(R.id.tv_sd_uid);
        tv_sd_name = (TextView)findViewById(R.id.tv_sd_name);
        tv_sd_gender = (TextView)findViewById(R.id.tv_sd_gender);
        tv_sd_yob = (TextView)findViewById(R.id.tv_sd_yob);
        tv_sd_co = (TextView)findViewById(R.id.tv_sd_co);
        tv_sd_vtc = (TextView)findViewById(R.id.tv_sd_vtc);
        tv_sd_po = (TextView)findViewById(R.id.tv_sd_po);
        tv_sd_dist = (TextView)findViewById(R.id.tv_sd_dist);
        tv_sd_state = (TextView)findViewById(R.id.tv_sd_state);
        tv_sd_pc = (TextView)findViewById(R.id.tv_sd_pc);
        tv_cancel_action = (TextView)findViewById(R.id.tv_cancel_action);

        ll_scanned_data_wrapper = (LinearLayout)findViewById(R.id.ll_scanned_data_wrapper);
        ll_data_wrapper = (LinearLayout)findViewById(R.id.ll_data_wrapper);
        ll_action_button_wrapper = (LinearLayout)findViewById(R.id.ll_action_button_wrapper);

        //init storage
        storage = new Storage(this);
    }

    /**
     * Function to check if user has granted access to camera
     * @return boolean
     */
    public boolean checkCameraPermission (){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            return false;
        }
        return true;
    }

    /**
     * onclick handler for scan new card
     * @param view
     */
    public void scanNow( View view){
        // we need to check if the user has granted the camera permissions
        // otherwise scanner will not work
        if(!checkCameraPermission()){return;}

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a Aadharcard QR Code");
        integrator.setResultDisplayDuration(500);
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();
    }

    /**
     * function handle scan result
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            // process received data
            if(scanContent != null && !scanContent.isEmpty()){
                processScannedData(scanContent);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(),"Scan Cancelled", Toast.LENGTH_SHORT);
                toast.show();
            }

        }else{
            Toast toast = Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * process encoded string received from aadhaar card QR code
     * @param scanData
     */
    protected void processScannedData(String scanData){
        // check if the scanned string is XML
        // This is to support old QR codes

        if(isXml(scanData)){
            XmlPullParserFactory pullParserFactory;

            try {
                // init the parserfactory
                pullParserFactory = XmlPullParserFactory.newInstance();
                // get the parser
                XmlPullParser parser = pullParserFactory.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(scanData));
                aadharData = new AadharCard();

                // parse the XML
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_DOCUMENT) {
                        Log.d("Rajdeol","Start document");
                    } else if(eventType == XmlPullParser.START_TAG && DataAttributes.AADHAAR_DATA_TAG.equals(parser.getName())) {
                        // extract data from tag
                        //uid
                        aadharData.setUuid(parser.getAttributeValue(null,DataAttributes.AADHAR_UID_ATTR));
                        //name
                        aadharData.setName(parser.getAttributeValue(null,DataAttributes.AADHAR_NAME_ATTR));
                        //gender
                        aadharData.setGender(parser.getAttributeValue(null,DataAttributes.AADHAR_GENDER_ATTR));
                        // year of birth
                        aadharData.setDateOfBirth(parser.getAttributeValue(null,DataAttributes.AADHAR_DOB_ATTR));
                        // care of
                        aadharData.setCareOf(parser.getAttributeValue(null,DataAttributes.AADHAR_CO_ATTR));
                        // village Tehsil
                        aadharData.setVtc(parser.getAttributeValue(null,DataAttributes.AADHAR_VTC_ATTR));
                        // Post Office
                        aadharData.setPostOffice(parser.getAttributeValue(null,DataAttributes.AADHAR_PO_ATTR));
                        // district
                        aadharData.setDistrict(parser.getAttributeValue(null,DataAttributes.AADHAR_DIST_ATTR));
                        // state
                        aadharData.setState(parser.getAttributeValue(null,DataAttributes.AADHAR_STATE_ATTR));
                        // Post Code
                        aadharData.setPinCode(parser.getAttributeValue(null,DataAttributes.AADHAR_PC_ATTR));

                    } else if(eventType == XmlPullParser.END_TAG) {
                        Log.d("Rajdeol","End tag "+parser.getName());

                    } else if(eventType == XmlPullParser.TEXT) {
                        Log.d("Rajdeol","Text "+parser.getText());

                    }
                    // update eventType
                    eventType = parser.next();
                }

                // display the data on screen
                displayScannedData();
                return;
            } catch (XmlPullParserException e) {
                showErrorPrompt("Error in processing QRcode XML");
                e.printStackTrace();
                return;
            } catch (IOException e) {
                showErrorPrompt(e.toString());
                e.printStackTrace();
                return;
            }
        }

        // process secure QR code
        processEncodedScannedData(scanData);
    }// EO function

    /**
     * Function to process encoded aadhar data
     * @param scanData
     */
    protected void processEncodedScannedData(String scanData){
        try {
            SecureQrCode decodedData = new SecureQrCode(this,scanData);
            aadharData = decodedData.getScannedAadharCard();
            // display the Aadhar Data
            showSuccessPrompt("Scanned Aadhar Card Successfully");
            displayScannedData();
        } catch (QrCodeException e) {
            showErrorPrompt(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * show scanned information
     */
    public void displayScannedData(){
        ll_data_wrapper.setVisibility(View.GONE);
        ll_scanned_data_wrapper.setVisibility(View.VISIBLE);
        ll_action_button_wrapper.setVisibility(View.VISIBLE);

        // clear old values if any
        tv_sd_uid.setText("");
        tv_sd_name.setText("");
        tv_sd_gender.setText("");
        tv_sd_yob.setText("");
        tv_sd_co.setText("");
        tv_sd_vtc.setText("");
        tv_sd_po.setText("");
        tv_sd_dist.setText("");
        tv_sd_state.setText("");
        tv_sd_pc.setText("");

        // update UI Elements
        tv_sd_uid.setText(aadharData.getUuid());
        tv_sd_name.setText(aadharData.getName());
        tv_sd_gender.setText(aadharData.getGender());
        tv_sd_yob.setText(aadharData.getDateOfBirth());
        tv_sd_co.setText(aadharData.getCareOf());
        tv_sd_vtc.setText(aadharData.getVtc());
        tv_sd_po.setText(aadharData.getPostOffice());
        tv_sd_dist.setText(aadharData.getDistrict());
        tv_sd_state.setText(aadharData.getState());
        tv_sd_pc.setText(aadharData.getPinCode());
    }

    /**
     * display home screen onclick listener for cancel button
     * @param view
     */
    public void showHome(View view){
        ll_data_wrapper.setVisibility(View.VISIBLE);
        ll_scanned_data_wrapper.setVisibility(View.GONE);
        ll_action_button_wrapper.setVisibility(View.GONE);
    }

    /**
     * save data to storage
     */
    public void saveData(View view){
        // We are going to use json to save our data
        // create json object
        JSONObject aadharDataJson = new JSONObject();
        try {
            aadharDataJson.put(DataAttributes.AADHAR_UID_ATTR, aadharData.getUuid());
            aadharDataJson.put(DataAttributes.AADHAR_NAME_ATTR, aadharData.getName());
            aadharDataJson.put(DataAttributes.AADHAR_GENDER_ATTR, aadharData.getGender());
            aadharDataJson.put(DataAttributes.AADHAR_DOB_ATTR, aadharData.getDateOfBirth());
            aadharDataJson.put(DataAttributes.AADHAR_CO_ATTR, aadharData.getCareOf());
            aadharDataJson.put(DataAttributes.AADHAR_VTC_ATTR, aadharData.getVtc());
            aadharDataJson.put(DataAttributes.AADHAR_PO_ATTR, aadharData.getPostOffice());
            aadharDataJson.put(DataAttributes.AADHAR_DIST_ATTR, aadharData.getDistrict());
            aadharDataJson.put(DataAttributes.AADHAR_STATE_ATTR, aadharData.getState());
            aadharDataJson.put(DataAttributes.AADHAR_PC_ATTR, aadharData.getPinCode());
            aadharDataJson.put(DataAttributes.AADHAR_LAND_ATTR, aadharData.getLandmark());
            aadharDataJson.put(DataAttributes.AADHAR_HOUSE_ATTR, aadharData.getHouse());
            aadharDataJson.put(DataAttributes.AADHAR_LOCATION_ATTR, aadharData.getLocation());
            aadharDataJson.put(DataAttributes.AADHAR_STREET_ATTR, aadharData.getStreet());
            aadharDataJson.put(DataAttributes.AADHAR_SUBDIST_ATTR, aadharData.getSubDistrict());
            aadharDataJson.put(DataAttributes.AADHAR_EMAIL_ATTR, aadharData.getEmail());
            aadharDataJson.put(DataAttributes.AADHAR_MOBILE_ATTR, aadharData.getMobile());
            aadharDataJson.put(DataAttributes.AADHAR_SIG_ATTR, aadharData.getSignature());

            // read data from storage
            String storageData = storage.readFromFile();

            JSONArray storageDataArray;
            //check if file is empty
            if(storageData.length() > 0){
                storageDataArray = new JSONArray(storageData);
            }else{
                storageDataArray = new JSONArray();
            }


            // check if storage is empty
            if(storageDataArray.length() > 0){
                // check if data already exists
                for(int i = 0; i<storageDataArray.length();i++){
                    String dataUid = storageDataArray.getJSONObject(i).getString(DataAttributes.AADHAR_UID_ATTR);
                    if(uid.equals(dataUid)){
                        // do not save anything and go back
                        // show home screen
                        tv_cancel_action.performClick();
                        return;
                    }
                }
            }
            // add the aadhaar data
            storageDataArray.put(aadharDataJson);
            // save the aadhaardata
            storage.writeToFile(storageDataArray.toString());

            // show home screen
            tv_cancel_action.performClick();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Function to check if string is xml
     * @param testString
     * @return boolean
     */
    protected boolean isXml (String testString){
        Pattern pattern;
        Matcher matcher;
        boolean retBool = false;

        // REGULAR EXPRESSION TO SEE IF IT AT LEAST STARTS AND ENDS
        // WITH THE SAME ELEMENT
        final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";

        // IF WE HAVE A STRING
        if (testString != null && testString.trim().length() > 0) {

            // IF WE EVEN RESEMBLE XML
            if (testString.trim().startsWith("<")) {

                pattern = Pattern.compile(XML_PATTERN_STR,
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

                // RETURN TRUE IF IT HAS PASSED BOTH TESTS
                matcher = pattern.matcher(testString);
                retBool = matcher.matches();
            }
            // ELSE WE ARE FALSE
        }

        return retBool;
    }

    /**
     * onclick handler for show saved cards
     * this will start the SavedAadhaarCardActivity
     * @param view
     */
    public void showSavedCards(View view){
        // intent for SavedAadhaarcardActivity
        Intent intent = new Intent(this,SavedAadhaarCardActivity.class);
        // Start Activity
        startActivity(intent);
    }

    public void showErrorPrompt(String message){
        new KAlertDialog(this, KAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText(message)
                .show();


    }

    public void showSuccessPrompt(String message){
        new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText(message)
                .show();
    }
}// EO class
