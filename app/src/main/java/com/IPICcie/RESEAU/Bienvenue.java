package com.IPICcie.RESEAU;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;



public class Bienvenue extends AppCompatActivity {

    private static final String bienvenue = "bienvenue";
    private static final String fichier = "Fichier_reglage.xml";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (Boolean.TRUE.equals(isGranted)) {
                    checkPermission();
                } else {
                    AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
                    constructeur.setTitle("Avertissement");
                    constructeur.setMessage("Certaines fonctionnalités pourraient ne pas être disponible \nPour plus d'informations, contactez le service d'assistance IPIC&cie");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenue);
        SharedPreferences pref = getSharedPreferences(fichier,Context.MODE_PRIVATE);
        CheckBox coche= findViewById(R.id.checkBox);
        coche.setChecked(pref.getBoolean(bienvenue,false));
        checkPermission();

        Button cestpartit = findViewById(R.id.aller);
        Button politiqueC = findViewById(R.id.p_c);
        coche.setOnCheckedChangeListener((buttonView, isChecked) -> change());
        cestpartit.setOnClickListener(v -> cestparti());
        politiqueC.setOnClickListener(v -> politique());

    }

    public void cestparti(){
        CheckBox coche= findViewById(R.id.checkBox);
        SharedPreferences pref = getSharedPreferences(fichier,Context.MODE_PRIVATE);
        SharedPreferences.Editor editeur= pref.edit();
        editeur.putBoolean(bienvenue,coche.isChecked()).apply();
        int plusvoir=0;
        if (coche.isChecked()){
            plusvoir=1;
        }

        //barre de chargement
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle("Vive la baguette");
        ProgressBar chargement = new ProgressBar(Bienvenue.this,null, android.R.attr.progressBarStyleHorizontal);
        chargement.setIndeterminate(true);
        chargement.setBackgroundColor(getColor(R.color.bleu_marine));
        constructeur.setMessage(R.string.chargement_0);
        constructeur.setView(chargement);
        constructeur.setCancelable(false);
        constructeur.show();

        Intent intent = new Intent(this, Principal.class);
        intent.putExtra(bienvenue,plusvoir);
        startActivity(intent);
    }

    public void change() {
        CheckBox coche= findViewById(R.id.checkBox);
        SharedPreferences pref = getSharedPreferences(fichier,Context.MODE_PRIVATE);
        SharedPreferences.Editor editeur= pref.edit();
        editeur.putBoolean(bienvenue,coche.isChecked()).apply();

    }

    public void politique(){
    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://docs.google.com/document/d/19Wsd6aIo9_8jv5VuNH5oQqNj6wQFUx1lsUYztSRdzXA/edit?usp=sharing")));
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(Bienvenue.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Bienvenue.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Bienvenue.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(Bienvenue.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(Bienvenue.this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(Bienvenue.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}