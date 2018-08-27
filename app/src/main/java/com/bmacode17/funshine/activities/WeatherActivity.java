package com.bmacode17.funshine.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bmacode17.funshine.R;
import com.bmacode17.funshine.models.DailyWeatherReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity {

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORDINATE = "/?lat=";       //  "/?lat=9.9687&lon=76.299";
    final String URL_UNIT = "&units=metric";
    final String URL_API_KEY = "&APPID=971a6cea59f1074dd5e137135f279944";
    private static final String TAG = "Basel";

    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<DailyWeatherReport> weatherReportList;

    private ImageView imageView_weatherTypeMini , imageView_weatherType;
    private TextView textView_date , textView_weatherType , textView_cityAndCountry , textView_temp , textView_tempMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        imageView_weatherTypeMini = (ImageView) findViewById(R.id.imageView_weatherTypeMini);
        imageView_weatherType = (ImageView) findViewById(R.id.imageView_weatherType);
        textView_cityAndCountry = (TextView) findViewById(R.id.textView_cityAndCountry);
        textView_date = (TextView) findViewById(R.id.textView_date);
        textView_weatherType = (TextView) findViewById(R.id.textView_weatherType);
        textView_temp = (TextView) findViewById(R.id.textView_temp);
        textView_tempMin = (TextView) findViewById(R.id.textView_tempMin);

        weatherReportList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            getDeviceLocation();
        } else {
            checkLocationPermission();
        }
    }

    private void getDeviceLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            @SuppressLint("MissingPermission")
            Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: Location is found !");
                        Location currentLocation = (Location) task.getResult();
                        Log.d(TAG, "onComplete: Location: " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
                        downloadWeatherData(currentLocation);
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    } else {
                        Log.d(TAG, "onComplete : Current location is null! ");
                        Toast.makeText(WeatherActivity.this, "Unable to get current location !", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException ex) {
            Log.d(TAG, "GetDeviceLocation : SecurityException: " + ex.getMessage());
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(WeatherActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation();
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void downloadWeatherData(Location location){

        //  "/?lat=9.9687&lon=76.299";

        String NEW_URL_COORDINATE = URL_COORDINATE + location.getLatitude() + "&lon=" + location.getLongitude();
        final String URL = URL_BASE + NEW_URL_COORDINATE + URL_UNIT + URL_API_KEY;
        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response){
                try{

                    JSONObject cityObject = response.getJSONObject("city");
                    String city = cityObject.getString("name");
                    String country = cityObject.getString("country");
                    JSONArray listArray = response.getJSONArray("list");
                    for (int i = 0; i < 5; i++) {
                        JSONObject object = listArray.getJSONObject(i);
                        JSONObject mainObject = object.getJSONObject("main");
                        Double temp = mainObject.getDouble("temp");
                        Double tempMin = mainObject.getDouble("temp_min");
                        Double tempMax = mainObject.getDouble("temp_max");

                        JSONArray weatherArray = object.getJSONArray("weather");
                        JSONObject weatherObject = weatherArray.getJSONObject(0);
                        String weatherType = weatherObject.getString("main");
                        String rawDate = object.getString("dt_txt");

                        DailyWeatherReport report = new DailyWeatherReport(city,country,temp.intValue(),tempMin.intValue(),tempMax.intValue(),weatherType,rawDate);
                        Log.d(TAG, "onResponse: The current weather type is :" + report.getWeatherType());
                        weatherReportList.add(report);
                    }
            }catch(Exception ex){
                Log.d(TAG, "onErrorResponse: " + ex.getLocalizedMessage());
            }
            updateUi();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getLocalizedMessage());
            }
        });
        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void updateUi(){

        if(weatherReportList.size()>0){
            DailyWeatherReport report = weatherReportList.get(1);
            switch (report.getWeatherType()){
                case "Rain":
                    imageView_weatherType.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.rainy, null));
                    imageView_weatherTypeMini.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.rainy , null));
                    break;
                case "Snow":
                    imageView_weatherType.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.snow, null));
                    imageView_weatherTypeMini.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.snow, null));
                    break;
                case "Clouds":
                    imageView_weatherType.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cloudy, null));
                    imageView_weatherTypeMini.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cloudy, null));
                    break;
                case "Clear":
                    imageView_weatherType.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sunny_mini, null));
                    imageView_weatherTypeMini.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sunny_mini, null));
                    break;
            }

            textView_date.setText(report.getRawDate());
            textView_cityAndCountry.setText(report.getCityName() + " , " + report.getCountry());
            textView_temp.setText(Integer.toString(report.getTempMax())+"°");
            textView_tempMin.setText(Integer.toString(report.getTemp())+"°");
            textView_weatherType.setText(report.getWeatherType());
        }
    }
}

