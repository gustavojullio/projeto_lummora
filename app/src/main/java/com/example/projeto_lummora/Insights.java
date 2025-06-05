package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Insights extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insights);


    }

    // Método para redirecionar para a tela de timer
    public void onClickTimer(View view) {
        Intent intent = new Intent(Insights.this, IndexTimer.class);
        startActivity(intent);
        finish();
    }


    // Método para redirecionar para a tela de livros
    public void onClickLivros(View view) {
        Intent intent = new Intent(Insights.this, Livros.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela pomodoro
    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Insights.this, Pomodoro.class);
        startActivity(intent);
        finish();
    }


    // Método para redirecionar para a tela de agenda
    public void onClickAgenda(View view) {
        Intent intent = new Intent(Insights.this, Pomodoro.class);
        startActivity(intent);
        finish();
    }
}