package com.example.projeto_lummora;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;




public class IniciarSessao extends AppCompatActivity {
    // Declaração das variáveis
   EditText edtEmail, edtSenha;
   FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_iniciar_sessao);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        // Inicialização das variáveis
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        // Inicialização do Firebase
        auth = FirebaseAuth.getInstance();

    }

    // Método para voltar à tela anterior
    public void onVoltar(View view) {
        finish();
    }



   public void onEsqueceuEmail(View v) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuEmail.class);
        startActivity(intent);
    }

   /* public void onEsqueceuSenha(View v) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuSenha.class);
        startActivity(intent);
    }*/

    // Método responsável por realizar o login do usuário
    public void onEntrar(View view) {
        // Recuperação das informações digitadas
        String emailDigitado = edtEmail.getText().toString();
        String senhaDigitada = edtSenha.getText().toString();

        // Verifica se os campos não estão em branco
        if (emailDigitado.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senhaDigitada.isEmpty()) {
            Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(emailDigitado, senhaDigitada).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();

                // Verifica se o e-mail foi verificado
                if (user != null && user.isEmailVerified()) {
                    Intent intent = new Intent(IniciarSessao.this, IndexTimer.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Caso o e-mail não tenha sido verificado
                    new AlertDialog.Builder(this)
                            .setTitle("E-mail não verificado")
                            .setMessage("Por favor, verifique seu e-mail antes de logar.")
                            .setPositiveButton("OK", (dialog, which) -> {})
                            .setCancelable(false)
                            .show();
                }
            } else {
                // Caso o login falhe
                Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                Log.e("LoginError", task.getException().getMessage());
            }
        });
    }



    // Método para lidar com "Esqueceu a senha?"
    public void onEsqueceuSenha(View view) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuSenha.class);
        startActivity(intent);
    }
}