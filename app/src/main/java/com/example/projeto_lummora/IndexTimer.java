package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class IndexTimer extends AppCompatActivity {

    GestureDetector gestureDetector;
    private RecyclerView recyclerView;
    private TimerAdapter timerAdapter;
    private List<Disciplina> disciplinaList;
    private TextView txtDataAtual;
    private TextView txtTempoTotal;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    // Variáveis para controlar o timer
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private Disciplina activeDisciplina = null;
    private int activeDisciplinaPosition = -1;
    private long startTimeMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        txtDataAtual = findViewById(R.id.textView2);
        txtTempoTotal = findViewById(R.id.textView14);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Se não houver usuário logado, volta para a tela de login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Referência para o nó do usuário no Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Disciplinas").child(currentUser.getUid());

        setupRecyclerView();
        loadDisciplinasFromFirebase();
        setupGestureDetector();

        // Configura o botão para adicionar nova disciplina
        ImageButton addDisciplinaButton = findViewById(R.id.imageButton4);
        addDisciplinaButton.setOnClickListener(v -> showAddEditDialog(null));

        exibirDataAtual();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycleTimer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        disciplinaList = new ArrayList<>();
        timerAdapter = new TimerAdapter(this, disciplinaList);
        recyclerView.setAdapter(timerAdapter);

        timerAdapter.setOnDisciplinaClickListener(new TimerAdapter.OnDisciplinaClickListener() {
            @Override
            public void onItemClick(Disciplina disciplina) {
                // Se um timer está rodando na disciplina clicada, o clique deve ser ignorado
                if (activeDisciplina != null && activeDisciplina.getId().equals(disciplina.getId())) {
                    return;
                }
                showEditDeleteDialog(disciplina);
            }

            @Override
            public void onPlayClick(Disciplina disciplina, int position) {
                handlePlayPause(disciplina, position);
            }
        });
    }

    private void loadDisciplinasFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                disciplinaList.clear();
                long tempoTotalGeral = 0; // Variável para somar o tempo

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Disciplina disciplina = dataSnapshot.getValue(Disciplina.class);
                    if (disciplina != null) {
                        disciplinaList.add(disciplina);
                        tempoTotalGeral += disciplina.getTempoTotalSegundos(); // Soma o tempo de cada disciplina
                    }
                }

                // Formata o tempo total e exibe no TextView do cabeçalho
                String tempoTotalFormatado = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        TimeUnit.SECONDS.toHours(tempoTotalGeral),
                        TimeUnit.SECONDS.toMinutes(tempoTotalGeral) % 60,
                        tempoTotalGeral % 60);
                txtTempoTotal.setText(tempoTotalFormatado);

                timerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(IndexTimer.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePlayPause(Disciplina disciplina, int position) {
        // Se um timer já estiver rodando
        if (activeDisciplina != null) {
            // E for o mesmo item, então pause
            if (activeDisciplina.getId().equals(disciplina.getId())) {
                stopActiveTimer();
            } else { // Se for um item diferente, pare o antigo e inicie o novo
                stopActiveTimer();
                startTimer(disciplina, position);
            }
        } else { // Se nenhum timer estiver rodando, inicie
            startTimer(disciplina, position);
        }
    }

    private void startTimer(Disciplina disciplina, int position) {
        activeDisciplina = disciplina;
        activeDisciplinaPosition = position;
        startTimeMillis = System.currentTimeMillis();
        timerAdapter.setRunningDisciplinaId(disciplina.getId());
        timerAdapter.notifyItemChanged(position);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (activeDisciplina != null) {
                    long millis = System.currentTimeMillis() - startTimeMillis;
                    long currentSessionSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);
                    long totalSeconds = activeDisciplina.getTempoTotalSegundos() + currentSessionSeconds;

                    // Atualiza o tempo no ViewHolder para feedback em tempo real
                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(activeDisciplinaPosition);
                    if (viewHolder instanceof TimerAdapter.TimerViewHolder) {
                        TextView tempoView = ((TimerAdapter.TimerViewHolder) viewHolder).tempo;
                        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                                TimeUnit.SECONDS.toHours(totalSeconds),
                                TimeUnit.SECONDS.toMinutes(totalSeconds) % 60,
                                totalSeconds % 60);
                        tempoView.setText(timeFormatted);
                    }
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopActiveTimer() {
        if (activeDisciplina == null) return;

        timerHandler.removeCallbacks(timerRunnable);

        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);

        // 1. Atualiza o tempo total geral
        long newTotalSeconds = activeDisciplina.getTempoTotalSegundos() + elapsedSeconds;
        activeDisciplina.setTempoTotalSegundos(newTotalSeconds);

        // 2. Lógica para salvar o histórico diário
        // Pega a data de hoje no formato YYYY-MM-DD
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataDeHoje = sdf.format(new Date());

        // Pega o histórico da disciplina
        Map<String, Long> historico = activeDisciplina.getHistoricoDiario();
        if(historico == null) {
            historico = new HashMap<>();
        }

        // Pega o tempo já salvo para hoje (ou 0 se não houver) e adiciona o novo tempo
        long tempoDeHoje = historico.getOrDefault(dataDeHoje, 0L);
        historico.put(dataDeHoje, tempoDeHoje + elapsedSeconds);
        activeDisciplina.setHistoricoDiario(historico);

        // 3. Salva o objeto completo (com tempo total e histórico) no Firebase
        databaseReference.child(activeDisciplina.getId()).setValue(activeDisciplina);

        // Reseta o estado
        String stoppedId = activeDisciplina.getId();
        activeDisciplina = null;
        startTimeMillis = 0;
        timerAdapter.setRunningDisciplinaId(null);

        for (int i = 0; i < disciplinaList.size(); i++) {
            if (disciplinaList.get(i).getId().equals(stoppedId)) {
                timerAdapter.notifyItemChanged(i);
                break;
            }
        }
    }


    private void showAddEditDialog(final Disciplina disciplina) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_discipline_input, null);
        builder.setView(dialogView);

        TextView tituloModal = dialogView.findViewById(R.id.dialog_title);
        tituloModal.setText(disciplina == null ? getString(R.string.adicionar_nova_disciplina) : getString(R.string.editar_disciplina));

        final TextInputEditText input = dialogView.findViewById(R.id.edt_discipline_name);

        if (disciplina != null) {
            input.setText(disciplina.getTitulo());
        }

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String titulo = input.getText().toString().trim();
            if (titulo.isEmpty()) {
                Toast.makeText(this, "O título não pode estar vazio.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (disciplina == null) {
                // Adicionar nova disciplina
                String id = databaseReference.push().getKey();
                Disciplina novaDisciplina = new Disciplina(id, titulo);
                databaseReference.child(id).setValue(novaDisciplina);
            } else {
                // Editar disciplina existente
                disciplina.setTitulo(titulo);
                databaseReference.child(disciplina.getId()).setValue(disciplina);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditDeleteDialog(final Disciplina disciplina) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("O que você deseja fazer?");
        String[] options = {"Editar", "Excluir"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Editar
                    showAddEditDialog(disciplina);
                    break;
                case 1: // Excluir
                    new AlertDialog.Builder(this)
                            .setTitle("Excluir Disciplina")
                            .setMessage("Você tem certeza que quer excluir \"" + disciplina.getTitulo() + "\"?")
                            .setPositiveButton("Sim", (d, w) -> {
                                databaseReference.child(disciplina.getId()).removeValue();
                                Toast.makeText(this, "Disciplina excluída.", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Não", null)
                            .show();
                    break;
            }
        });
        builder.show();
    }

    // Garante que o timer pare quando o app for destruído para evitar memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopActiveTimer(); // Salva o progresso se o usuário sair da tela
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 400;
            private static final int SWIPE_VELOCITY_THRESHOLD = 400;
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX < 0) {
                            startActivity(new Intent(IndexTimer.this, Livros.class));
                            finish();
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    public void onClickLivros(View view) {
        Intent intent = new Intent(IndexTimer.this, Livros.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
    public void onClickPomodoro(View view) {
        Intent intent = new Intent(IndexTimer.this, Pomodoro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
    public void onClickInsights(View view) {
        Intent intent = new Intent(IndexTimer.this, Insights.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
    public void onClickAgenda(View view) {
        Intent intent = new Intent(IndexTimer.this, Agenda.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
    public void onClickPerson(View view) {
        Intent intent = new Intent(IndexTimer.this, ConfiguracoesUsuario.class);
        startActivity(intent);
    }

    private void exibirDataAtual() {
        SimpleDateFormat formatadorData = new SimpleDateFormat("E, dd/MM", new Locale("pt", "BR"));
        String dataFormatada = formatadorData.format(new Date());
        dataFormatada = dataFormatada.substring(0, 1).toUpperCase() + dataFormatada.substring(1).replace(".", "");
        txtDataAtual.setText(dataFormatada);
    }
}