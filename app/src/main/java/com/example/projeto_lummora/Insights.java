package com.example.projeto_lummora;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class Insights extends AppCompatActivity {

    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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

        pieChart = findViewById(R.id.graficoMaterias);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(1, 1, 1, 1);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.YInMn_Blue));
        pieChart.setTransparentCircleColor(ContextCompat.getColor(this, R.color.YInMn_Blue));
        pieChart.setTouchEnabled(false);

        ArrayList<PieEntry> yValues = new ArrayList<>();

        yValues.add(new PieEntry(100f,"Matematica"));
        yValues.add(new PieEntry(50f, "Biologia"));
        yValues.add(new PieEntry(30f, "Portugues"));
        yValues.add(new PieEntry(20f, "Geografia"));
        yValues.add(new PieEntry(40f, "Historia"));
        yValues.add(new PieEntry(10f, "Redação"));

        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(Cores.CORES_GRAFICO);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14);
        data.setValueTextColor(Color.WHITE);

        data.setDrawValues(false);
        pieChart.setDrawEntryLabels(false);

        pieChart.setData(data);

        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        pieChart.getLegend().setTextSize(14);
        pieChart.getLegend().setTextColor(Color.WHITE);
        pieChart.getLegend().setDrawInside(false);
    }


    // Método para redirecionar para a tela de timer
    public void onClickTimer(View view) {
        Intent intent = new Intent(Insights.this, IndexTimer.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    // Método para redirecionar para a tela de livros
    public void onClickLivros(View view) {
        Intent intent = new Intent(Insights.this, Livros.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    // Método para redirecionar para a tela pomodoro
    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Insights.this, Pomodoro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    // Método para redirecionar para a tela de agenda
    public void onClickAgenda(View view) {
        Intent intent = new Intent(Insights.this, Agenda.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

}