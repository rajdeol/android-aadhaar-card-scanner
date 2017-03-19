package com.rajdeol.aadhaarreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rajdeol.aadhaarreader.utils.CardListAdapter;
import com.rajdeol.aadhaarreader.utils.DataAttributes;
import com.rajdeol.aadhaarreader.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SavedAadhaarCardActivity extends AppCompatActivity {
    private ListView lv_saved_card_list;
    private TextView tv_no_saved_card;
    private Storage storage;
    private JSONArray storageDataArray;
    private ArrayList<JSONObject> cardDataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //hide the default action bar
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_aadhaar_card);

        //init UI elements
        tv_no_saved_card = (TextView)findViewById(R.id.tv_no_saved_card);
        lv_saved_card_list = (ListView)findViewById(R.id.lv_saved_card_list);

        // init storage
        storage = new Storage(this);

        // read data from storage
        String storageData = storage.readFromFile();

        //check if file is not empty
        if(storageData.length() > 0){
            try {
                // convert JSON string to array
                storageDataArray = new JSONArray(storageData);

                // handle case of empty JSONArray after delete
                if(storageDataArray.length()<1){
                    // hide list and show message
                    tv_no_saved_card.setVisibility(View.VISIBLE);
                    lv_saved_card_list.setVisibility(View.GONE);
                    //exit
                    return;
                }

                // init data list
                cardDataList = new <JSONObject>ArrayList();

                //prepare the data list for list adapter
                for(int i = 0; i<storageDataArray.length();i++){
                    JSONObject dataObject = storageDataArray.getJSONObject(i);
                    cardDataList.add(dataObject);
                }

                // create List Adapter with data
                ArrayAdapter<ArrayList> savedCardListAdapter = new CardListAdapter(this,cardDataList);
                // populate list
                lv_saved_card_list.setAdapter(savedCardListAdapter);

            }catch (JSONException e){
                e.printStackTrace();
            }
        }else{
            // hide list and show message
            tv_no_saved_card.setVisibility(View.VISIBLE);
            lv_saved_card_list.setVisibility(View.GONE);
        }
    }

    /**
     * delete saved aadhaar card
     * @param uid
     */
    public void deleteCard(String uid){
        // read data from storage
        String storageData = storage.readFromFile();

        JSONArray storageDataArray;
        //check if file is empty
        if(storageData.length() > 0){
            try {
                storageDataArray = new JSONArray(storageData);
                // coz I am working on Android version which doesnot support remove method on JSONArray
                JSONArray updatedStorageDataArray = new JSONArray();

                // check if data already exists
                for(int i = 0; i<storageDataArray.length();i++){
                    String dataUid = storageDataArray.getJSONObject(i).getString(DataAttributes.AADHAR_UID_ATTR);
                    if(!uid.equals(dataUid)){
                        updatedStorageDataArray.put(storageDataArray.getJSONObject(i));
                    }
                }

                // save the updated list
                storage.writeToFile(updatedStorageDataArray.toString());

                // Hide the list if all cards are deleted
                if(updatedStorageDataArray.length() < 1){
                    // hide list and show message
                    tv_no_saved_card.setVisibility(View.VISIBLE);
                    lv_saved_card_list.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    /**
     * Start Home Activity
     * @param view
     */
    public void showHome(View view){
        // intent for HomeActivity
        Intent intent = new Intent(this,HomeActivity.class);
        // Start Activity
        startActivity(intent);

    }
}//EO class
