package br.net.johansson.filipe.mypetlocator_pet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private TextView txtCod;
    private TextView txtCoords;
    private Button btnRevelarCod;
    private LatLng latLng;

    private String email = "pet@mypet.com";
    private String senha = "589023pthgwe0py0";
    private String pid = "PWN28923IF";

    private boolean isLogged = false;

    private static final int REQUEST_CODE = 7000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());

                txtCoords.setText(Double.toString(location.getLatitude())
                        + "/" + Double.toString(location.getLongitude()));

                if(isLogged) {
                    saveLocationOnDatabase(latLng);
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE);

            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_CODE);

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }

        txtCod = (TextView) findViewById(R.id.txtCod);
        txtCoords = (TextView) findViewById(R.id.txtCoords);
        btnRevelarCod = (Button) findViewById(R.id.btnRevelarCod);

        btnRevelarCod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarUsuario();
            }
        });

    }

    private void registrarUsuario() {
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            String userId = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference currentUserDB = FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("pets")
                                    .child(pid)
                                    .child(userId);
                            currentUserDB.setValue(true);

                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                            mostrarPid(currentUser);

                        } else if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                            entrarConta();

                        }
                    }
                });

    }

    private void entrarConta() {
        firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                            mostrarPid(currentUser);

                        }

                    }
                });

    }

    private void mostrarPid(FirebaseUser currentUser) {
        if(currentUser != null) {
            txtCod.setText(pid);
            isLogged = true;
        }

    }

    private void saveLocationOnDatabase(LatLng latLng) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference currentUserDB = FirebaseDatabase.getInstance()
                .getReference()
                .child("pets")
                .child(pid);

        currentUserDB.child("lat").setValue(latLng.latitude);
        currentUserDB.child("lng").setValue(latLng.longitude);
        currentUserDB.child(userId).setValue(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }

        }

    }
}
