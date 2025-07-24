package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Pomodoro extends AppCompatActivity {

    // Constantes de tempo em milissegundos
    private static long TEMPO_POMODORO = 1;
    private static long TEMPO_PAUSA_CURTA = 1;
    private static long TEMPO_PAUSA_LONGA = 1;

    private long tempoPomodoro;
    private long tempoCurto;
    private long tempoLongo;

    // Elementos da UI
    private TextView txtTimerPomodoro, txtTimerPausaCurta, txtTimerPausaLonga;
    private Button btnIniciar, btnPausar, btnReiniciar;
    private ProgressBar progressBar;

    private CountDownTimer countDownTimer;
    private long tempoRestanteEmMs;
    private boolean timerRodando;

    // Gerenciamento de estado do ciclo
    private enum EstadoPomodoro { POMODORO, PAUSA_CURTA, PAUSA_LONGA, PARADO }
    private EstadoPomodoro estadoAtual = EstadoPomodoro.PARADO;
    private int ciclosPomodoro = 0;

    GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        /*
        * private static final long TEMPO_POMODORO = 25 * 60 * 1000;
          private static final long TEMPO_PAUSA_CURTA = 5 * 60 * 1000;
          private static final long TEMPO_PAUSA_LONGA = 30 * 60 * 1000;
        * */

        // Inicialização dos componentes da UI
        txtTimerPomodoro = findViewById(R.id.txtTimerPomodoro);
        txtTimerPausaCurta = findViewById(R.id.txtTimerPausaCurta);
        txtTimerPausaLonga = findViewById(R.id.txtTimerPausaLonga);

        btnIniciar = findViewById(R.id.btnIniciar);
        btnPausar = findViewById(R.id.btnPausar);
        btnReiniciar = findViewById(R.id.btnReiniciar);
        progressBar = findViewById(R.id.progressBar);

        btnIniciar.setOnClickListener(v -> {
            String[] vetPomo = txtTimerPomodoro.getText().toString().split(":");
            String txtPomo = vetPomo[0];

            String[] vetCurto = txtTimerPomodoro.getText().toString().split(":");
            String txtCurto = vetCurto[0];

            String[] vetLongo = txtTimerPomodoro.getText().toString().split(":");
            String txtLongo = vetLongo[0];

            txtPomo = txtPomo.equals("00") ? "10" : txtPomo;
            txtCurto = txtCurto.equals("00") ? "10" : txtPomo;
            txtLongo = txtLongo.equals("00") ? "10" : txtPomo;

            tempoPomodoro = Long.parseLong(txtPomo);
            tempoCurto = Long.parseLong(txtCurto);
            tempoLongo = Long.parseLong(txtLongo);

            TEMPO_POMODORO = tempoPomodoro * 60 * 1000;
            TEMPO_PAUSA_CURTA = tempoCurto * 60 * 1000;
            TEMPO_PAUSA_LONGA = tempoLongo * 60 * 1000;

            if (timerRodando) return; // Segurança para evitar duplo clique

            // Se o ciclo nunca começou, inicia o primeiro pomodoro
            if (estadoAtual == EstadoPomodoro.PARADO) {
                iniciarProximoCiclo();
            } else {
                // Se estava pausado, apenas continua de onde parou
                continuarTimer();
            }
        });

        btnPausar.setOnClickListener(v -> pausarTimer());
        btnReiniciar.setOnClickListener(v -> reiniciarCiclo());

        reiniciarCiclo(); // Inicia a tela no estado padrão
        setupGestureDetector();
    }

    public void onClickEditarPomodoro(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Pomodoro");

        String txtPomo = String.valueOf(tempoPomodoro);
        String txtCurto = String.valueOf(tempoCurto);
        String txtLongo = String.valueOf(tempoLongo);

        final EditText inputPomo = new EditText(this);
        final EditText inputCurto = new EditText(this);
        final EditText inputLongo = new EditText(this);

        inputPomo.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputCurto.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputLongo.setInputType(InputType.TYPE_CLASS_NUMBER);

        inputPomo.setHint("Pomodoro");
        builder.setView(inputPomo);

        builder.show();
    }

    private void iniciarProximoCiclo() {
        if (estadoAtual == EstadoPomodoro.PARADO) {
            estadoAtual = EstadoPomodoro.POMODORO;
            ciclosPomodoro = 1;
            iniciarTimer(TEMPO_POMODORO);
        } else if (estadoAtual == EstadoPomodoro.POMODORO) {
            if (ciclosPomodoro == 2) {
                estadoAtual = EstadoPomodoro.PAUSA_LONGA;
                iniciarTimer(TEMPO_PAUSA_LONGA);
            } else {
                estadoAtual = EstadoPomodoro.PAUSA_CURTA;
                iniciarTimer(TEMPO_PAUSA_CURTA);
            }
        } else if (estadoAtual == EstadoPomodoro.PAUSA_CURTA) {
            estadoAtual = EstadoPomodoro.POMODORO;
            ciclosPomodoro++;
            iniciarTimer(TEMPO_POMODORO);
        } else if (estadoAtual == EstadoPomodoro.PAUSA_LONGA) {
            reiniciarCiclo();
            Toast.makeText(this, "Ciclo Pomodoro completo!", Toast.LENGTH_SHORT).show();
        }
    }


    private void continuarTimer() {
        iniciarTimer(tempoRestanteEmMs);
    }

    private void iniciarTimer(long duracaoMs) {
        tempoRestanteEmMs = duracaoMs;
        countDownTimer = new CountDownTimer(tempoRestanteEmMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestanteEmMs = millisUntilFinished;
                atualizarUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerRodando = false;
                iniciarProximoCiclo();
            }
        }.start();

        timerRodando = true;
        atualizarBotoes();
    }

    private void pausarTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRodando = false;
        atualizarBotoes();
    }

    private void reiniciarCiclo() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        estadoAtual = EstadoPomodoro.PARADO;
        ciclosPomodoro = 0;
        timerRodando = false;
        atualizarUI(TEMPO_POMODORO);
        atualizarBotoes();
    }

    private void atualizarUI(long tempoMs) {
        int minutos = (int) (tempoMs / 1000) / 60;
        int segundos = (int) (tempoMs / 1000) % 60;
        String tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);

        long duracaoTotal = getDuracaoTotalEstado(estadoAtual);

        // Reseta todos os textos para o padrão antes de atualizar o correto
        txtTimerPomodoro.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_POMODORO / 1000) / 60));
        txtTimerPausaCurta.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_CURTA / 1000) / 60));
        txtTimerPausaLonga.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_LONGA / 1000) / 60));

        switch (estadoAtual) {
            case POMODORO:
                txtTimerPomodoro.setText(tempoFormatado);
                break;
            case PAUSA_CURTA:
                txtTimerPausaCurta.setText(tempoFormatado);
                break;
            case PAUSA_LONGA:
                txtTimerPausaLonga.setText(tempoFormatado);
                break;
            case PARADO:
                // Já resetado acima
                break;
        }

        progressBar.setProgress((int) (tempoMs * 100 / duracaoTotal));
    }

    private void atualizarBotoes() {
        if (timerRodando) {
            btnIniciar.setVisibility(View.GONE);
            btnPausar.setVisibility(View.VISIBLE);
            btnReiniciar.setVisibility(View.VISIBLE);
        } else {
            btnIniciar.setText("Continuar");
            btnIniciar.setVisibility(View.VISIBLE);
            btnPausar.setVisibility(View.GONE);
            btnReiniciar.setVisibility(View.VISIBLE);
            if (estadoAtual == EstadoPomodoro.PARADO) {
                btnIniciar.setText("Iniciar");
                btnReiniciar.setVisibility(View.GONE);
            }
        }
    }

    private long getDuracaoTotalEstado(EstadoPomodoro estado) {
        switch (estado) {
            case PAUSA_CURTA:
                return TEMPO_PAUSA_CURTA;
            case PAUSA_LONGA:
                return TEMPO_PAUSA_LONGA;
            case POMODORO:
            case PARADO:
            default:
                return TEMPO_POMODORO;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
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
                            startActivity(new Intent(Pomodoro.this, Livros.class));
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        } else {
                            startActivity(new Intent(Pomodoro.this, Insights.class));
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
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onClickTimer(View view) {
        Intent intent = new Intent(Pomodoro.this, IndexTimer.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickLivros(View view) {
        Intent intent = new Intent(Pomodoro.this, Livros.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void onClickInsights(View view) {
        Intent intent = new Intent(Pomodoro.this, Insights.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void onClickAgenda(View view) {
        Intent intent = new Intent(Pomodoro.this, Agenda.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void onClickPerson(View view) {
        Intent intent = new Intent(Pomodoro.this, ConfiguracoesUsuario.class);
        startActivity(intent);
    }
}