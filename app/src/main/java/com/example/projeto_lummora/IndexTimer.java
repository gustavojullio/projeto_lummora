package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class IndexTimer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_index_timer);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        RecyclerView recyclerView = findViewById(R.id.recycleTimer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Timer> timerList = new ArrayList<>();
        try {
            timerList.add(new Timer(2,2, "Teste 1"));
            timerList.add(new Timer(2,2, "Teste 2"));
            timerList.add(new Timer(2,2, "Teste 3"));
            timerList.add(new Timer(2,2, "Teste 4"));
            timerList.add(new Timer(2,2, "Teste 5"));
        } catch (Exception e) {
            Toast.makeText(this,  "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        TimerAdapter timerAdapter = new TimerAdapter(this, timerList);
        recyclerView.setAdapter(timerAdapter);

    }

    // Método para redirecionar para a tela de livros
    public void onClickLivros(View view) {
        Intent intent = new Intent(IndexTimer.this, Livros.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    // Método para redirecionar para a tela pomodoro
    public void onClickPomodoro(View view) {
        Intent intent = new Intent(IndexTimer.this, Pomodoro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    // Método para redirecionar para a tela de insights
    public void onClickInsights(View view) {
        Intent intent = new Intent(IndexTimer.this, Insights.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    // Método para redirecionar para a tela de agenda
    public void onClickAgenda(View view) {
        Intent intent = new Intent(IndexTimer.this, Agenda.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}