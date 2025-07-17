package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.GestureDetector;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Livros extends AppCompatActivity {

    GestureDetector gestureDetector;
    private RecyclerView recyclerView;
    private LivroAdapter livroAdapter;
    private List<Livro> livroList;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    // Variáveis para controlar o timer
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private Livro activeLivro = null;
    private int activeLivroPosition = -1;
    private long startTimeMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livros);

        // tela inteira
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Referência para o novo nó Livros no Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Livros").child(currentUser.getUid());

        setupRecyclerView();
        loadLivrosFromFirebase();
        setupGestureDetector();

        ImageButton addLivroButton = findViewById(R.id.imageButton2);
        addLivroButton.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycleLivros);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        livroList = new ArrayList<>();
        livroAdapter = new LivroAdapter(this, livroList);
        recyclerView.setAdapter(livroAdapter);

        livroAdapter.setOnLivroClickListener(new LivroAdapter.OnLivroClickListener() {
            @Override
            public void onItemClick(Livro livro) {
                if (activeLivro != null && activeLivro.getId().equals(livro.getId())) {
                    return;
                }
                showEditDeleteDialog(livro);
            }

            @Override
            public void onPlayClick(Livro livro, int position) {
                handlePlayPause(livro, position);
            }
        });
    }

    private void loadLivrosFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                livroList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Livro livro = dataSnapshot.getValue(Livro.class);
                    if (livro != null) {
                        livroList.add(livro);
                    }
                }
                livroAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Livros.this, "Falha ao carregar livros.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePlayPause(Livro livro, int position) {
        if (activeLivro != null) {
            if (activeLivro.getId().equals(livro.getId())) {
                stopActiveTimer();
            } else {
                stopActiveTimer();
                startTimer(livro, position);
            }
        } else {
            startTimer(livro, position);
        }
    }

    private void startTimer(Livro livro, int position) {
        activeLivro = livro;
        activeLivroPosition = position;
        startTimeMillis = System.currentTimeMillis();
        livroAdapter.setRunningLivroId(livro.getId());
        livroAdapter.notifyItemChanged(position);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (activeLivro != null) {
                    long millis = System.currentTimeMillis() - startTimeMillis;
                    long currentSessionSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);
                    long totalSeconds = activeLivro.getTempoTotalSegundos() + currentSessionSeconds;

                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(activeLivroPosition);
                    if (viewHolder instanceof LivroAdapter.LivroViewHolder) {
                        TextView tempoView = ((LivroAdapter.LivroViewHolder) viewHolder).tempoLeitura;
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
        if (activeLivro == null) return;

        timerHandler.removeCallbacks(timerRunnable);
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long newTotalSeconds = activeLivro.getTempoTotalSegundos() + TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
        activeLivro.setTempoTotalSegundos(newTotalSeconds);

        databaseReference.child(activeLivro.getId()).setValue(activeLivro);

        String stoppedId = activeLivro.getId();
        activeLivro = null;
        startTimeMillis = 0;
        livroAdapter.setRunningLivroId(null);

        for (int i = 0; i < livroList.size(); i++) {
            if (livroList.get(i).getId().equals(stoppedId)) {
                livroAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void showAddEditDialog(final Livro livro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(livro == null ? "Adicionar Livro" : "Editar Livro");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (livro != null) {
            input.setText(livro.getTitulo());
        }
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String titulo = input.getText().toString().trim();
            if (titulo.isEmpty()) {
                Toast.makeText(this, "O título não pode estar vazio.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (livro == null) {
                String id = databaseReference.push().getKey();
                Livro novoLivro = new Livro(id, titulo);
                databaseReference.child(id).setValue(novoLivro);
            } else {
                livro.setTitulo(titulo);
                databaseReference.child(livro.getId()).setValue(livro);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditDeleteDialog(final Livro livro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("O que você deseja fazer?");
        String[] options = {"Editar", "Excluir"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Editar
                    showAddEditDialog(livro);
                    break;
                case 1: // Excluir
                    new AlertDialog.Builder(this)
                            .setTitle("Excluir Livro")
                            .setMessage("Você tem certeza que quer excluir \"" + livro.getTitulo() + "\"?")
                            .setPositiveButton("Sim", (d, w) -> databaseReference.child(livro.getId()).removeValue())
                            .setNegativeButton("Não", null)
                            .show();
                    break;
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(activeLivro != null) {
            stopActiveTimer();
        }
    }


    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            startActivity(new Intent(Livros.this, IndexTimer.class));
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        } else {
                            startActivity(new Intent(Livros.this, Pomodoro.class));
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

    public void onClickTimer(View view) {
        Intent intent = new Intent(Livros.this, IndexTimer.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickPomodoro(View view) {
        Intent intent = new Intent(Livros.this, Pomodoro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void onClickInsights(View view) {
        Intent intent = new Intent(Livros.this, Insights.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void onClickAgenda(View view) {
        Intent intent = new Intent(Livros.this, Agenda.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void onClickPerson(View view) {
        Intent intent = new Intent(Livros.this, ConfiguracoesUsuario.class);
        startActivity(intent);
    }
}