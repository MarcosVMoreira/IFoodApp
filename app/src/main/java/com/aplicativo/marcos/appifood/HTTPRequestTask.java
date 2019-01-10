package com.aplicativo.marcos.appifood;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HTTPRequestTask extends AsyncTask<Void, Void, String> {

    private String lat;
    private String lng;
    private String forecast = "teste";

    public AsyncResponse delegate = null;

    public interface AsyncResponse {
        void processFinish(String output);
    }



    public HTTPRequestTask(AsyncResponse delegate, String lat, String lng){
        this.delegate = delegate;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onPostExecute(String o){
        // your stuff
        delegate.processFinish(o);
    }


    @Override
    protected String doInBackground(Void... params) {


        try {

            //Crio o objetivo JSONObject e inicializo
            JSONObject jsonObject = new JSONObject();

            //Chamo a função que recebe como parâmetro o link da API e me retorna um JSONObject
            jsonObject = getJSONObjectFromURL("https://api.darksky.net/forecast/1cc7df62073be7f5c1b866e6251edc" +
                    "c8/"+lat+","+lng);

            //Pego o objeto JSON que corresponde a previsão do tempo no local
            forecast = jsonObject.getJSONObject("currently").getString("icon");



        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }




        return forecast;

    }

    public String getForecast () {
        return forecast;
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();

        return new JSONObject(jsonString);
    }


}

