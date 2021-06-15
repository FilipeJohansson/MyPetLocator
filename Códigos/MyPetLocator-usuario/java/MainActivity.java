package br.net.johansson.filipe.mypetlocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static int SCREEN_TIME = 3000;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                updateUser(currentUser);

            }

        }, SCREEN_TIME);

    }

    private void updateUser(FirebaseUser currentUser) {
        if(currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(MainActivity.this, Entrar.class);
            startActivity(intent);

        }

    }
}
