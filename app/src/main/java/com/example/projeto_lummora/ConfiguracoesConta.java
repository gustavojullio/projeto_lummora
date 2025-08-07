package com.example.projeto_lummora;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConfiguracoesConta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracoes_conta);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }



    public void onVoltar(View view) {
        finish();
    }

    public void onClickProgram(View view) {
        finish();
        Runnable rd = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };

        try {
            rd.wait(100);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void apagarDados(View view) {
        String[] opcoes = {"Apagar", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Apagar dados da contas");
        builder.setItems(opcoes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser user = auth.getCurrentUser();

                    final String[] TABELAS = {"Disciplinas", "Livros", "Tarefas"};

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                    for(String tabela : TABELAS) {
                        try {
                            Query query = ref.child(tabela).child(user.getUid());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    snapshot.getRef().removeValue();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        builder.show();
    }

    public void apagarConta(View view) {
        String[] opcoes = {"Apagar", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Apagar conta");
        builder.setItems(opcoes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser user = auth.getCurrentUser();

                    final String[] TABELAS = {"Disciplinas", "Livros", "Tarefas", "Usuarios"};

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                    for(String tabela : TABELAS) {
                        try {
                            Query query = ref.child(tabela).child(user.getUid());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    snapshot.getRef().removeValue();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    user.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getApplicationContext(), "Conta Apagada com sucesso", Toast.LENGTH_SHORT).show();
                            if (user != null) {
                                if (user.isEmailVerified()) {

                                    Intent it = new Intent(ConfiguracoesConta.this, MainActivity.class);
                                    startActivity(it);
                                    finish();
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Erro: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    Intent it = new Intent(ConfiguracoesConta.this, MainActivity.class);
                    startActivity(it);
                    finishAffinity();
                }
            }
        });
        builder.show();


    }
}