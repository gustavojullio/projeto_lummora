package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Use sempre o AlertDialog do androidx
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class IniciarSessao extends AppCompatActivity {
    // Declaração das variáveis
    EditText edtEmail, edtSenha;
    FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    // NOVO: O ActivityResultLauncher deve ser um campo da classe, não um método
    // E o registerForActivityResult deve ser chamado no contexto da Activity
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
                        Toast.makeText(this, "Falha no login com Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void onVoltar(View view) {
        finish();
    }

    public void onEsqueceuEmail(View v) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuEmail.class);
        startActivity(intent);
    }

    public void onEntrar(View view) {
        String emailDigitado = edtEmail.getText().toString();
        String senhaDigitada = edtSenha.getText().toString();

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

                if (user != null && user.isEmailVerified()) {
                    Intent intent = new Intent(IniciarSessao.this, IndexTimer.class);
                    startActivity(intent);
                    finish();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("E-mail não verificado")
                            .setMessage("Por favor, verifique seu e-mail antes de logar.")
                            .setPositiveButton("OK", (dialog, which) -> {})
                            .setCancelable(false)
                            .show();
                }
            } else {
                Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                Log.e("LoginError", task.getException().getMessage());
            }
        });
    }

    public void onSignInWithGoogle(View view) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(IniciarSessao.this, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(IniciarSessao.this, IndexTimer.class));
                    finish();
                } else {
                    Toast.makeText(IniciarSessao.this, "Falha no login com Google: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onEsqueceuSenha(View view) {
        Intent intent = new Intent(IniciarSessao.this, EsqueceuSenha.class);
        startActivity(intent);
    }
}