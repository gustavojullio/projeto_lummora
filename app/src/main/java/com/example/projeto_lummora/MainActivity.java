package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


    }

    public void onIniciar(View v) {
        Intent intent = new Intent(MainActivity.this, IniciarSessao.class);
        startActivity(intent);
    }

    public void onCadastrar(View v){
        Intent intent = new Intent(MainActivity.this, CriarConta.class);
        startActivity(intent);
    }
}