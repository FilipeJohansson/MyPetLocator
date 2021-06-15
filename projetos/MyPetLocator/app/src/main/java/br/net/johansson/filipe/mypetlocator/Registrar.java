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
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class Registrar extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText edtEmail, edtSenha, edtConfirmarSenha;
    private TextView txtConfirmacaoEmail;
    private Button btnRegistrar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        firebaseAuth = FirebaseAuth.getInstance();

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtSenha = (EditText) findViewById(R.id.edtSenha);
        edtConfirmarSenha = (EditText) findViewById(R.id.edtConfirmarSenha);

        txtConfirmacaoEmail = (TextView) findViewById(R.id.txtConfirmacaoEmail);

        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarUsuario();
            }
        });

        txtConfirmacaoEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Registrar.this);
                dialog.setMessage("Enviar email de confirmação novamente?")
                        .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                currentUser.sendEmailVerification();

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
        });

        progressDialog = new ProgressDialog(this);

    }

    private void registrarUsuario() {
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();
        String confirmarSenha = edtConfirmarSenha.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(senha) || TextUtils.isEmpty(confirmarSenha)) {
            if(TextUtils.isEmpty(email) && TextUtils.isEmpty(senha) && TextUtils.isEmpty(confirmarSenha)) {
                Toast.makeText(Registrar.this,
                        "Você precisa preencher todos os campos",
                        Toast.LENGTH_LONG).show();

            } else if(TextUtils.isEmpty(email)) {
                Toast.makeText(Registrar.this,
                        "Você precisa colocar um email",
                        Toast.LENGTH_LONG).show();

            } else if(TextUtils.isEmpty(senha)) {
                Toast.makeText(Registrar.this,
                        "Você precisa colocar uma senha",
                        Toast.LENGTH_LONG).show();

            } else if(TextUtils.isEmpty(confirmarSenha)) {
                Toast.makeText(Registrar.this,
                        "Você precisa confirmar sua senha",
                        Toast.LENGTH_LONG).show();

            }

        } else {
            progressDialog.setMessage("Registrando usuário");
            progressDialog.show();

            if(senha.equals(confirmarSenha)) {
                firebaseAuth.createUserWithEmailAndPassword(email, senha)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(Registrar.this,
                                            "Enviamos um email de confirmação para você",
                                            Toast.LENGTH_LONG).show();

                                    String userId = firebaseAuth.getCurrentUser().getUid();
                                    DatabaseReference currentUserDB = FirebaseDatabase.getInstance()
                                            .getReference()
                                            .child("usuarios")
                                            .child(userId);
                                    currentUserDB.setValue(true);

                                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                    currentUser.sendEmailVerification();

                                } else if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(Registrar.this,
                                            "Email já registrado",
                                            Toast.LENGTH_LONG).show();

                                } else if(task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(Registrar.this,
                                            "Você precisa colocar uma senha mais forte",
                                            Toast.LENGTH_LONG).show();

                                } else if(task.getException() instanceof FirebaseAuthEmailException) {
                                    Toast.makeText(Registrar.this,
                                            "Não conseguimos enviar o email de confirmação para você. Tente novamente",
                                            Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(Registrar.this,
                                            "Algo deu errado, tente novamente",
                                            Toast.LENGTH_LONG).show();

                                }

                                progressDialog.dismiss();

                            }
                        });

            } else {
                Toast.makeText(Registrar.this,
                        "As senhas não coincidem",
                        Toast.LENGTH_LONG).show();

                progressDialog.dismiss();

            }

        }

    }
}
