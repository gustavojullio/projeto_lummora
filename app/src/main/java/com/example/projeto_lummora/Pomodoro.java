package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Pomodoro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pomodoro);

    }
    // Método para redirecionar para a tela timer
    public void onClickTimer(View view) {
        Intent intent = new Intent(Pomodoro.this, IndexTimer.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela de livros
    public void onClickLivros(View view) {
        Intent intent = new Intent(Pomodoro.this, Livros.class);
        startActivity(intent);
        finish();
    }


    // Método para redirecionar para a tela de insights
    public void onClickInsights(View view) {
        Intent intent = new Intent(Pomodoro.this, Insights.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela de agenda
    public void onClickAgenda(View view) {
        Intent intent = new Intent(Pomodoro.this, Agenda.class);
        startActivity(intent);
        finish();
    }


}