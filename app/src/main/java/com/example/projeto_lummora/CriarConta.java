package com.example.projeto_lummora;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CriarConta extends AppCompatActivity {
    EditText edtEmail;
    EditText edtSenha;
    EditText edtConfirmarSenha;
    EditText edtNome;
    EditText edtCelular;
    DatabaseReference databaseReference;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_criar_conta);

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
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        edtNome = findViewById(R.id.edtNome);
        edtCelular = findViewById(R.id.edtCelular);

        // Inicialização do Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

    }



    // Método para voltar à tela anterior
    public void onVoltar(View view) {
        finish();
    }

    // Método responsável por criar o cadastro do usuário
    public void onCriar(View view) {
        // Recuperação das informações digitadas
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();
        String confirmarSenha = edtConfirmarSenha.getText().toString();
        String nome = edtNome.getText().toString();
        String celular = edtCelular.getText().toString();

        // Validações
        if (email.isEmpty()) {
            Toast.makeText(this, "É necessário digitar um E-mail!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.isEmpty()) {
            Toast.makeText(this, "É necessário digitar uma senha!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "Senha precisa ter ao menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nome.isEmpty()) {
            Toast.makeText(this, "Por favor, digite seu nome.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (celular.isEmpty()) {
            Toast.makeText(this, "Por favor, digite seu celular.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar usuário no Firebase
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            // Recupera o ID do usuário
                            String userId = user.getUid();
                            // Cria um objeto Usuario com os dados
                            Usuario novoUsuario = new Usuario(nome, celular, email);

                            // Salva o usuário no Firebase
                            databaseReference.child(userId).setValue(novoUsuario);

                            // Exibe mensagem de sucesso
                            new AlertDialog.Builder(CriarConta.this)
                                    .setTitle("E-mail enviado")
                                    .setMessage("Por favor, verifique seu E-mail para finalizar o cadastro.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        // Redirecionar para a tela de login
                                        Intent intent = new Intent(CriarConta.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            Toast.makeText(this, "Falha ao enviar e-mail de confirmação.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Caso o cadastro falhe, exibe a mensagem de erro
                Toast.makeText(this, "Erro no cadastro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}