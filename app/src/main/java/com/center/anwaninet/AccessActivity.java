package com.center.anwaninet;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.center.anwaninet.Helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccessActivity extends AppCompatActivity {

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_ORGANISATION_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_API_TYPE = "type";
    private static final String KEY_DATA = "data";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    EditText nameEdt, emailEdt;
    Spinner typeSpin;
    TextView nameTxt, emailTxt, typeTxt;
    Button registerBtn, generateBtn;

    String Type, orgName, orgEmail, orgType, Email;
    int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        nameEdt = (EditText) findViewById(R.id.edt_organisation_api);
        emailEdt = (EditText) findViewById(R.id.edt_email_api);
        typeSpin = (Spinner) findViewById(R.id.spin_type_api);
        nameTxt=(TextView)findViewById(R.id.txt_name_api);
        emailTxt=(TextView)findViewById(R.id.txt_email_api);
        typeTxt=(TextView)findViewById(R.id.txt_type_api);
        registerBtn=(Button)findViewById(R.id.btn_register_api);
        generateBtn=(Button)findViewById(R.id.btn_generate_api);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orgName=nameEdt.getText().toString();
                Email=emailEdt.getText().toString();
                new RegisterClient().execute();
            }
        });
    }

    private class RegisterClient extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(AccessActivity.this, R.style.mydialog);
            pDialog.setMessage("Registering. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();

            httpParams.put(KEY_ORGANISATION_NAME, nameEdt.getText().toString());
            httpParams.put(KEY_EMAIL, emailEdt.getText().toString());
            httpParams.put(KEY_API_TYPE, Type);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_client.php", "POST", httpParams);
            if(success==1)
                try {
                    success = jsonObject.getInt(KEY_SUCCESS);

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    pDialog.dismiss();
                    if (success == 0) {
                        Toast.makeText(AccessActivity.this,"Address Verification Successful",Toast.LENGTH_LONG).show();
                        new FetchClient().execute();

                    } else {
                        Toast.makeText(AccessActivity.this,"Address Verification Failed",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class FetchClient extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(AccessActivity.this, R.style.mydialog);
            pDialog.setMessage("Registering. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_ORGANISATION_NAME, orgName);
            httpParams.put(KEY_EMAIL, Email);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_client.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject businesses;
                if (success == 1) {
                    businesses = jsonObject.getJSONObject(KEY_DATA);

                    orgName = businesses.getString(KEY_ORGANISATION_NAME);
                    orgType = businesses.getString(KEY_API_TYPE);
                    orgEmail = businesses.getString(KEY_EMAIL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        nameTxt.setText(orgName);
                        emailTxt.setText(orgEmail);
                        typeTxt.setText(orgType);
                    } else {

                    }
                }
            });
        }
    }
}
