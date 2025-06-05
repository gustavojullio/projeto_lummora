package com.example.projeto_lummora;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class EsqueceuSenha extends AppCompatActivity {
    private EditText edtEmail;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueceu_senha);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        edtEmail = findViewById(R.id.edtEmail);
        auth = FirebaseAuth.getInstance();
    }

    public void enviarEmailRedefinicao(View view) {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showAlertDialog("Campo vazio", "Por favor, digite seu e-mail para redefinir a senha.");
            return;
        }

        AlertDialog loadingDialog = showLoadingDialog("Enviando", "Processando sua solicitação...");

        // Tentativa direta de enviar o email de reset
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();

                    if (task.isSuccessful()) {
                        // Alteração na mensagem de sucesso
                        showSuccessDialog("E-mail enviado",
                                "Se este e-mail estiver registrado, você receberá um link de redefinição. Caso contrário, não receberá nenhuma ação.",
                                this::finish);
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // Alteração na mensagem de erro para e-mail não registrado
                            showAlertDialog("E-mail não encontrado",
                                    "Não encontramos nenhuma conta com este endereço de e-mail.");
                        } else {
                            showAlertDialog("Erro",
                                    "Não foi possível enviar o e-mail: " +
                                            task.getException().getMessage());
                        }
                    }
                });
    }

    private AlertDialog showLoadingDialog(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccessDialog(String title, String message, Runnable onDismiss) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> onDismiss.run())
                .setCancelable(false)
                .show();
    }

    public void onVoltar(View v) {
        finish();
    }
}
