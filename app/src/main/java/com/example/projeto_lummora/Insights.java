package com.example.projeto_lummora;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Insights extends AppCompatActivity {

    private PieChart graficoMaterias, graficoLivros;
    private BarChart graficoMediaSemanal;
    private TextView txtTempoTotalGeral, txtMediaDiaria;

    private List<Disciplina> listaDisciplinas = new ArrayList<>();
    private List<Livro> listaLivros = new ArrayList<>();

    private GestureDetector gestureDetector;
    private int dataSourcesLoaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );



        graficoMaterias = findViewById(R.id.graficoMaterias);
        graficoLivros = findViewById(R.id.graficoLivros);
        graficoMediaSemanal = findViewById(R.id.graficoMediaSemanal);
        txtTempoTotalGeral = findViewById(R.id.txtTempoTotalGeral);
        txtMediaDiaria = findViewById(R.id.txtMediaDiaria);


        loadAllDataFromFirebase();
    }

    private void loadAllDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Lidar com usuário não logado
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();
        DatabaseReference dbDisciplinas = FirebaseDatabase.getInstance().getReference("Disciplinas").child(userId);
        DatabaseReference dbLivros = FirebaseDatabase.getInstance().getReference("Livros").child(userId);

        // Listener para Disciplinas
        dbDisciplinas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaDisciplinas.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Disciplina d = ds.getValue(Disciplina.class);
                    if (d != null && d.getTempoTotalSegundos() > 0) {
                        listaDisciplinas.add(d);
                    }
                }
                checkIfAllDataIsLoaded();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkIfAllDataIsLoaded();
            }
        });


        dbLivros.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaLivros.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Livro l = ds.getValue(Livro.class);
                    if (l != null && l.getTempoTotalSegundos() > 0) {
                        listaLivros.add(l);
                    }
                }
                checkIfAllDataIsLoaded();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkIfAllDataIsLoaded();
            }
        });
    }

    private synchronized void checkIfAllDataIsLoaded() {
        dataSourcesLoaded++;
        // Quando os 2 listeners (disciplinas e livros) terminarem, processa os dados.
        if (dataSourcesLoaded == 2) {
            processarTodosOsDados();
        }
    }

    private void processarTodosOsDados() {
        if (listaDisciplinas.isEmpty() && listaLivros.isEmpty()) {
            Toast.makeText(this, "Nenhum dado de estudo para exibir.", Toast.LENGTH_SHORT).show();
            return;
        }

        calcularEExibirTotais();
        configurarGraficoDisciplinas();
        configurarGraficoLivros();
        configurarGraficoMediaSemanal();
    }

    private void calcularEExibirTotais() {
        long tempoTotalGeralSegundos = 0;
        Set<String> diasUnicos = new HashSet<>();

        for (Disciplina d : listaDisciplinas) {
            tempoTotalGeralSegundos += d.getTempoTotalSegundos();
            if (d.getHistoricoDiario() != null) {
                diasUnicos.addAll(d.getHistoricoDiario().keySet());
            }
        }
        for (Livro l : listaLivros) {
            tempoTotalGeralSegundos += l.getTempoTotalSegundos();
            if (l.getHistoricoDiario() != null) {
                diasUnicos.addAll(l.getHistoricoDiario().keySet());
            }
        }

        // Exibe o tempo total
        txtTempoTotalGeral.setText(formatarTempoHHMMSS(tempoTotalGeralSegundos));

        // Calcula e exibe a média diária
        if (!diasUnicos.isEmpty()) {
            long mediaDiariaSegundos = tempoTotalGeralSegundos / diasUnicos.size();
            txtMediaDiaria.setText(formatarTempoHHMMSS(mediaDiariaSegundos));
        } else {
            txtMediaDiaria.setText("00:00:00");
        }
    }

    private void configurarGraficoDisciplinas() {
        if(listaDisciplinas.isEmpty()) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Disciplina d : listaDisciplinas) {
            entries.add(new PieEntry(d.getTempoTotalSegundos(), d.getTitulo()));
        }
        configurarPieChart(graficoMaterias, entries, "Disciplinas");
    }

    private void configurarGraficoLivros() {
        if(listaLivros.isEmpty()) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Livro l : listaLivros) {
            entries.add(new PieEntry(l.getTempoTotalSegundos(), l.getTitulo()));
        }
        configurarPieChart(graficoLivros, entries, "Livros");
    }

    private void configurarGraficoMediaSemanal() {
        long[] tempoPorDiaSemana = new long[7]; // 0=Dom, 1=Seg, ..., 6=Sab

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Processa disciplinas
        for (Disciplina d : listaDisciplinas) {
            if (d.getHistoricoDiario() != null) {
                d.getHistoricoDiario().forEach((dataStr, segundos) -> {
                    try {
                        Date data = sdf.parse(dataStr);
                        cal.setTime(data);
                        int diaDaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.DAY_OF_WEEK é 1-7
                        tempoPorDiaSemana[diaDaSemana] += segundos;
                    } catch (ParseException e) { e.printStackTrace(); }
                });
            }
        }
        // Processa livros
        for (Livro l : listaLivros) {
            if (l.getHistoricoDiario() != null) {
                l.getHistoricoDiario().forEach((dataStr, segundos) -> {
                    try {
                        Date data = sdf.parse(dataStr);
                        cal.setTime(data);
                        int diaDaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        tempoPorDiaSemana[diaDaSemana] += segundos;
                    } catch (ParseException e) { e.printStackTrace(); }
                });
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        // Reorganiza para começar na Segunda (índice 0 do gráfico)
        entries.add(new BarEntry(0, tempoPorDiaSemana[1] / 60f)); // Seg
        entries.add(new BarEntry(1, tempoPorDiaSemana[2] / 60f)); // Ter
        entries.add(new BarEntry(2, tempoPorDiaSemana[3] / 60f)); // Qua
        entries.add(new BarEntry(3, tempoPorDiaSemana[4] / 60f)); // Qui
        entries.add(new BarEntry(4, tempoPorDiaSemana[5] / 60f)); // Sex
        entries.add(new BarEntry(5, tempoPorDiaSemana[6] / 60f)); // Sab
        entries.add(new BarEntry(6, tempoPorDiaSemana[0] / 60f)); // Dom

        BarDataSet dataSet = new BarDataSet(entries, "Minutos por dia");
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);

        BarData barData = new BarData(dataSet);
        graficoMediaSemanal.setData(barData);

        // Estilização do gráfico de barras
        graficoMediaSemanal.getDescription().setEnabled(false);
        graficoMediaSemanal.getLegend().setEnabled(false);
        graficoMediaSemanal.setDrawValueAboveBar(true);
        graficoMediaSemanal.getXAxis().setValueFormatter(new IndexAxisValueFormatter(Arrays.asList("Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom")));
        graficoMediaSemanal.getXAxis().setTextColor(Color.WHITE);
        graficoMediaSemanal.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        graficoMediaSemanal.getAxisLeft().setTextColor(Color.WHITE);
        graficoMediaSemanal.getAxisRight().setEnabled(false);
        graficoMediaSemanal.invalidate(); // refresh
    }

    private void configurarPieChart(PieChart pieChart, ArrayList<PieEntry> entries, String label) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.YInMn_Blue));
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setEntryLabelColor(Color.BLACK);

        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(Cores.CORES_GRAFICO); // Usando suas cores customizadas

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.getLegend().setEnabled(false);
        pieChart.invalidate(); // refresh
    }

    private String formatarTempoHHMMSS(long totalSegundos) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(totalSegundos),
                TimeUnit.SECONDS.toMinutes(totalSegundos) % 60,
                totalSegundos % 60);
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getX() - e1.getX() > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // Swipe para a direita (vai para Pomodoro)
                    startActivity(new Intent(Insights.this, Pomodoro.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (e1.getX() - e2.getX() > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // Swipe para a esquerda (vai para Agenda)
                    startActivity(new Intent(Insights.this, Agenda.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onClickTimer(View view) {
        Intent intent = new Intent(Insights.this, IndexTimer.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickLivros(View view) {
        Intent intent = new Intent(Insights.this, Livros.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Insights.this, Pomodoro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickAgenda(View view) {
        Intent intent = new Intent(Insights.this, Agenda.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void onClickPerson(View view) {
        Intent intent = new Intent(Insights.this, ConfiguracoesUsuario.class);
        startActivity(intent);
    }
}