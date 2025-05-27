package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CriarConta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_criar_conta);

    }

    public void onVoltar(View v) {
        Intent intent = new Intent(CriarConta.this, MainActivity.class);
        startActivity(intent);
    }

    public void onCriar(View v) {
        Intent intent = new Intent(CriarConta.this, IniciarSessao.class);
        startActivity(intent);
    }
}