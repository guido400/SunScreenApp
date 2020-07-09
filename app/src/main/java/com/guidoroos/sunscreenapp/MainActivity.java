package com.guidoroos.sunscreenapp;



import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends LifecycleLoggingActivity {
    //Tag for logging purpose
    protected final String TAG = getClass().getSimpleName();

    // apikey from secrets resource which is gitignored
    private String apiKey;



   // reference to location text field
    private EditText editLocationText;
    // reference to output text field
    private TextView textOutput;

    // reference to get setlocation button
    private Button setLocationButton;
    // reference to get advice button
    private Button getAdviceButton;

    // save returned coordinates from geocoder
    private String coordinates = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views.
        initializeViews();







    }

    private void initializeViews () {

        //Cache location textfield
        editLocationText = (EditText) findViewById(R.id.loc_text);

        //Cache location textfield
        textOutput = (TextView) findViewById(R.id.text_output);

        //cache setlocation button
        setLocationButton = (Button) findViewById(R.id.set_loc);

        // cache getadvice button
        getAdviceButton = (Button) findViewById(R.id.get_adv);

        //get apikey secret
        apiKey = getString(R.string.weather_api_key);
    }

    // on click method to retrieve a location from text field and query entry via geocoding object
    public void setLocation(View view) {
        String location = editLocationText.getText().toString();
        Log.d(TAG, "location: " + location);

        //check if field is not emtpy
        if (location.trim().length() == 0)  {
            Utils.showToast(this,"No location entered");

        }
        else {
            // run method that will find longitude and latitude via geocoding object
            String coordinates = getCoordinatesFromLocation(location);
            // set coordinates as class variable so it can be assessed from getadvice() method
            this.coordinates = coordinates;
        }

    }


    // when button clicked, use location entered to pass longitude and latitude coordinates to weatherbit api, used to extract uv data
    public void getAdvice(View view) {
        // check if location entered in textbox is empty
        if (coordinates == null || coordinates.trim().length() == 0 ) {
            Utils.showToast(this, "No location set");
            return;
        }
        try {
            Log.d(TAG, "coordinates: " + coordinates);
            // use async task framework to create a background thread to call weatherbit api
            GetWeatherAsync weatherTask = new GetWeatherAsync();
            weatherTask.execute(coordinates);
        } catch (Exception e) {

                Utils.showToast(this, "Failed to get weather data for this location");
            }
        }

    //method to get latitude and longitude of an address

    public String getCoordinatesFromLocation(String location) {

        //geocoder is a class to transform adress of location description into (lat,long) coordinate
        Geocoder coder = new Geocoder(this);
        // geocoder getfrom location method returns a list of Address instances
        List<Address> address;

        try {
            // only interested in the first result that matches
            address = coder.getFromLocationName(location, 1);
            if (address == null) {
                Utils.showToast(this,"Location not found");
                return null;

            }
            //get first (and only) result of geocoder getlocationfromname() method
            Address result = address.get(0);
            //fetch latitude and longitude
            double lat = result.getLatitude();
            double lng = result.getLongitude();

            Utils.showToast(this, "Location set");
            //return coordinates in format that can returned as one return value
            return lat + "," + lng;
        } catch (Exception e) {
            Utils.showToast(this,"Location not found");
            return null;
        }
    }


    // async task inner class used to call weatherbit api and present sun forecast from there in ui
    class GetWeatherAsync extends AsyncTask<String, Void, String> {
        protected final String TAG = getClass().getSimpleName();

        // save value for uv forecast attributes
        private Double uv;
        private String temp;
        private String cityName;
        private String description;
        private String lat;
        private String lon;




        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground() runs");
            //api adress that can be completed with coordinates
            String API_URL = "https://api.weatherbit.io/v2.0/forecast/hourly?";
            String coordinate = urls [0];

            //fetch latitude and longitude from coordinates variable
            try {
                this.lat = coordinate.split(",")[0];
                this.lon = coordinate.split(",")[1];
            }
            catch (Exception E){};

            // create http request to api
            try {
                URL url = new URL(API_URL + "key=" + apiKey + "&lat=" + lat + "&lon=" + lon + "&hours=5");
                //create http connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //read response string
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    Log.d(TAG, "weather result:" + stringBuilder.toString());

                    //return response string
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        // parse api returned json to present in ui
        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute() runs" + response);
            if (!(response == null)) {
                try {
                    // create json object from output and parse it following the structure in the output
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray array = object.getJSONArray("data");

                    //get city name from json object
                    this.cityName = object.getString("city_name");


                    //iterate over forecast for first hour after current time. needed because weatherbit api first value for forecast was found to often lies 1-2 hours in the past.
                    for (int i = 0; i < 5; i++) {
                        // get current data in standard time
                        Date currentTime = Calendar.getInstance().getTime();
                        // get standard time weather forecast time from jsonobject
                        String timeString = array.getJSONObject(i).getString("timestamp_utc");
                        //convert returned standard time to Date
                        Date time = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(timeString);
                        Log.d(TAG, currentTime.toString() +" " + time.toString());


                        // return the first forecast after current time
                        if (time.after (currentTime)) {
                            // return uv index when time from forecast is later than now
                            this.uv = array.getJSONObject(i).getDouble("uv");
                            this.description =  array.getJSONObject(i).getJSONObject("weather").getString("description");
                            this.temp = array.getJSONObject(i).getString("temp");

                            break;
                        }
                    }


                    //formula of knmi.nl for calculating minutes in sun recommended
                    String sunTime;
                    Log.d(TAG, uv.toString());
                    // check for very low uv or null value
                    if (uv != null &&  uv > 0.1) {
                        // 150 is a number for an average skin colour
                        int minutes = (int) Math.round (150.0 / uv);
                        sunTime = Integer.toString ( minutes)+ " minutes"  ;
                    }
                    // check if uv index is so low that there is no risk at all
                    else if (uv < 0.1) {
                        sunTime = "as long as you like!";
                    }

                    //return empty string when uv index is null
                    else {
                        sunTime = "";
                    }

                    // multiline string to present uv forecast on ui screen
                    String outputText = "Location: " + cityName + "\n"
                                    + "Weather: " + description + "\n"
                                    + "Temperature: " + temp + "\n\n"
                                    + "UV Index: " + uv + "\n\n"
                                    + "Advice to be outside without sun protection for:" + " \n"
                                    + sunTime ;

                    //set multine string to screen
                    textOutput.setText (outputText);




                } catch (JSONException | ParseException e) {
                    // Appropriate error handling code
                }



            }
            else Utils.showToast(textOutput.getContext(), "no weather data found for this location");


        }
    }
}

