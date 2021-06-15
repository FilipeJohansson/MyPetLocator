package br.net.johansson.filipe.mypetlocator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PetList extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference currentPetDB;

    private Button btnAddPet;
    private EditText edtNomePet, edtCodPet;
    private TextView txtNomePet;
    private ImageView imgPet;
    private String codPet;

    //private FirebaseRecyclerAdapter<Pet, ListPetViewHolder> adapter;

    /*private RecyclerView listPets;
    private RecyclerView.LayoutManager layoutManager;*/

    public static final String PREFS = "prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);

        final SharedPreferences settings = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        firebaseAuth = FirebaseAuth.getInstance();

        /*listPets = (RecyclerView) findViewById(R.id.listPets);
        listPets.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listPets.setLayoutManager(layoutManager);*/

        edtNomePet = (EditText) findViewById(R.id.edtNomePet);
        edtCodPet = (EditText) findViewById(R.id.edtCodPet);
        txtNomePet = (TextView) findViewById(R.id.txtNomePet);
        imgPet = (ImageView) findViewById(R.id.imgPet);
        btnAddPet = (Button) findViewById(R.id.btnAddPet);

        txtNomePet.setVisibility(View.INVISIBLE);
        imgPet.setVisibility(View.INVISIBLE);

        btnAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codPet = edtCodPet.getText().toString();
                String nomePet = edtNomePet.getText().toString();

                if(!TextUtils.isEmpty(codPet) && !TextUtils.isEmpty(nomePet)) {
                    editor.putString("codPet", codPet);
                    editor.putBoolean("ok", true);
                    editor.commit();

                    Boolean isOk = settings.getBoolean("ok", false);

                    Log.d("petLOG", String.valueOf(isOk));

                    String _codPet = settings.getString("codPet", "");

                    Log.d("petLOG", _codPet);

                    currentPetDB = FirebaseDatabase.getInstance().getReference().child("pets").child(_codPet);

                    setupSystem();
                    //updateList();

                    DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference();

                    String userId = firebaseAuth.getCurrentUser().getUid();
                    currentUserDB.child("usuarios")
                            .child(userId)
                            .child("pets")
                            .child(codPet)
                            .setValue(true);

                    currentUserDB.child("pets")
                            .child(codPet)
                            .child("nome")
                            .setValue(nomePet);

                } else {
                    Toast.makeText(PetList.this,
                            "Você precisa preencher todos os campos",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

        Boolean isOk = settings.getBoolean("ok", true);

        Log.d("petLOG", String.valueOf(isOk));

        if(isOk) {
            String _codPet = settings.getString("codPet", "");

            Log.d("petLOG", _codPet);

            currentPetDB = FirebaseDatabase.getInstance().getReference().child("pets").child(_codPet);

            setupSystem();
            //updateList();
        }

    }

    private void setupSystem() {
        currentPetDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pet pet = dataSnapshot.getValue(Pet.class);

                txtNomePet.setText(pet.getNome());

                txtNomePet.setVisibility(View.VISIBLE);
                imgPet.setVisibility(View.VISIBLE);

                Log.d("petLOG", pet.getNome() + "/" + String.valueOf(pet.getLat()) + "/" + String.valueOf(pet.getLng()));

                /*pet.setPid(codPet);
                pet.setLat(dataSnapshot.child("pets").child(codPet).getValue(Pet.class).getLat());
                pet.setLng(dataSnapshot.child("pets").child(codPet).getValue(Pet.class).getLng());
                pet.setNome(dataSnapshot.child("pets").child(codPet).getValue(Pet.class).getNome());*/

                /*Log.e("piddata", String.valueOf(dataSnapshot
                        .child("pets")
                        .child(codPet)
                        .child("lat")
                        .getValue()));*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    /*private void updateList() {
        adapter = new FirebaseRecyclerAdapter<Pet, ListPetViewHolder>(
                Pet.class,
                R.layout.pet_layout,
                ListPetViewHolder.class,
                currentPetDB
        ) {
            @Override
            protected void populateViewHolder(ListPetViewHolder viewHolder, Pet model, int position) {
                viewHolder.txtNomePet.setText(model.getNome());
            }
        };

        adapter.notifyDataSetChanged();
        listPets.setAdapter(adapter);

    }*/

}
