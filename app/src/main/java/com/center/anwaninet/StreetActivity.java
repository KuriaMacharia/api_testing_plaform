package com.center.anwaninet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.center.anwaninet.Helper.HttpJsonParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StreetActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener{

    private static final String KEY_DATA = "data";

    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ROAD = "road";
    private static final String KEY_REGION = "region";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_SUCCESS = "success";

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_ADDRESS_NAME = "name";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_TYPE = "address_type";

    private static final String BASE_URL = "http://www.anwani.net/seya/";
    String[] region = {"---Select---", "Ruaka", "Kiambu Town", "CBD"};

    ListView listStreet;
    TextView countTxt;
    ImageView homeImg, terrainImg, flatImg, satelliteImg;
    private GoogleMap mMap;
    int success;
    String fullAddress, addressName, Type, Latitude, Longitude, Road, Region, County,
            mapLat, mapLong, mapAddress, mapName, mapNameClicked, mapType, stla, stlo;
    private ArrayList<HashMap<String, String>> streetList, filteredList, addressList, computedList;
    ArrayList <String> roadList, countList, regionList;
    private ProgressDialog pDialog;
    Spinner regionSpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street);

        streetList = new ArrayList<>();
        filteredList = new ArrayList<>();
        roadList = new ArrayList<>();
        addressList = new ArrayList<>();
        countList = new ArrayList<>();
        computedList = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_street);
        mapFragment.getMapAsync(this);

        pDialog = new ProgressDialog(StreetActivity.this);
        pDialog.setMessage("Loading. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        homeImg=(ImageView) findViewById(R.id.img_home);
        terrainImg = (ImageView) findViewById(R.id.img_terrain_map);
        flatImg =(ImageView) findViewById(R.id.img_flat_map);
        satelliteImg = (ImageView) findViewById(R.id.img_sattelite_map);
        listStreet=(ListView) findViewById(R.id.list_streets);
        countTxt=(TextView) findViewById(R.id.txt_street_count);
        regionSpin=(Spinner) findViewById(R.id.spin_region);

        regionSpin.setOnItemSelectedListener(this);
        ArrayAdapter ct = new ArrayAdapter(StreetActivity.this, android.R.layout.simple_spinner_item, region);
        ct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpin.setAdapter(ct);

        new LoadStreet().execute();

        homeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StreetActivity.this, MainActivity.class));
            }
        });

        terrainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });

        flatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        satelliteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-1.289238, 36.820442);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Start"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private class LoadStreet extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_streets.php", "GET", null);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        Latitude = incidence.getString(KEY_LATITUDE);
                        Longitude = incidence.getString(KEY_LONGITUDE);
                        Road = incidence.getString(KEY_ROAD);
                        Region = incidence.getString(KEY_REGION);
                        County = incidence.getString(KEY_COUNTY);

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(KEY_LATITUDE, Latitude);
                        map.put(KEY_LONGITUDE, Longitude);
                        map.put(KEY_COUNTY, County);
                        map.put(KEY_REGION, Region);
                        map.put(KEY_ROAD, Road);
                        streetList.add(map);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        filteredList.addAll(streetList);
                        new LoadAddresses().execute();
                        SetStandardMarkers();

                        pDialog.dismiss();
                    } else {
                        Toast.makeText(StreetActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }
            });
        }
    }

    private class LoadAddresses extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_addresses.php", "GET", null);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        fullAddress = incidence.getString(KEY_ADDRESS);
                        addressName = incidence.getString(KEY_ADDRESS_NAME);
                        Type = incidence.getString(KEY_TYPE);
                        Latitude = incidence.getString(KEY_LATITUDE);
                        Longitude = incidence.getString(KEY_LONGITUDE);
                        Road = incidence.getString(KEY_ROAD);
                        Region = incidence.getString(KEY_REGION);
                        County = incidence.getString(KEY_COUNTY);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ADDRESS, fullAddress);
                        map.put(KEY_ADDRESS_NAME, addressName);
                        map.put(KEY_TYPE, Type);
                        map.put(KEY_LATITUDE, Latitude);
                        map.put(KEY_LONGITUDE, Longitude);
                        map.put(KEY_COUNTY, County);
                        map.put(KEY_REGION, Region);
                        map.put(KEY_ROAD, Road);
                        addressList.add(map);
                        roadList.add(Road);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        Set<String> set = new HashSet<>(roadList);
                        countList.addAll(set);
                        AnalyzeCount();
                    } else {
                        Toast.makeText(StreetActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }
            });
        }
    }

    private void AnalyzeCount(){
        countTxt.setText(String.valueOf(filteredList.size()));
        if(countList.size()>0){
            for (int a=0; a< countList.size(); a++){

                String myRo = countList.get(a);
                String myCount = String.valueOf(Collections.frequency(roadList, myRo));

                HashMap<String, String> map2 = new HashMap<String, String>();
                map2.put("count", myCount);
                map2.put("road", myRo);
                computedList.add(map2);
            }
            PopulateList();
        }
    }

    private void PopulateList(){
        class MapComparator implements Comparator<Map<String, String>> {
            private final String key;

            public MapComparator(String key){
                this.key = key;
            }

            public int compare(Map<String, String> first,
                               Map<String, String> second){
                // TODO: Null checking, both for maps and values
                String firstValue = first.get(key);
                String secondValue = second.get(key);
                return firstValue.compareTo(secondValue);
            }
        }
        Collections.sort(computedList, new MapComparator("road"));

        ListAdapter adapter = new SimpleAdapter(
                StreetActivity.this, computedList, R.layout.street_item,
                new String[]{"count", "road"},
                new int[]{R.id.txt_address_count, R.id.txt_address_number});
        listStreet.setAdapter(adapter);
    }

    private void SetStandardMarkers(){
        mMap.clear();

        if (filteredList.size()>0){

            for (int i = 0; i < filteredList.size(); i++) {

                mapLong = filteredList.get(i).get(KEY_LONGITUDE);
                mapLat = filteredList.get(i).get(KEY_LATITUDE);
                mapAddress = filteredList.get(i).get(KEY_ROAD);

                Double stlong = Double.parseDouble(mapLong);
                Double stlat = (Double.parseDouble(mapLat));
                stla = filteredList.get(0).get(KEY_LATITUDE);
                stlo = filteredList.get(0).get(KEY_LONGITUDE);

                Double stlat2 = (Double.parseDouble(stla));
                Double stlong2 = Double.parseDouble(stlo);
                LatLng building = new LatLng(stlat, stlong);

                mMap.addMarker(new MarkerOptions().position(building).title(mapAddress));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(building));
                Log.e("PlaceLL", mapAddress);

                LatLng start = new LatLng(stlat2, stlong2);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
