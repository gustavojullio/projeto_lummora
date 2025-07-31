package com.example.projeto_lummora;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Agenda extends AppCompatActivity {

    private TextView txtDataAtual;

    GestureDetector gestureDetector;
    private RecyclerView recyclerView;
    private AgendaAdapter adapter;
    private List<Tarefa> listaDeTarefas = new ArrayList<>();
    private List<Object> itensAgrupados = new ArrayList<>();

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Tarefas").child(user.getUid());

        txtDataAtual = findViewById(R.id.dataAtual);

        setupRecyclerView();
        loadTarefasFromFirebase();
        setupGestureDetector();

        ImageButton btnAdd = findViewById(R.id.imageButton);
        btnAdd.setOnClickListener(v -> showAddEditTarefaDialog(null));

        exibirDataAtual();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewAgenda);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgendaAdapter(this, itensAgrupados, tarefa -> showEditDeleteDialog(tarefa));
        recyclerView.setAdapter(adapter);
    }

    private void loadTarefasFromFirebase() {
        databaseReference.orderByChild("dataTimestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaDeTarefas.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Tarefa tarefa = dataSnapshot.getValue(Tarefa.class);
                    if (tarefa != null) {
                        listaDeTarefas.add(tarefa);
                    }
                }
                processarEAgruparTarefas();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Agenda.this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processarEAgruparTarefas() {
        itensAgrupados.clear();
        if (listaDeTarefas.isEmpty()) return;

        // Usar TreeMap para agrupar tarefas por mês/ano e manter a ordem
        Map<String, List<Tarefa>> tarefasPorMes = new TreeMap<>();
        SimpleDateFormat formatadorMesAno = new SimpleDateFormat("MMMM / yy", new Locale("pt", "BR"));

        for (Tarefa tarefa : listaDeTarefas) {
            String chaveMesAno = formatadorMesAno.format(new Date(tarefa.getDataTimestamp()));
            if (!tarefasPorMes.containsKey(chaveMesAno)) {
                tarefasPorMes.put(chaveMesAno, new ArrayList<>());
            }
            tarefasPorMes.get(chaveMesAno).add(tarefa);
        }

        // Monta a lista final com cabeçalhos de mês e tarefas
        for (Map.Entry<String, List<Tarefa>> entry : tarefasPorMes.entrySet()) {
            itensAgrupados.add(entry.getKey().toUpperCase()); // Adiciona o cabeçalho "MAIO / 25"
            itensAgrupados.addAll(entry.getValue()); // Adiciona as tarefas do mês
        }
    }

    private void showAddEditTarefaDialog(final Tarefa tarefa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_tarefa, null);
        builder.setView(dialogView);

        final EditText edtNomeTarefa = dialogView.findViewById(R.id.edtNomeTarefa);
        final RadioGroup radioGroupTipo = dialogView.findViewById(R.id.radioGroupTipo);
        final RadioButton radioProva = dialogView.findViewById(R.id.radioProva);
        final RadioButton radioTrabalho = dialogView.findViewById(R.id.radioTrabalho);
        final RadioButton radioTarefa = dialogView.findViewById(R.id.radioTarefa);
        final TextView txtDataSelecionada = dialogView.findViewById(R.id.txtDataSelecionada);

        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat formatadorData = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());

        if (tarefa != null) {
            builder.setTitle("Alterar Tarefa");
            edtNomeTarefa.setText(tarefa.getNome());
            calendar.setTimeInMillis(tarefa.getDataTimestamp());
            switch (tarefa.getTipo()) {
                case "Prova": radioProva.setChecked(true); break;
                case "Trabalho": radioTrabalho.setChecked(true); break;
                case "Tarefa": radioTarefa.setChecked(true); break;
            }
        } else {
            builder.setTitle("Adicionar Nova Tarefa");
            radioTarefa.setChecked(true); // Default
        }

        txtDataSelecionada.setText(formatadorData.format(calendar.getTime()));

        txtDataSelecionada.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(Agenda.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        txtDataSelecionada.setText(formatadorData.format(calendar.getTime()));
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = edtNomeTarefa.getText().toString().trim();
            if (nome.isEmpty()) {
                Toast.makeText(this, "O nome da tarefa é obrigatório.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedRadioId = radioGroupTipo.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = radioGroupTipo.findViewById(selectedRadioId);
            String tipo = selectedRadioButton.getText().toString();

            if (tarefa == null) { // Criando nova tarefa
                String id = databaseReference.push().getKey();
                Tarefa novaTarefa = new Tarefa();
                novaTarefa.setId(id);
                novaTarefa.setNome(nome);
                novaTarefa.setTipo(tipo);
                novaTarefa.setDataTimestamp(calendar.getTimeInMillis());
                databaseReference.child(id).setValue(novaTarefa);
            } else { // Editando tarefa existente
                tarefa.setNome(nome);
                tarefa.setTipo(tipo);
                tarefa.setDataTimestamp(calendar.getTimeInMillis());
                databaseReference.child(tarefa.getId()).setValue(tarefa);
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void showEditDeleteDialog(final Tarefa tarefa) {
        new AlertDialog.Builder(this)
                .setTitle("Escolha uma ação")
                .setItems(new String[]{"Editar", "Excluir"}, (dialog, which) -> {
                    if (which == 0) { // Editar
                        showAddEditTarefaDialog(tarefa);
                    } else { // Excluir
                        new AlertDialog.Builder(this)
                                .setTitle("Excluir Tarefa")
                                .setMessage("Tem certeza que deseja excluir '" + tarefa.getNome() + "'?")
                                .setPositiveButton("Sim", (d, w) -> databaseReference.child(tarefa.getId()).removeValue())
                                .setNegativeButton("Não", null)
                                .show();
                    }
                })
                .show();
    }


    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e2.getX() - e1.getX()) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(e2.getX() - e1.getX()) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (e2.getX() > e1.getX()) {
                            startActivity(new Intent(Agenda.this, Insights.class));
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public void onClickTimer(View view) {
        Intent intent = new Intent(Agenda.this, IndexTimer.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickLivros(View view) {
        Intent intent = new Intent(Agenda.this, Livros.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickInsights(View view) {
        Intent intent = new Intent(Agenda.this, Insights.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Agenda.this, Pomodoro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickPerson(View view) {
        Intent intent = new Intent(Agenda.this, ConfiguracoesUsuario.class);
        startActivity(intent);
    }

    private void exibirDataAtual() {
        SimpleDateFormat formatadorData = new SimpleDateFormat("E, dd/MM", new Locale("pt", "BR"));
        String dataFormatada = formatadorData.format(new Date());
        dataFormatada = dataFormatada.substring(0, 1).toUpperCase() + dataFormatada.substring(1).replace(".", "");
        txtDataAtual.setText(dataFormatada);
    }
}