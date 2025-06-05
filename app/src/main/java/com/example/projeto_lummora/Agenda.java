package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Agenda extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agenda);

    }



    // Método para redirecionar para a tela timer
    public void onClickTimer(View view) {
        Intent intent = new Intent(Agenda.this, IndexTimer.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela de livros
    public void onClickLivros(View view) {
        Intent intent = new Intent(Agenda.this, Livros.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela de pomodoro
    public void onClickInsights(View view) {
        Intent intent = new Intent(Agenda.this, Insights.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela pomodoro
    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Agenda.this, Pomodoro.class);
        startActivity(intent);
        finish();
    }

}