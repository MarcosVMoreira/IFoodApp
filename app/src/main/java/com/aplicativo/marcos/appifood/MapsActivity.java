package com.aplicativo.marcos.appifood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
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
import java.util.LinkedList;

public class MapsActivity extends AppCompatActivity implements HTTPRequestTask.AsyncResponse {

    TextView foodText;
    ImageView imageView;
    CardView pickPlaceButton, logoutCard;
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
        pickPlaceButton = findViewById(R.id.pickPlaceCard);
        logoutCard = findViewById(R.id.logoutCard);

        imageView = (ImageView) findViewById(R.id.imageView);

        pickPlaceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Código básico para montar o PlacePicker do Google
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = builder.build(MapsActivity.this);

                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    //caso Google Play Services não esteja instalado, atualizado ou ativo
                    Toast.makeText(getApplicationContext(), "Erro ao conectar ao Google Places", Toast.LENGTH_LONG).show();
                    ////e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getApplicationContext(), "Google Places está atualmente indisponivel", Toast.LENGTH_LONG).show();
                    //e.printStackTrace();
                }

            }
        });

        logoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Se botão de logout for clickado, desloga
                LoginManager.getInstance().logOut();
                logoutRedirect();
            }
        });
    }

    private void requestPermission() {
        //Verifico se o app tem permissão de acesso a localização precisa. Se não, requesto.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Se não tiver permissão, eu peço permissão
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
        /*Se for retornada a expressão "erro api", quer dizer que não consegui estabelecer conexão com a mesma
          pode ser problema da API estar indisponível ou o dispositivo estar sem conexão com internet*/
        if (output.equals("erro api")) {
            Toast.makeText(getApplicationContext(), "Erro ao fazer requisição à API. Verifique conexão com internet.", Toast.LENGTH_LONG).show();
        } else if (output.equals("erro json")) {
            //
            Toast.makeText(getApplicationContext(), "Erro ao fazer conversão JSON", Toast.LENGTH_LONG).show();
        } else {
            //Recebe o valor retornado da thread quando ela termina o processo de consulta a API
            String icon;
            String prob;

            //Divido novamente a prob e icon que vieram concatenados da thread
            prob = output.split(":")[0];
            icon = output.split(":")[1];

            foodText.setText(userResponse(prob, icon));
        }


    }


    public String userResponse (String prob, String icon) {

        String mensage = "", estagioDoDia;
        Double probDouble;
        int random;

        //Busco se é manha, tarde, noite ou madrugada
        estagioDoDia = estagioDoDia();

        //transformo a probabilidade de chuva em double pra ficar mais fácil comparar
        probDouble = Double.parseDouble(prob);

        //verifico qual é o estágio do dia
        if (estagioDoDia == "manha") {
            mensage = "Eaí, "+nomeUsuario+". Bom dia. ";
        } else if (estagioDoDia == "tarde") {
            mensage = "Olá, "+nomeUsuario+". Boa tarde. ";
        } else if (estagioDoDia == "noite") {
            mensage = "Oi, "+nomeUsuario+". Como vai a noite? ";
        } else {
            mensage ="Oi, "+nomeUsuario+". ";
        }

        //Caso esteja com mais de 70% de probabilidade de chuva, considero que choverá
        if (probDouble >= 0.7) {
            //Se choverá, indico pizza

            imageView.setImageResource(R.drawable.pizza_sem_fundo);

            random = (int) (Math.random() * 5);

            switch (random){
                case 0:
                    return mensage+"Com essa chuva, uma pizza de brocolis seria o ideal, não acha? Afinal, " +
                            "não podemos sair da dieta, e brocolis está na dieta, não é mesmo?";
                case 1:
                    return mensage+"Parece que hoje vai chover. Para não pegar chuva, pede uma pizza pelo iFood!";
                case 2:
                    return mensage+"Chuva combina com o que? Isso mesmo: pizza. Acho que você devia pedir uma de Pepperoni, hem? Só pedir pelo iFood.";
                case 3:
                    return mensage+"Calabresa ou frango com catupiry, qual sabor você prefere? Hoje é dia de pizza e, se é pizza, é pelo iFood!";
                case 4:
                    if (estagioDoDia == "manha") {
                        return mensage+"Que tal comer uma pizza no almoço? Com a chuva de hoje, seria uma boa pedida!";
                    } else {
                        return mensage+"Netflix and chill hoje a noite? Não sem antes pedir uma pizza portuguesa. O iFood tem várias" +
                                " pizzarias a sua disposição!";
                    }
                default:
                    return mensage+"Nessa chuva, que tal pedir uma pizza de bacon?";
            }
        } else {
            //Caso não chova, indico sorvete/gelados

            imageView.setImageResource(R.drawable.sorvete_sem_fundo);

            random = (int) (Math.random() * 5);

            switch (random){
                case 0:
                    if (icon == "clear-day") {
                        return mensage+"Esse calor está pedindo um sorvete de creme, não é mesmo? Acho que é uma ótima pedida." +
                                " O iFood conta com as melhores sorveterias a sua disposição!";
                    } else if (icon == "cloudy" || icon == "partly-cloudy-day" || icon == "partly-cloudy-night") {
                        return mensage+"Que tal uma paleta mexicana nesse tempo nublado? E não se preocupe em sair de casa: o iFood leva " +
                                "a paleta até você!";
                    }
                case 1:
                    return mensage+"Um sorvete na casquinha cairia bem hoje, não acha? Morango com creme é uma ótima combinação.";
                case 2:
                    return mensage+"Que tal pedir um açaí? O iFood tem várias opções para você.";
                case 3:
                    return mensage+"Que tal um geladinho, sacolé ou chup-chup? Não importa o nome, hoje seria uma boa pedida!";
                case 4:
                    if (estagioDoDia == "manha") {
                        return mensage+"Hummmm, um sorvete cairia bem hoje, não acha? Peça um pelo iFood.";
                    } else {
                        return mensage+"Sorvete a noite, porque não? Nesse calor, não deixa de ser uma ótima ideia. Peça um sorvete pelo iFood" +
                                " no conforto da sua casa!";
                    }
                default:
                    return mensage+"Sem previsão de chuva para hoje. Aproveita e pede um sorvete no iFood!";
            }
        }
    }

    private void logoutRedirect() {
        //Redireciona para o main, onde é feito o logout no início do código Main
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    private String estagioDoDia () {
        String estagioDoDia = null;

        //pego data e horário atual
        Calendar cal = Calendar.getInstance();

        //utilizo SDF para pegar o horário atual
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        //CompareTo se for menor retorna valor menor que zero e se for maior retorna valor maior que 0.
        //Se for igual, retorna 0
        if ((sdf.format(cal.getTime()).compareTo("00:00") >= 0) && (sdf.format(cal.getTime()).compareTo("06:00") < 0)) {
            estagioDoDia = "madrugada";
        } else if ((sdf.format(cal.getTime()).compareTo("06:00") >= 0) && (sdf.format(cal.getTime()).compareTo("12:00") < 0)) {
            estagioDoDia = "manha";
        } else if ((sdf.format(cal.getTime()).compareTo("12:00") >= 0) && (sdf.format(cal.getTime()).compareTo("18:00") < 0)) {
            estagioDoDia = "tarde";
        } else {
            estagioDoDia = "noite";
        }

        return estagioDoDia;
    }
}

