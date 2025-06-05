package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Livros extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_livros);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // Método para redirecionar para a tela de livros
    public void onClickTimer(View view) {
        Intent intent = new Intent(Livros.this, IndexTimer.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela pomodoro
    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Livros.this, Pomodoro.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela de insights
    public void onClickInsights(View view) {
        Intent intent = new Intent(Livros.this, Insights.class);
        startActivity(intent);
        finish();
    }

    // Método para redirecionar para a tela de agenda
    public void onClickAgenda(View view) {
        Intent intent = new Intent(Livros.this, Agenda.class);
        startActivity(intent);
        finish();
    }
}