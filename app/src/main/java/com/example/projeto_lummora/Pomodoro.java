package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; 

import java.util.Locale;

public class Pomodoro extends AppCompatActivity {

    // Constantes de tempo em milissegundos (valores padrão)
    private long TEMPO_POMODORO = 25 * 60 * 1000;
    private long TEMPO_PAUSA_CURTA = 5 * 60 * 1000;
    private long TEMPO_PAUSA_LONGA = 30 * 60 * 1000;

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

    // Detector de gestos para os swipes
    GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        // Configurações para ocultar a barra de navegação e status, dando uma experiência imersiva
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        // Inicialização dos componentes da UI
        txtTimerPomodoro = findViewById(R.id.txtTimerPomodoro);
        txtTimerPausaCurta = findViewById(R.id.txtTimerPausaCurta);
        txtTimerPausaLonga = findViewById(R.id.txtTimerPausaLonga);

        btnIniciar = findViewById(R.id.btnIniciar);
        btnPausar = findViewById(R.id.btnPausar);
        btnReiniciar = findViewById(R.id.btnReiniciar);
        progressBar = findViewById(R.id.progressBar);

        // Define o OnClickListener para os TextViews de tempo e para o botão de edição
        // Ao clicar em qualquer um, o AlertDialog para editar os tempos será aberto
        txtTimerPomodoro.setOnClickListener(this::onClickEditarPomodoro);
        txtTimerPausaCurta.setOnClickListener(this::onClickEditarPomodoro);
        txtTimerPausaLonga.setOnClickListener(this::onClickEditarPomodoro);
        findViewById(R.id.btnEditPomodoro).setOnClickListener(this::onClickEditarPomodoro);

        // Define os tempos iniciais no display da UI
        atualizarTempoDisplay();

        // Configura os listeners dos botões de controle do timer
        btnIniciar.setOnClickListener(v -> {
            if (timerRodando) return; // Segurança para evitar clique duplo se o timer já estiver rodando

            if (estadoAtual == EstadoPomodoro.PARADO) {
                // Se o ciclo nunca começou, inicia o primeiro pomodoro
                iniciarProximoCiclo();
            } else {
                // Se estava pausado, apenas continua de onde parou
                continuarTimer();
            }
        });

        btnPausar.setOnClickListener(v -> pausarTimer());
        btnReiniciar.setOnClickListener(v -> reiniciarCiclo());

        reiniciarCiclo(); // Inicia a tela no estado padrão (reinicia os contadores, etc.)
        setupGestureDetector(); // Configura o detector de gestos (swipes)
    }

    /**
     * Método responsável por abrir o AlertDialog para o usuário definir os tempos
     * de Pomodoro, Pausa Curta e Pausa Longa.
     * @param view A view que foi clicada (neste caso, pode ser um TextView ou um ImageButton).
     */
    public void onClickEditarPomodoro(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // O título agora está no layout customizado, não precisamos do título padrão do builder.

        // Infla o layout customizado para o AlertDialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_pomodoro_times, null);
        builder.setView(dialogView); // Define a view customizada para o AlertDialog

        // Referencia os EditTexts dentro do layout customizado do diálogo
        final TextInputEditText inputPomodoro = dialogView.findViewById(R.id.edt_pomodoro_time);
        final TextInputEditText inputPausaCurta = dialogView.findViewById(R.id.edt_short_break_time);
        final TextInputEditText inputPausaLonga = dialogView.findViewById(R.id.edt_long_break_time);

        // Preenche os EditTexts com os valores atuais dos tempos para facilitar a edição
        inputPomodoro.setText(String.valueOf(TEMPO_POMODORO / (60 * 1000))); // Converte ms para minutos
        inputPausaCurta.setText(String.valueOf(TEMPO_PAUSA_CURTA / (60 * 1000)));
        inputPausaLonga.setText(String.valueOf(TEMPO_PAUSA_LONGA / (60 * 1000)));

        // Configura o botão "Salvar" do AlertDialog
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            try {
                // Pega os valores digitados, converte para long e depois para milissegundos
                long newPomodoroTime = Long.parseLong(inputPomodoro.getText().toString()) * 60 * 1000;
                long newPausaCurtaTime = Long.parseLong(inputPausaCurta.getText().toString()) * 60 * 1000;
                long newPausaLongaTime = Long.parseLong(inputPausaLonga.getText().toString()) * 60 * 1000;

                // Validação básica: garante que os tempos sejam maiores que zero
                if (newPomodoroTime <= 0 || newPausaCurtaTime <= 0 || newPausaLongaTime <= 0) {
                    Toast.makeText(this, "Por favor, insira tempos maiores que zero.", Toast.LENGTH_SHORT).show();
                    return; // Sai do método se a validação falhar
                }

                // Atualiza as constantes globais com os novos tempos
                TEMPO_POMODORO = newPomodoroTime;
                TEMPO_PAUSA_CURTA = newPausaCurtaTime;
                TEMPO_PAUSA_LONGA = newPausaLongaTime;

                // Reinicia o ciclo e atualiza a UI para refletir os novos tempos imediatamente
                reiniciarCiclo();
                atualizarTempoDisplay(); // Garante que os TextViews no cabeçalho mostrem os novos tempos
                Toast.makeText(this, "Tempos do Pomodoro atualizados!", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                // Lida com erro se o usuário digitar algo que não seja um número
                Toast.makeText(this, "Entrada inválida. Por favor, insira apenas números.", Toast.LENGTH_SHORT).show();
            }
        });
        // Configura o botão "Cancelar" do AlertDialog
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show(); // Exibe o AlertDialog
    }

    /**
     * Inicia o próximo ciclo do Pomodoro (Pomodoro, Pausa Curta, Pausa Longa).
     * Gerencia a transição entre os estados do timer.
     */
    private void iniciarProximoCiclo() {
        Intent it = new Intent(Pomodoro.this, AudioService.class);
        it.setAction("");
        startService(it);
        if (estadoAtual == EstadoPomodoro.PARADO) {
            // Se o timer está parado, inicia um novo ciclo de Pomodoro
            estadoAtual = EstadoPomodoro.POMODORO;
            ciclosPomodoro = 1; // Reseta a contagem de ciclos
            iniciarTimer(TEMPO_POMODORO);
        } else if (estadoAtual == EstadoPomodoro.POMODORO) {
            // Após um Pomodoro, decide se é pausa curta ou longa
            if (ciclosPomodoro % 4 == 0) { // Regra do Pomodoro: Pausa Longa após 4 Pomodoros
                estadoAtual = EstadoPomodoro.PAUSA_LONGA;
                iniciarTimer(TEMPO_PAUSA_LONGA);
            } else {
                estadoAtual = EstadoPomodoro.PAUSA_CURTA;
                iniciarTimer(TEMPO_PAUSA_CURTA);
            }
        } else if (estadoAtual == EstadoPomodoro.PAUSA_CURTA) {
            // Após uma Pausa Curta, volta para o Pomodoro
            estadoAtual = EstadoPomodoro.POMODORO;
            ciclosPomodoro++; // Incrementa o contador de Pomodoros completados
            iniciarTimer(TEMPO_POMODORO);
        } else if (estadoAtual == EstadoPomodoro.PAUSA_LONGA) {
            // Após uma Pausa Longa, o ciclo completo é encerrado e reiniciado
            reiniciarCiclo(); // Volta ao estado PARADO
            Toast.makeText(this, "Ciclo Pomodoro completo!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Continua o timer de onde ele parou (se o tempo restante for maior que zero).
     */
    private void continuarTimer() {
        if (tempoRestanteEmMs > 0) {
            iniciarTimer(tempoRestanteEmMs);
        } else {
            Toast.makeText(this, "Tempo esgotado. Inicie um novo ciclo.", Toast.LENGTH_SHORT).show();
            reiniciarCiclo();
        }
    }

    /**
     * Inicia ou reinicia o CountDownTimer com a duração especificada.
     * @param duracaoMs Duração total do timer em milissegundos.
     */
    private void iniciarTimer(long duracaoMs) {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancela qualquer timer que esteja rodando
        }
        tempoRestanteEmMs = duracaoMs;
        countDownTimer = new CountDownTimer(tempoRestanteEmMs, 1000) { // Intervalo de 1 segundo
            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestanteEmMs = millisUntilFinished;
                atualizarUI(millisUntilFinished); // Atualiza o display e a barra de progresso
            }

            @Override
            public void onFinish() {
                timerRodando = false;
                iniciarProximoCiclo(); // Ao finalizar, avança para o próximo estágio do ciclo
            }
        }.start(); // Inicia o timer

        timerRodando = true; // Indica que o timer está rodando
        atualizarBotoes(); // Atualiza a visibilidade dos botões
    }

    /**
     * Pausa o timer em execução.
     */
    private void pausarTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancela o timer
        }
        timerRodando = false; // Indica que o timer não está mais rodando
        atualizarBotoes(); // Atualiza a visibilidade dos botões
    }

    /**
     * Reinicia completamente o ciclo do Pomodoro para o estado inicial.
     */
    private void reiniciarCiclo() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        estadoAtual = EstadoPomodoro.PARADO; // Define o estado como parado
        ciclosPomodoro = 0; // Reseta a contagem de ciclos
        timerRodando = false; // Timer não está rodando
        // Ao reiniciar, exibe o tempo total do Pomodoro inicialmente
        atualizarUI(TEMPO_POMODORO); // Atualiza a UI para o tempo padrão do Pomodoro
        atualizarTempoDisplay(); // Garante que todos os displays são resetados para o padrão/configurado
        atualizarBotoes(); // Atualiza a visibilidade dos botões
    }

    /**
     * Atualiza a interface do usuário (TextView do timer e ProgressBar)
     * com o tempo restante atual.
     * @param tempoMs Tempo restante em milissegundos.
     */
    private void atualizarUI(long tempoMs) {
        int minutos = (int) (tempoMs / 1000) / 60;
        int segundos = (int) (tempoMs / 1000) % 60;
        String tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);

        long duracaoTotal = getDuracaoTotalEstado(estadoAtual); // Pega a duração total do estado atual

        // Atualiza apenas o texto do timer que está ativo no momento
        switch (estadoAtual) {
            case POMODORO:
                txtTimerPomodoro.setText(tempoFormatado);
                // Os outros tempos são resetados para seus valores completos para clareza
                txtTimerPausaCurta.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_CURTA / 1000) / 60));
                txtTimerPausaLonga.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_LONGA / 1000) / 60));
                break;
            case PAUSA_CURTA:
                txtTimerPausaCurta.setText(tempoFormatado);
                txtTimerPomodoro.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_POMODORO / 1000) / 60));
                txtTimerPausaLonga.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_LONGA / 1000) / 60));
                break;
            case PAUSA_LONGA:
                txtTimerPausaLonga.setText(tempoFormatado);
                txtTimerPomodoro.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_POMODORO / 1000) / 60));
                txtTimerPausaCurta.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_CURTA / 1000) / 60));
                break;
            case PARADO:
                // Quando o timer está parado, todos os tempos exibem suas durações totais
                atualizarTempoDisplay();
                break;
        }

        // Atualiza a barra de progresso com base no tempo restante
        progressBar.setProgress((int) (tempoMs * 100 / duracaoTotal));
    }

    /**
     * Atualiza o texto de todos os TextViews de tempo no cabeçalho
     * com base nos valores atuais das constantes TEMPO_POMODORO, TEMPO_PAUSA_CURTA, TEMPO_PAUSA_LONGA.
     */
    private void atualizarTempoDisplay() {
        txtTimerPomodoro.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_POMODORO / 1000) / 60));
        txtTimerPausaCurta.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_CURTA / 1000) / 60));
        txtTimerPausaLonga.setText(String.format(Locale.getDefault(), "%02d:00", (TEMPO_PAUSA_LONGA / 1000) / 60));
    }

    /**
     * Atualiza a visibilidade e o texto dos botões de controle (Iniciar, Pausar, Reiniciar)
     * com base no estado atual do timer.
     */
    private void atualizarBotoes() {
        if (timerRodando) {
            btnIniciar.setVisibility(View.GONE);
            btnPausar.setVisibility(View.VISIBLE);
            btnReiniciar.setVisibility(View.VISIBLE);
        } else {
            btnIniciar.setText("Continuar"); // Se parado, o botão Iniciar vira "Continuar"
            btnIniciar.setVisibility(View.VISIBLE);
            btnPausar.setVisibility(View.GONE);
            btnReiniciar.setVisibility(View.VISIBLE);
            if (estadoAtual == EstadoPomodoro.PARADO) {
                btnIniciar.setText("Iniciar"); // Se o ciclo está totalmente parado, vira "Iniciar"
                btnReiniciar.setVisibility(View.GONE); // Reiniciar não faz sentido se nada começou
            }
        }
    }

    /**
     * Retorna a duração total em milissegundos para o estado atual do Pomodoro.
     * Usado para calcular o progresso da barra.
     * @param estado O estado atual do Pomodoro (POMODORO, PAUSA_CURTA, etc.).
     * @return A duração total daquele estado em milissegundos.
     */
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
        // Garante que o timer é cancelado para evitar vazamentos de memória
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     * Configura o GestureDetector para detectar gestos de deslize (swipe)
     * e navegar entre as telas.
     */
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 400; // Distância mínima para ser considerado swipe
            private static final int SWIPE_VELOCITY_THRESHOLD = 400; // Velocidade mínima para ser considerado swipe

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                // Verifica se o movimento horizontal foi maior que o vertical
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    // Verifica se a distância e a velocidade foram suficientes para um swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) { // Swipe para a direita
                            startActivity(new Intent(Pomodoro.this, Livros.class));
                            finish(); // Finaliza a atividade atual
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Animação de transição
                        } else { // Swipe para a esquerda
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
        // Encaminha os eventos de toque para o GestureDetector
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    // Métodos para navegação entre telas via clique nos botões/TextViews do Tab Layout
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