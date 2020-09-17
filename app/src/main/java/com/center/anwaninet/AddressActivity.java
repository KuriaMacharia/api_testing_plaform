package com.center.anwaninet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class AddressActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final String KEY_DATA = "data";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_ADDRESS_NAME = "name";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_TYPE = "address_type";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ROAD = "road";
    private static final String KEY_REGION = "region";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_SUCCESS = "success";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    String[] category = {"---Select---", "Residence", "Finance","Government Office", "Education", "Hospitals", "Religion","Transport","Other"};

    String[] government ={"---Select---","Police","Huduma Center", "Administration Office","Court","Prison","Government Institution", "Other Address Type"};
    String[] finance ={"---Select---","Business Premises", "Mixed Use", "Offices","Hotel","Lodging","Mall","Petrol Station", "Other Address Type"};
    String[] residence = {"---Select---","Single Residence","Apartment Building","Informal Settlement","Community Setup","Farm","Other Address Type"};
    String[] education = {"---Select---","Pre-school","Nursery School","Primary School","Secondary School","Technical School","College","University","Other Address Type"};
    String[] hospitals ={"---Select---","Public Hospitals", "Private Hospitals","Public Clinic","Private Clinic","Laboratory","Other Address Type"};
    String[] religion = {"---Select---","Church","Mosque","Temple","Other Address Type"};
    String[] transport ={"---Select---","Airport","Train Station","Bus Station","Port","Other Address Type"};
    String[] other = {"---Select---","Other Address Type"};
    String[] selCat= { };

    String fullAddress, addressName, Type, Latitude, Longitude, Road, Region, County, addressCount, roadCount, regionCount,
            mapLat, mapLong, mapAddress, mapName, mapNameClicked, mapType, stla, stlo, selectedType, selectedCategory, Category;
    TextView addressCountTxt, streetCountTxt, regionsCountTxt, mapTxt, listTxt, addressTxt, addressNameTxt;
    ImageView homeImg, terrainImg, flatImg, satelliteImg, zoomInImg, zoomOutImg;
    ConstraintLayout customCons, standardCons, listCons, mapCons, infoCons;
    private ArrayList<HashMap<String, String>> addressList, filteredList;
    ArrayList <String> streetList, regionList;
    ListView listAddress;
    Spinner typeSpin, categorySpin;
    int success;
    private ProgressDialog pDialog;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        addressList = new ArrayList<>();
        filteredList = new ArrayList<>();
        streetList = new ArrayList<>();
        regionList = new ArrayList<>();

        pDialog = new ProgressDialog(AddressActivity.this);
        pDialog.setMessage("Loading. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addressCountTxt=(TextView) findViewById(R.id.txt_address_count);
        streetCountTxt=(TextView) findViewById(R.id.txt_roads_count);
        regionsCountTxt=(TextView) findViewById(R.id.txt_region_count);
        mapTxt=(TextView) findViewById(R.id.txt_map_address);
        listTxt=(TextView) findViewById(R.id.txt_list_map);
        addressTxt=(TextView) findViewById(R.id.txt_address_map);
        addressNameTxt=(TextView) findViewById(R.id.txt_address_name_map);
        homeImg=(ImageView) findViewById(R.id.img_home);
        standardCons=(ConstraintLayout) findViewById(R.id.cons_standard_map);
        customCons=(ConstraintLayout) findViewById(R.id.cons_custom_map);
        mapCons=(ConstraintLayout) findViewById(R.id.cons_map);
        listCons=(ConstraintLayout) findViewById(R.id.cons_list_address);
        infoCons=(ConstraintLayout) findViewById(R.id.cons_info_window);
        listAddress=(ListView) findViewById(R.id.list_addresses);
        typeSpin=(Spinner) findViewById(R.id.spin_type_address);
        categorySpin=(Spinner) findViewById(R.id.spin_category_address);

        terrainImg = (ImageView) findViewById(R.id.img_terrain_map);
        flatImg =(ImageView) findViewById(R.id.img_flat_map);
        satelliteImg = (ImageView) findViewById(R.id.img_sattelite_map);
        zoomInImg =(ImageView) findViewById(R.id.img_zoom_in);
        zoomOutImg = (ImageView) findViewById(R.id.img_zoom_out);

        //typeSpin.setOnItemSelectedListener(this);

        categorySpin.setOnItemSelectedListener(this);
        ArrayAdapter ct = new ArrayAdapter(AddressActivity.this, android.R.layout.simple_spinner_item, category);
        ct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpin.setAdapter(ct);

        new LoadAddresses().execute();

        homeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddressActivity.this, MainActivity.class));
            }
        });

        mapTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTxt.setBackground(getResources().getDrawable(R.color.White));
                listTxt.setBackground(getResources().getDrawable(R.color.tabsBar));
                mapTxt.setTextColor(getResources().getColor(R.color.tabsBar));
                listTxt.setTextColor(getResources().getColor(R.color.White));
                mapCons.setVisibility(View.VISIBLE);
                listCons.setVisibility(View.GONE);

                customCons.setVisibility(View.VISIBLE);
                standardCons.setVisibility(View.GONE);
                SetStandardMarkers();
            }
        });

        listTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTxt.setBackground(getResources().getDrawable(R.color.tabsBar));
                listTxt.setBackground(getResources().getDrawable(R.color.White));
                mapTxt.setTextColor(getResources().getColor(R.color.White));
                listTxt.setTextColor(getResources().getColor(R.color.tabsBar));

                mapCons.setVisibility(View.GONE);
                listCons.setVisibility(View.VISIBLE);

            }
        });

        standardCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customCons.setVisibility(View.VISIBLE);
                standardCons.setVisibility(View.GONE);
                SetStandardMarkers();
            }
        });

        customCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                standardCons.setVisibility(View.VISIBLE);
                customCons.setVisibility(View.GONE);
                SetCustomMarkers();
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

        zoomInImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        zoomOutImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedCategory = String.valueOf(categorySpin.getSelectedItem());

            filteredList.clear();
            streetList.clear();
            regionList.clear();

            if (selectedCategory.contentEquals("---Select---")){
                filteredList.addAll(addressList);
                for (int b = 0; b < addressList.size(); b++) {
                    streetList.add(addressList.get(b).get(KEY_ROAD));
                    regionList.add(addressList.get(b).get(KEY_REGION));
                }

                PopulateList();
            }else{
                for (int a = 0; a < addressList.size(); a++) {
                    if(addressList.get(a).get(KEY_CATEGORY).contentEquals(selectedCategory)){
                        filteredList.add(addressList.get(a));
                        streetList.add(addressList.get(a).get(KEY_ROAD));
                        regionList.add(addressList.get(a).get(KEY_REGION));
                    }
                    PopulateList();
                }
            }

        if(!selectedCategory.isEmpty()){

            if(selectedCategory.contentEquals("Residence")) {
                selCat=residence;
            }else if(selectedCategory.contentEquals("Finance")){
                selCat=finance;
            }else if(selectedCategory.contentEquals("Government Office")){
                selCat=government;
            }else if(selectedCategory.contentEquals("Education")){
                selCat=education;
            }else if(selectedCategory.contentEquals("Hospitals")){
                selCat=hospitals;
            }else if(selectedCategory.contentEquals("Religion")){
                selCat=religion;
            }else if(selectedCategory.contentEquals("Transport")){
                selCat=transport;
            }else if(selectedCategory.contentEquals("Other")){
                selCat=other;
            }else{
            }

            ArrayAdapter scsc = new ArrayAdapter(AddressActivity.this, android.R.layout.simple_spinner_item, selCat);
            scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpin.setAdapter(scsc);

            typeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedType = String.valueOf(typeSpin.getSelectedItem());

                    if(!selectedType.contentEquals("---Select---")) {
                        filteredList.clear();
                        streetList.clear();
                        regionList.clear();

                        if (selectedType.contentEquals("All Types")){
                            filteredList.addAll(addressList);
                            for (int b = 0; b < addressList.size(); b++) {
                                streetList.add(addressList.get(b).get(KEY_ROAD));
                                regionList.add(addressList.get(b).get(KEY_REGION));
                            }

                            PopulateList();
                        }else{
                            for (int a = 0; a < addressList.size(); a++) {
                                if(addressList.get(a).get(KEY_TYPE).contentEquals(selectedType)){
                                    filteredList.add(addressList.get(a));
                                    streetList.add(addressList.get(a).get(KEY_ROAD));
                                    regionList.add(addressList.get(a).get(KEY_REGION));
                                }
                                PopulateList();
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
                        Category = incidence.getString(KEY_CATEGORY);
                        Type = incidence.getString(KEY_TYPE);
                        Latitude = incidence.getString(KEY_LATITUDE);
                        Longitude = incidence.getString(KEY_LONGITUDE);
                        Road = incidence.getString(KEY_ROAD);
                        Region = incidence.getString(KEY_REGION);
                        County = incidence.getString(KEY_COUNTY);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ADDRESS, fullAddress);
                        map.put(KEY_ADDRESS_NAME, addressName);
                        map.put(KEY_CATEGORY, Category);
                        map.put(KEY_TYPE, Type);
                        map.put(KEY_LATITUDE, Latitude);
                        map.put(KEY_LONGITUDE, Longitude);
                        map.put(KEY_COUNTY, County);
                        map.put(KEY_REGION, Region);
                        map.put(KEY_ROAD, Road);
                        addressList.add(map);

                        streetList.add(Road);
                        regionList.add(Region);
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
                        filteredList.addAll(addressList);

                        LoadStats();
                        SetStandardMarkers();
                        PopulateList();
                        pDialog.dismiss();
                    } else {
                        Toast.makeText(AddressActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }
            });
        }
    }

    private void PopulateList(){
        addressCountTxt.setText(String.valueOf(filteredList.size()));

        ListAdapter adapter = new SimpleAdapter(
                AddressActivity.this, filteredList, R.layout.address_item,
                new String[]{KEY_ADDRESS, KEY_LATITUDE, KEY_LONGITUDE, KEY_ADDRESS_NAME, KEY_TYPE},
                new int[]{R.id.txt_address_number, R.id.txt_latitude_item, R.id.txt_longitude_item,
                        R.id.txt_address_name_item, R.id.txt_address_type_item});

        listAddress.setAdapter(adapter);
    }

    private void SetCustomMarkers(){
            mMap.clear();
            Rect rectangle = new Rect(0,0,30,30);
            Rect rectangle5 = new Rect(0,0,30,30);

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(25, 25, conf);
            Canvas canvas1 = new Canvas(bmp);
            canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.apartmment), null, rectangle, null);
    // paint defines the text color, stroke width and size
            Paint color = new Paint();
            color.setTextSize(35);
            color.setColor(Color.BLACK);

    // modify canvas
            //canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.apartmment), 30,30, color);
            //canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.apartmment), null, rectangle, null);
            //canvas1.drawText("U", 30, 40, color);
            //apartment, single, informal, community, school, police, lodging, mixed use, other


            Bitmap.Config conf2 = Bitmap.Config.ARGB_8888;
            Bitmap bmp2 = Bitmap.createBitmap(25, 25, conf2);
            Canvas canvas2 = new Canvas(bmp2);
            canvas2.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.single_residence_3), null, rectangle, null);

            Bitmap.Config conf3 = Bitmap.Config.ARGB_8888;
            Bitmap bmp3 = Bitmap.createBitmap(25, 25, conf3);
            Canvas canvas3 = new Canvas(bmp3);
            canvas3.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.police1), null, rectangle5, null);

            Bitmap.Config conf4 = Bitmap.Config.ARGB_8888;
            Bitmap bmp4 = Bitmap.createBitmap(25, 25, conf4);
            Canvas canvas4 = new Canvas(bmp4);
            canvas4.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.informal), null, rectangle5, null);

            Bitmap.Config conf6 = Bitmap.Config.ARGB_8888;
            Bitmap bmp6 = Bitmap.createBitmap(25, 25, conf6);
            Canvas canvas6 = new Canvas(bmp6);
            canvas6.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.school), null, rectangle5, null);

            Bitmap.Config conf7 = Bitmap.Config.ARGB_8888;
            Bitmap bmp7 = Bitmap.createBitmap(25, 25, conf7);
            Canvas canvas7 = new Canvas(bmp7);
            canvas7.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.apartment4), null, rectangle5, null);

            Bitmap.Config conf8 = Bitmap.Config.ARGB_8888;
            Bitmap bmp8 = Bitmap.createBitmap(25, 25, conf8);
            Canvas canvas8 = new Canvas(bmp8);
            canvas8.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.lodging), null, rectangle5, null);

            Bitmap.Config conf5 = Bitmap.Config.ARGB_8888;
            Bitmap bmp5 = Bitmap.createBitmap(25, 25, conf5);
            Canvas canvas5 = new Canvas(bmp5);
            canvas5.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.single_residence_3), null, rectangle, null);

            Bitmap.Config conf9 = Bitmap.Config.ARGB_8888;
            Bitmap bmp9 = Bitmap.createBitmap(25, 25, conf9);
            Canvas canvas9 = new Canvas(bmp9);
            canvas9.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.church), null, rectangle5, null);

            Bitmap.Config conf11 = Bitmap.Config.ARGB_8888;
            Bitmap bmp11 = Bitmap.createBitmap(25, 25, conf11);
            Canvas canvas11 = new Canvas(bmp11);
            canvas11.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.roadside_shop), null, rectangle5, null);

            Bitmap.Config conf10 = Bitmap.Config.ARGB_8888;
            Bitmap bmp10 = Bitmap.createBitmap(25, 25, conf10);
            Canvas canvas10 = new Canvas(bmp10);
            canvas10.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.marker), null, rectangle5, null);

            if (filteredList.size()>0){

                for (int i = 0; i < filteredList.size(); i++) {

                    mapLong = filteredList.get(i).get(KEY_LONGITUDE);
                    mapLat = filteredList.get(i).get(KEY_LATITUDE);
                    mapAddress = filteredList.get(i).get(KEY_ADDRESS);
                    mapName = filteredList.get(i).get(KEY_ADDRESS_NAME);
                    if (!mapName.contentEquals("No Name")){
                        mapNameClicked=mapName;
                    }else{
                        mapNameClicked="";
                    }
                    mapType = filteredList.get(i).get(KEY_TYPE);

                    Double stlong = Double.parseDouble(mapLong);
                    Double stlat = (Double.parseDouble(mapLat));
                    stla = filteredList.get(0).get(KEY_LATITUDE);
                    stlo = filteredList.get(0).get(KEY_LONGITUDE);

                    Double stlat2 = (Double.parseDouble(stla));
                    Double stlong2 = Double.parseDouble(stlo);
                    LatLng building = new LatLng(stlat, stlong);

                    if(mapType.contentEquals("Apartment Building")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else if(mapType.contentEquals("Single Residence")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp2))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else if(mapType.contentEquals("Police")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp3))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else if(mapType.contentEquals("informal Settlement")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp4))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else if(mapType.contentEquals("Community Setup")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp5))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    } else if(mapType.contentEquals("Pre-school")||mapType.contentEquals("Nursery School")||
                            mapType.contentEquals("Primary School")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp6))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else if(mapType.contentEquals("Mixed Use")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp7))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    } else if(mapType.contentEquals("Lodging")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp8))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    } else if(mapType.contentEquals("Church")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp9))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else if(mapType.contentEquals("Business Premises")){
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp11))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));

                    }else{
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bmp10))
                                .position(building).title(mapNameClicked + ":   " + mapAddress));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(building));
                    }

                    Log.e("PlaceLL", mapNameClicked + ":   " + mapAddress);

                    LatLng start = new LatLng(stlat2, stlong2);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
                }
            }
    }

    private void SetStandardMarkers(){
        mMap.clear();

        if (filteredList.size()>0){

            for (int i = 0; i < filteredList.size(); i++) {

                mapLong = filteredList.get(i).get(KEY_LONGITUDE);
                mapLat = filteredList.get(i).get(KEY_LATITUDE);
                mapAddress = filteredList.get(i).get(KEY_ADDRESS);
                mapName = filteredList.get(i).get(KEY_ADDRESS_NAME);
                if (!mapName.contentEquals("No Name")){
                    mapNameClicked=mapName;
                }else{
                    mapNameClicked="";
                }
                mapType = filteredList.get(i).get(KEY_TYPE);

                Double stlong = Double.parseDouble(mapLong);
                Double stlat = (Double.parseDouble(mapLat));
                stla = filteredList.get(0).get(KEY_LATITUDE);
                stlo = filteredList.get(0).get(KEY_LONGITUDE);

                Double stlat2 = (Double.parseDouble(stla));
                Double stlong2 = Double.parseDouble(stlo);
                LatLng building = new LatLng(stlat, stlong);

                mMap.addMarker(new MarkerOptions().position(building).title(mapNameClicked + ":   " + mapAddress));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(building));
                Log.e("PlaceLL", mapNameClicked + ":   " + mapAddress);

                LatLng start = new LatLng(stlat2, stlong2);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
            }
        }
    }

    private void LoadStats(){
        Set<String> set = new HashSet<>(streetList);
        streetList.clear();
        streetList.addAll(set);
        Set<String> setR = new HashSet<>(regionList);
        regionList.clear();
        regionList.addAll(setR);

        streetCountTxt.setText(String.valueOf(streetList.size()));
        regionsCountTxt.setText(String.valueOf(regionList.size()));
    }
}
