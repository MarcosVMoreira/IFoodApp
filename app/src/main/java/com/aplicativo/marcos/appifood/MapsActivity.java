package com.aplicativo.marcos.appifood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;


public class MapsActivity extends AppCompatActivity implements HTTPRequestTask.AsyncResponse {

    TextView placeName;
    TextView placeAddress;
    Button pickPlaceButton;
    private final static int FINE_LOCATION = 100;
    private final static int PLACE_PICKER_REQUEST = 1;
    private LinkedList<String> latLong = new LinkedList<String>();
    Place place;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestPermission();

        placeName = (TextView) findViewById(R.id.placeName);
        placeAddress = (TextView) findViewById(R.id.placeAddress);
        pickPlaceButton = (Button) findViewById(R.id.pickPlaceButton);
        pickPlaceButton.setOnClickListener(new View.OnClickListener() {

//Add a click handler that’ll start the place picker//

            @Override
            public void onClick(View view) {

//Use PlacePicker.IntentBuilder() to construct an Intent//

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = builder.build(MapsActivity.this);

//Create a PLACE_PICKER_REQUEST constant that we’ll use to obtain the selected place//]

                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void requestPermission() {

//Check whether our app has the fine location permission, and request it if necessary//
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
            }
        }
    }

//Handle the result of the permission request//

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to detect your location!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

//Retrieve the results from the place picker dialog//

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

//If the resultCode is OK...//

        if (resultCode == RESULT_OK) {

//...then retrieve the Place object, using PlacePicker.getPlace()//

            place = PlacePicker.getPlace(this, data);

//Extract the place’s name and display it in the TextView//

            //placeName.setText(place.getName());

//Extract the place’s address, and display it in the TextView//

            //placeAddress.setText(place.getAddress());

            latLong = splitaLatLng(place.getLatLng());

            new HTTPRequestTask(this, latLong.get(0), latLong.get(1)).execute();

//If the user exited the dialog without selecting a place...//

        } else if (resultCode == RESULT_CANCELED) {

//...then display the following toast//
            Toast.makeText(getApplicationContext(), "No place selected", Toast.LENGTH_LONG).show();

        }
    }

    private LinkedList<String> splitaLatLng(LatLng latLng) {
        LinkedList<String> lista = new LinkedList<String>();
        String latLngString = String.valueOf(latLng);
        String latitude;
        String longitude;

        latitude = latLngString.split("\\(")[1].split(",")[0];
        longitude = latLngString.split("\\(")[1].split(",")[1].replace(")", "");

        lista.add(latitude);
        lista.add(longitude);

        return lista;
    }



    @Override
    public void processFinish(String output){
        //Recebe o valor retornado da thread quando ela terminar seu processo
        placeAddress.setText(output);
        System.out.println("resultado aqui dentro: "+output);
    }
}

