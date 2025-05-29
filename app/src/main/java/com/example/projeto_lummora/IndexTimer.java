package com.example.projeto_lummora;

import android.os.Bundle;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recycleTimer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Timer> timerList = new ArrayList<>();
        try {
            timerList.add(new Timer(2,23, "Teste 1"));
            timerList.add(new Timer(2,30, "Teste 2"));
            timerList.add(new Timer(2,12, "Teste 3"));
            timerList.add(new Timer(2,13, "Teste 4"));
            timerList.add(new Timer(2,50, "Teste 5"));
        } catch (Exception e) {
            Toast.makeText(this,  "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        TimerAdapter timerAdapter = new TimerAdapter(this, timerList);
        recyclerView.setAdapter(timerAdapter);

    }
}