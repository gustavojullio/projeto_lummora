package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IniciarSessao extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_iniciar_sessao);

    }

    public void onVoltar(View v) {
        Intent intent = new Intent(IniciarSessao.this, MainActivity.class);
        startActivity(intent);
    }

    public void onEntrar(View v) {
        Intent intent = new Intent(IniciarSessao.this, IndexTimer.class);
        startActivity(intent);
    }

    public void onEsqueceuEmail(View v) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuEmail.class);
        startActivity(intent);
    }

    public void onEsqueceuSenha(View v) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuSenha.class);
        startActivity(intent);
    }
}