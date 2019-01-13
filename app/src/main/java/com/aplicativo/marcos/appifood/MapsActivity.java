package com.aplicativo.marcos.appifood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;



public class MapsActivity extends AppCompatActivity implements HTTPRequestTask.AsyncResponse {

    TextView foodText;
    Button pickPlaceButton, logoutButton;
    private final static int FINE_LOCATION = 100;
    private final static int PLACE_PICKER_REQUEST = 1;
    private LinkedList<String> latLong = new LinkedList<String>();
    private Place place;
    private String nomeUsuario;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Pego o  nome do usuário logado pelo facebook
        Intent intent = getIntent();
        nomeUsuario = intent.getStringExtra("nome");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestPermission();

        foodText = (TextView) findViewById(R.id.foodText);
        pickPlaceButton = (Button) findViewById(R.id.pickPlaceButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);

        pickPlaceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //código básico para montar o PlacePicker
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = builder.build(MapsActivity.this);

                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //System.out.println("Lol");
                LoginManager.getInstance().logOut();
                logoutRedirect();
            }
        });
    }

    private void requestPermission() {
        //verifico se o app tem permissão de acesso a localização precisa. Se não, requesto.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Esse app precisa de permissão de localização por GPS para detectar sua localização!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        /*verifico se deu certo a marcação do usuário de uma localização. Caso deu certo, eu utilizo a localização
        para pegar os valores de latitude e longitude e enviar para a API do darksky.net
        Não utilizei a API do OpenWeather porque ela estava com problemas para requisições de latitude e longitude,
        retornando sempre o mesmo JSON não importa qual localização fosse inserida. Como eu preferi trabalhar com latitude e
        longitude e não nome de cidade, o melhor a ser feito foi procurar outra API que respondesse bem a lat e long.*/
        if (resultCode == RESULT_OK) {

            //pego o objeto do placePicker
            place = PlacePicker.getPlace(this, data);

            //pego a latitude e longitude e salvo em uma linkedlist
            latLong = latLngSplit(place.getLatLng());

            /*Passo a latitude e a longitude que estão na lista como parâmetro pra minha thread que fará consulta a API
            Estou usando thread para não travar a UI enquanto faço a requisição para a API e aguardo resposta
            A resposta dessa thread eu pego no método processFinish, que só me retorna o valor quando o processo
            inteiro da thread termina, para evitar acesso as variáveis dentro da thread em um momento importuno
            (Exemplo: não foi recebido o retorno da API ainda e tento consultar o JSON resposta inexistente*/
            new HTTPRequestTask(this, latLong.get(0), latLong.get(1)).execute();

        } else if (resultCode == RESULT_CANCELED) {
            //Caso o usuário não tenha selecionado um local, aviso ele
            Toast.makeText(getApplicationContext(), "Nenhuma localização selecionada", Toast.LENGTH_LONG).show();

        }
    }

    private LinkedList<String> latLngSplit(LatLng latLng) {

        /*Valor da latitude e longitude voltam em formado complicado de trabalhar. Portanto, separo tais valores
        em duas Strings distintas para facilitar a concatenação na URL da API.*/

        LinkedList<String> list = new LinkedList<String>();
        String latLngString = String.valueOf(latLng);
        String lat;
        String lng;

        lat = latLngString.split("\\(")[1].split(",")[0];
        lng = latLngString.split("\\(")[1].split(",")[1].replace(")", "");

        list.add(lat);
        list.add(lng);

        return list;
    }



    @Override
    public void processFinish(String output){

        //Recebe o valor retornado da thread quando ela termina o processo de consulta a API
        String icon;
        String prob;

        //Divido novamente a prob e icon que vieram concatenados da thread
        prob = output.split(":")[0];
        icon = output.split(":")[1];

        foodText.setText(userResponse(prob, icon));
    }


    public String userResponse (String prob, String icon) {
        String mensage = null;
        Double probDouble;
        int random;


        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println( "hora "+sdf.format(cal.getTime()) );

        //PAREI AQUI TENTANDO PEGAR A HORA DO COMPUTADOR

        //Lógica para sugerir pizza ou sorvete

        probDouble = Double.parseDouble(prob);

        //Caso esteja com mais de 80% de probabilidade de chuva, considero que choverá
        if (probDouble >= 0.8) {
            random = (int) (Math.random() * 6);
            switch (random){
                case 1:
                    return "Pizza 1. "+nomeUsuario;
                case 2:
                    return "Pizza 2.";
                case 3:
                    return "Pizza 3.";
                case 4:
                    return "Pizza 4.";
                case 5:
                    return "Pizza 5.";
                default:
                    return "Pizza default.";
            }
        } else {
           return "Sorvete.";
        }

    }

    private void logoutRedirect() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }



}

