package com.example.chipiquizfinal.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.entity.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private FusedLocationProviderClient fused;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String email = MyApplication.getLoggedEmail();
        if (email == null) {
            Toast.makeText(this, "Не сте логнат!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

         User user = MyApplication.getDatabase()
                .userDao()
                .getUserByEmail(email);
        if (user == null) {
            Toast.makeText(this, "Потребителят не е в базата!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = String.valueOf(user.getId());

       fused = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fm.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 100);
            return;
        }

         fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                LatLng myPos = new LatLng(loc.getLatitude(), loc.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 12));
                map.addMarker(new MarkerOptions()
                        .position(myPos)
                        .title("Ти си тук"));

                 FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("lastLat", loc.getLatitude(),
                                "lastLng", loc.getLongitude());
            }
            loadAllUserPins();
        });
    }

    private void loadAllUserPins() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Double lat = doc.getDouble("lastLat");
                        Double lng = doc.getDouble("lastLng");
                        String name = doc.getString("username");
                        if (lat != null && lng != null) {
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title(name));
                        }
                    }
                });
    }
}

