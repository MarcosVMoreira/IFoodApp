package com.aplicativo.marcos.appifood;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main extends AppCompatActivity {

    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
    com.facebook.AccessToken userToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        textView = (TextView) findViewById(R.id.textViewLogin);
        callbackManager = CallbackManager.Factory.create();

        //Verifica se o usuário já está logado na conta do facebook. Se estiver, deslogo o usuário
        if (isLoggedIn()) {
            LoginManager.getInstance().logOut();
        }

        //Prepara o botão de login do facebook
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {
               /* System.out.println("acess token: " + loginResult.getAccessToken() + " LOOOOOOL " + loginResult.getAccessToken().getUserId()
                        + " LOOOOOOL " + loginResult.getAccessToken().getToken());*/
                userToken = loginResult.getAccessToken();

                /*Faço a requisição para a Graph do facebook para coletar o nome do usuário logado pelo facebook
                e passo o nome para o MapsActivity*/
                GraphRequest request = GraphRequest.newMeRequest(
                        userToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    openActivity2(object.getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login cancelado", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Houve um erro ao realizar login", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void openActivity2(String nomeUsuario) {
        Intent intent = new Intent(this, MapsActivity.class);
        //Adiciono no intent o nome do usuário logado com facebook
        intent.putExtra("nome", nomeUsuario);
        startActivity(intent);
    }

    public boolean isLoggedIn() {
        //Verifica se o usuário está logado
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }




}
