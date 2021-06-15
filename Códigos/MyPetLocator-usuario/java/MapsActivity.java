package br.net.johansson.filipe.mypetlocator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference currentPetDB;

    private LocationRequest locationRequest;

    private static int INTERVAL = 3000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISTANCE = 10;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int REQUEST_CODE = 7000;

    private Location lastLocation;
    private LatLng latLng;

    private boolean once = false;

    public static final String PREFS = "prefs";

    private Button btnAddPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final SharedPreferences settings = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();

        btnAddPet = (Button) findViewById(R.id.btnAddPet);

        btnAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, PetList.class);
                startActivity(intent);

            }
        });

        checkPermissions();
        buildGoogleServices();

        Boolean isOk = settings.getBoolean("ok", true);

        Log.d("petLOG", String.valueOf(isOk));

        if(isOk) {
            String _codPett = settings.getString("codPet", "");

            Log.d("petLOG", _codPett);

            currentPetDB = FirebaseDatabase.getInstance().getReference().child("pets").child(_codPett);

            setupSystem();
            //updateList();
        }

    }

    private void setupSystem() {
        currentPetDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pet pet = dataSnapshot.getValue(Pet.class);

                LatLng petLocation = new LatLng(pet.getLat(), pet.getLng());

                mMap.addMarker(new MarkerOptions().position(petLocation).title(pet.getNome()));

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();

        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(DISTANCE);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        saveLocationOnDatabase(latLng);

        if(!once) {
            closeCamera(latLng);
            once = false;
        }

    }

    private void saveLocationOnDatabase(LatLng latLng) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference currentUserDB = FirebaseDatabase.getInstance()
                .getReference()
                .child("usuarios")
                .child(userId);

        currentUserDB.child("lat").setValue(latLng.latitude);
        currentUserDB.child("lng").setValue(latLng.longitude);

    }

    public void closeCamera(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5));

    }

    private void checkPermissions() {
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE);

        } else if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_CODE);

        }

    }

    public boolean buildGoogleServices() {
        int disponivel = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if(disponivel == ConnectionResult.SUCCESS) {
            return true;

        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(disponivel)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this,
                    disponivel,
                    ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(MapsActivity.this, "Google Services indisponível", Toast.LENGTH_LONG).show();

        }

        return false;

    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }


}
