package com.aplicativo.marcos.appifood;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.facebook.FacebookSdk.getApplicationContext;


public class HTTPRequestTask extends AsyncTask<Void, Void, String> {

    private String lat;
    private String lng;
    private String forecast;
    private String probability;
    private String icon;

    public AsyncResponse delegate = null;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public HTTPRequestTask(AsyncResponse delegate, String lat, String lng){
        //construtor para receber latitude, longitude e MapsActivity
        this.delegate = delegate;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onPostExecute(String o){
        /*Quando termina a execução da thread, o onPostExecute recebe ela como parâmetro e passa para a interface
        AsyncResponse. Estou sobrescrevendo essa interface no MapsActivity e, sendo assim, consigo mandar o valor
         de resposta da therad para esse activity*/
        delegate.processFinish(o);
    }

    @Override
    protected String doInBackground(Void... params) {
        Context contexto = getApplicationContext();
        //thread para não travar a GUI
        try {
            //Crio o objetivo JSONObject e inicializo
            JSONObject jsonObject = new JSONObject();

            //Chamo a função que recebe como parâmetro o link da API e me retorna um JSONObject
            jsonObject = getJSONObjectFromURL("https://api.darksky.net/forecast/1cc7df62073be7f5c1b866e6251edc" +
                    "c8/" + lat + "," + lng+"?exclude=currently,hourly,flags");

            //Pego o objeto JSON que corresponde a previsão do tempo no local
            forecast = jsonObject.getJSONObject("daily").getString("data");

            //Pego a probabilidade de chuva no local, que segundo a doc da API vai de de 0 a 1
            probability = forecast.split("\\},")[0].split("precipProbability")[1].
                    split(",")[0].split(":")[1];

            /*Pego o icon da previsão do tempo para região
            Valores possíveis de icon, segundo API: clear-day, clear-night, rain, snow, sleet,
             wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night*/
            icon = forecast.split("\\},")[0].split("icon")[1].
                    split(",")[0].split(":")[1].replace("\"", "");

            //Concateno em uma string só para enviar ao MapsActivity
            forecast = probability+":"+icon;

        } catch (IOException e) {
            return "erro api";
        } catch (JSONException e) {
            return "erro json";
        }

        return forecast;

    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        //Faz a requisição a URL passada por parâmetro e retorne um objeto JSON
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        //setReadTimeout e setConnectTimeout trabalham sempre com ms
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
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

