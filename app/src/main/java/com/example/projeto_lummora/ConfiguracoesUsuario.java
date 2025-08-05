package com.example.projeto_lummora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ConfiguracoesUsuario extends AppCompatActivity {
    EditText txtDescricao;
    Button btnSalvarBio;
    TextView txtNome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracoes_usuario);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        txtDescricao = findViewById(R.id.editTextTextMultiLine2);
        btnSalvarBio = findViewById(R.id.btnSalvarBio);
        txtNome = findViewById(R.id.textView28);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        DatabaseReference dbDesc = ref.child("Usuarios").child(user.getUid()).child("descricao");
        DatabaseReference dbNome = ref.child("Usuarios").child(user.getUid()).child("nome");

        dbNome.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtNome.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbDesc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtDescricao.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void onConfiguracoes(View v) {
        Intent intent = new Intent(ConfiguracoesUsuario.this, ConfiguracoesConta.class);
        startActivity(intent);
    }

    public void onClickProgram(View view) {
        finish();
    }

    public void onClickLogout(View view) {
        Intent it = new Intent(ConfiguracoesUsuario.this, IniciarSessao.class);
        startActivity(it);
        finishAffinity();
        FirebaseAuth.getInstance().signOut();
    }

    public void salvarBio(View view) {
        String texto = txtDescricao.getText().toString();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        DatabaseReference dbDesc = ref.child("Usuarios").child(user.getUid()).child("descricao");

        dbDesc.setValue(texto);
    }
}