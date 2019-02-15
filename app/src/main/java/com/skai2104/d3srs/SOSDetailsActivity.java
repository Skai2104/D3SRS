package com.skai2104.d3srs;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SOSDetailsActivity extends AppCompatActivity {
    private TextView mNotificationTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosdetails);

        mNotificationTV = findViewById(R.id.notificationTV);

        String dataMessage = getIntent().getStringExtra("message");
        String dataFrom = getIntent().getStringExtra("from_user");
        String latitudeStr = getIntent().getStringExtra("latitude");
        String longitudeStr = getIntent().getStringExtra("longitude");

        double latitude = 0.0, longitude = 0.0;
        if (latitudeStr != null)
            latitude = Double.valueOf(latitudeStr);

        if (longitudeStr != null)
            longitude = Double.valueOf(longitudeStr);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = new ArrayList<>();
        String address = "";
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null) {
                Address returnedAddress = addressList.get(0);
                StringBuilder returnedAddressStr = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    returnedAddressStr.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = returnedAddressStr.toString();
            } else {
                Log.d("Current address error", "No address returned");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GeocoderException", e.getMessage());
        }


        mNotificationTV.setText(address);
    }
}
