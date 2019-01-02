package com.aplicativo.marcos.appifood;

import android.os.AsyncTask;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTTPRequestTask extends AsyncTask<Void, Void, Void> {



    @Override
    protected Void doInBackground(Void... params) {


        System.out.println("entrei no método");
// Create URL
        URL apiEndpoint = null;
        try {
            apiEndpoint = new URL("https://api.darksky.net/forecast/1cc7df62073be7f5c1b866e6251edcc8/-6.3771962,-36.667511");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

// Create connection
        try {
            System.out.println("criando conexao");
            HttpsURLConnection myConnection =
                    (HttpsURLConnection) apiEndpoint.openConnection();


            if (myConnection.getResponseCode() == 200) {
                System.out.println("conexao funcional");
                // Success
                // Further processing here

                InputStream responseBody = myConnection.getInputStream();

                InputStreamReader responseBodyReader =
                        new InputStreamReader(responseBody, "UTF-8");

                JsonReader jsonReader = new JsonReader(responseBodyReader);

                System.out.println("jsonReader = "+jsonReader);

                jsonReader.beginObject(); // Start processing the JSON object
                while (jsonReader.hasNext()) { // Loop through all keys


                    String key = jsonReader.nextName(); // Fetch the next key

                    System.out.println("key: "+key);


                    if (key.equals("currently")) { // Check if desired key
                        // Fetch the value as a String

                        jsonReader.beginObject();

                        while (jsonReader.hasNext()) {

                            
                            System.out.println("começou "+jsonReader+" terminou");

                        }

                        jsonReader.endObject();

                       /* jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) {
                            final String innerName = jsonReader.nextName();
                            System.out.println("passou");
                            System.out.println("inner "+innerName);
                            //String value = jsonReader.nextString();
                            //System.out.println("CONDICOES DO TEMPO ATUAL: " + value);
                        }*/


                        // Do something with the value
                        // ...

                        break; // Break out of the loop
                    } else {
                        jsonReader.skipValue(); // Skip values of other keys
                    }
                }

                jsonReader.close();
                myConnection.disconnect();

            } else {
                // Error handling code goes here
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

