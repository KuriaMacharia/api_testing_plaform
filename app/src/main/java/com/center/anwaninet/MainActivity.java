package com.center.anwaninet;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.center.agiza.MyView;
import com.center.emergency.Emergency;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout addressCons, apiCons, streetCons, emergencyCons;
    Emergency eView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_more_vert_white_24dp);
        toolbar.setOverflowIcon(drawable);

        addressCons=(ConstraintLayout) findViewById(R.id.cons_address_manager);
        apiCons=(ConstraintLayout) findViewById(R.id.cons_api_manager);
        streetCons=(ConstraintLayout) findViewById(R.id.cons_street_manager);
        emergencyCons=(ConstraintLayout) findViewById(R.id.cons_emergency_api);

        addressCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddressActivity.class));
            }
        });

        apiCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this, AccessActivity.class));
                View v = new MyView(MainActivity.this);
                setContentView(v);
            }
        });

        streetCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, StreetActivity.class));
            }
        });

        emergencyCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eView=new Emergency(MainActivity.this);
                eView.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
}
