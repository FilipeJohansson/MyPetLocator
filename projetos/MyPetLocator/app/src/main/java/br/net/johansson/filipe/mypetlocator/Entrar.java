package br.net.johansson.filipe.mypetlocator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Entrar extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText edtEmail, edtSenha;
    private TextView txtEsqueceuSenha;
    private Button btnNovaConta, btnEntrar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrar);

        firebaseAuth = FirebaseAuth.getInstance();

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtSenha = (EditText) findViewById(R.id.edtSenha);

        txtEsqueceuSenha = (TextView) findViewById(R.id.txtEsqueceuSenha);

        btnNovaConta = (Button) findViewById(R.id.btnNovaConta);
        btnEntrar = (Button) findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entrarConta();

            }
        });

        txtEsqueceuSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = edtEmail.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(Entrar.this,
                            "Favor, preencha o campo email",
                            Toast.LENGTH_LONG).show();

                } else {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(Entrar.this);
                    dialog.setMessage("Enviar email de recuperação para " + currentUser.getEmail() + "?")
                            .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Toast.makeText(Entrar.this,
                                                        "Email de recuperação enviado",
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(Entrar.this,
                                                        "Algo deu errado, tente novamente",
                                                        Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Apenas cancela
                                }
                            });

                    dialog.create().show();

                }

            }
        });

        btnNovaConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Entrar.this, Registrar.class);
                startActivity(intent);

            }
        });

        progressDialog = new ProgressDialog(this);

    }

    private void entrarConta() {
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            if(TextUtils.isEmpty(email) && TextUtils.isEmpty(senha)) {
                Toast.makeText(Entrar.this,
                        "Você precisa preencher os campos para entrar na sua conta",
                        Toast.LENGTH_LONG).show();

            } else if(TextUtils.isEmpty(email)) {
                Toast.makeText(Entrar.this,
                        "Você precisa colocar um email para entrar na conta",
                        Toast.LENGTH_LONG).show();

            } else if(TextUtils.isEmpty(senha)) {
                Toast.makeText(Entrar.this,
                        "Você precisa colocar uma senha para entrar na conta",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(Entrar.this,
                        "Algo deu errado, tente novamente",
                        Toast.LENGTH_LONG).show();

            }

        } else {
            firebaseAuth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                progressDialog.setMessage("Entrando na sua conta");
                                progressDialog.show();

                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                                if(currentUser.isEmailVerified()) {
                                    Intent intent = new Intent(Entrar.this, MapsActivity.class);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(Entrar.this,
                                            "Você precisa verificar seu email antes de entrar",
                                            Toast.LENGTH_LONG).show();

                                }

                            } else {
                                Toast.makeText(Entrar.this,
                                        "Usuário ou senha incorreto, tente novamente",
                                        Toast.LENGTH_LONG).show();

                            }

                            progressDialog.dismiss();

                        }
                    });

        }

    }

}
