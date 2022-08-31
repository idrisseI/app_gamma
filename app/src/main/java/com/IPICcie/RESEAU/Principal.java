package com.IPICcie.RESEAU;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;
import static java.lang.Double.parseDouble;

import android.Manifest;
import android.app.Application;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.IPICcie.RESEAU.CreditsNotesVersion;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


//TODO: ajouter des données journal
//TODO: montrer/cacher
//TODO: vérifier permissions
//TODO: vérifier capteurs (loc/boussole/wifi (adresse)...) activés
//TODO: MàJ depuis l'application
//TODO: réduire temps affichage param

public class Principal extends AppCompatActivity implements SensorEventListener {


    LocationManager locationManager = null;
    MyLocationNewOverlay mLocationOverlay;
    List<String[]> base1 = new ArrayList<>();
    List<String[]> base2 = new ArrayList<>();
    List<String[]> base3 = new ArrayList<>();
    List<String[]> base4 = new ArrayList<>();
    List<String[]> base5 = new ArrayList<>();
    List<Double> antenneProche;  //rendre local?
    List<GeoPoint> anciennesPositions = new ArrayList<>();
    private MapView mMap;
    private Marker position;
    private Marker positionAntenne;
    private TextView etatTexte;
    private String type;
    private int etatInt;
    private String fournisseur;
    private String locMoi;
    private String locAntenne;
    private ArrayList<String> instructIti;
    private int indiceInstruction = 0;
    private final List<Marker> marqueurs = new ArrayList<>();
    private InterstitialAd interstitialAd;
    private static final String AD_UNIT_ID = "ca-app-pub-9220805797747004/9068089003";
    private final List<String> donneesJournal = new ArrayList<>();
    private SensorManager sensorManager;
    private float DegreeStart = 0f;
    private ImageView boussole;
    private Sensor rotation_vector;
    private SensorEventListener ecouteOrientation;
    private AdView mAdView;
    private FolderOverlay itineraireOverlay;
    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 5;
    private static final String fichierReglage = "Fichier_reglage.xml";

    private Boolean isNetworkAvailable(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }

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

    InstallStateUpdatedListener installStateUpdatedListener = new
            InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    if (state.installStatus() == InstallStatus.INSTALLED) {
                        if (mAppUpdateManager != null) {
                            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                        }
                    } else {
                        Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
                    }
                }
            };

    //pour localisation auto
    LocationListener ecouteurGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location localisation) {
            montrerCacher(false);
            //Toast.makeText(getApplicationContext(), fournisseur + " localisation", Toast.LENGTH_SHORT).show();
            Log.d("GPS", "localisation : " + localisation.toString());
            String autres = String.format("Vitesse : %f - Altitude : %f - Cap : %f%n", localisation.getSpeed(), localisation.getAltitude(), localisation.getBearing());
            Log.d("GPS", autres);
            String timestamp = String.format("Timestamp : %d%n", localisation.getTime());
            Log.d("GPS", "timestamp : " + timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(localisation.getTime());
            Log.d("GPS", sdf.format(date));
            locMoi = textloc(localisation.getLatitude(), localisation.getLongitude());
            position.setPosition(new GeoPoint(localisation));
            position.setVisible(false);

        }

        @Override
        public void onProviderDisabled(String fournisseur) {
            Toast.makeText(getApplicationContext(), fournisseur + " désactivé !", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String fournisseur) {
            Toast.makeText(getApplicationContext(), fournisseur + " activé !", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String fournisseur, int status, Bundle extras) {
            if (etatInt != status) {
                switch (status) {
                    case LocationProvider.AVAILABLE:
                        Toast.makeText(getApplicationContext(), fournisseur + " état disponible", Toast.LENGTH_SHORT).show();
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Toast.makeText(getApplicationContext(), fournisseur + " état indisponible", Toast.LENGTH_SHORT).show();
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), fournisseur + " état temporairement indisponible", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), fournisseur + " état : " + status, Toast.LENGTH_SHORT).show();
                }
            }
            etatInt = status;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        etatTexte = findViewById(R.id.Etat);

        checkPermission();

        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, initializationStatus -> {
        });
        loadAd();
        preparePub();
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        if (pref.getBoolean("pub", false)) {
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        //carte
        initCarte();

        //boussole  //TODO:faire fonctionner boussole
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        rotation_vector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //localisation
        locAuto(pref.getBoolean("locAuto", true));
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), mMap);
        if (pref.getBoolean("locAuto", true)) {
            initialiserLocalisation();
        }
        mMap.setMultiTouchControls(true);
        mMap.getOverlays().add(mLocationOverlay);
        mMap.invalidate();

        //écouteurs
        initEcouteurs();

        donneesJournal.add("|+|");

        //importation des données
        etatTexte.setText(R.string.chargement_1);
        ProgressBar chargement = findViewById(R.id.chargement);
        new Chargement_donnees(chargement).execute();

        //Infos MàJ
        if (!pref.getString("version","pouf").equals(getString(R.string.version))) {
            AlertDialog.Builder constructeur2 = new AlertDialog.Builder(this);
            constructeur2.setTitle("Quoi de neuf");
            constructeur2.setMessage(String.format("Vous utilisez désormais %s !%nCette mise à jour mineure résout quelques problèmes d'affichage et améliore le guidage %n%n N'hésitez pas à nous partagez vos impressions et vos commentaires ", getString(R.string.version)));
            constructeur2.setNeutralButton(getString(R.string.cestparti), (dialog, which) -> {/*rien à faire*/});
            constructeur2.setPositiveButton("Notes de version", (dialog, which) -> {
                Intent intention = new Intent(Principal.this, CreditsNotesVersion.class);
                startActivity(intention);
            });
            TilesOverlay x=this.mMap.getOverlayManager().getTilesOverlay();
            //x.setOvershootTileCache(x.getOvershootTileCache() * 2);  TODO: accroitre le cache
            constructeur2.show();
        }else{
            checkupdate();
        }
        pref.edit().putString("version", getString(R.string.version)).apply();
    }

    @Override
    protected  void onStop(){
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        arreterLocalisation();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
        sensorManager.unregisterListener(Principal.this);
    }

    public void onExplose() {
        this.finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ajouter mes items de menu
        getMenuInflater().inflate(R.menu.menu, menu);
        // ajouter les items du système s'il y en a
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.parametres:
                voirParam();
                return true;
            case R.id.infos:
                voirInfos();
                return true;
            case R.id.Aide:
                Intent intention = new Intent(this, Aide.class);
                startActivity(intention);
                return true;
            case R.id.contact:
                Intent intention2 = new Intent(this, NousContacter.class);
                startActivity(intention2);
                return true;
            case R.id.version:
                Intent intention3 = new Intent(this, CreditsNotesVersion.class);
                startActivity(intention3);
                return true;
            case R.id.nous_soutenir:
                showInterstitial();
                return true;
            case R.id.détruire:
                onExplose();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void majParametres(View v) {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        EditText val = v.findViewById(R.id.zoom1);
        val.setText(pref.getString("zoom_pos", "15"));
        EditText val2 = v.findViewById(R.id.zoom2);
        val2.setText(pref.getString("zoom_ant", "15"));
        SeekBar val3 = v.findViewById(R.id.nombre_antennes_niveau);
        val3.setProgress(pref.getInt("nb_antennes", 2));
        CheckBox val4 = v.findViewById(R.id.loc_auto);
        val4.setChecked(pref.getBoolean("locAuto", true));
        CheckBox val5 = v.findViewById(R.id.enregistrement_auto);
        val5.setChecked(pref.getBoolean("enregistrement_auto", true));
        CheckBox val6 = v.findViewById(R.id.pub_active);
        val6.setChecked(pref.getBoolean("pub", false));
        CheckBox deuxG = v.findViewById(R.id.deuxG);
        CheckBox troisG = v.findViewById(R.id.troisG);
        CheckBox quatreG = v.findViewById(R.id.quatreG);
        CheckBox cinqG = v.findViewById(R.id.cinqG);
        deuxG.setChecked(pref.getBoolean("2G", true));
        troisG.setChecked(pref.getBoolean("3G", true));
        quatreG.setChecked(pref.getBoolean("4G", true));
        cinqG.setChecked(pref.getBoolean("5G", true));
    }

    private boolean changeParametres(View v) {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        SharedPreferences.Editor editeur = pref.edit();
        CheckBox val = v.findViewById(R.id.loc_auto);
        editeur.putBoolean("loc_auto", val.isChecked()).apply();
        locAuto(val.isChecked());
        CheckBox val1 = v.findViewById(R.id.enregistrement_auto);
        editeur.putBoolean("enregistrement_auto", val1.isChecked());
        EditText val2 = v.findViewById(R.id.zoom1);
        editeur.putString("zoom_pos", val2.getText().toString()).apply();
        EditText val3 = v.findViewById(R.id.zoom2);
        editeur.putString("zoom_ant", val3.getText().toString()).apply();
        SeekBar val4 = v.findViewById(R.id.nombre_antennes_niveau);
        editeur.putInt("nb_antennes", val4.getProgress()).apply();
        CheckBox val5 = v.findViewById(R.id.pub_active);
        editeur.putBoolean("pub", val5.isChecked()).apply();
        CheckBox deuxG = v.findViewById(R.id.deuxG);
        CheckBox troisG = v.findViewById(R.id.troisG);
        CheckBox quatreG = v.findViewById(R.id.quatreG);
        CheckBox cinqG = v.findViewById(R.id.cinqG);
        if ((deuxG.isChecked() || troisG.isChecked() || quatreG.isChecked() || cinqG.isChecked())) {
            editeur.putBoolean("2G", deuxG.isChecked()).apply();
            editeur.putBoolean("3G", troisG.isChecked()).apply();
            editeur.putBoolean("4G", quatreG.isChecked()).apply();
            editeur.putBoolean("5G", cinqG.isChecked()).apply();

        } else {
            Toast.makeText(Principal.this, "Laissez au moins un panneau (2G, 3G, 4G ou 5G)!", Toast.LENGTH_LONG).show();
            return (false);
        }
        if (!val5.isChecked()) {
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(adRequest);
        }
        return (true);
    }

    public void voirParam() {
        AlertDialog.Builder constructeur = new AlertDialog.Builder(Principal.this);
        constructeur.setTitle(getString(R.string.parametres_titre_txt));
        LayoutInflater inflater = this.getLayoutInflater();
        Log.d(TAG, "affichage parametres 1 "+System.currentTimeMillis());
        View v = inflater.inflate(R.layout.parametres, null);
        Log.d(TAG, "affichage parametres 2 "+System.currentTimeMillis());
        majParametres(v);
        constructeur.setView(v);
        Button valider = v.findViewById(R.id.validerParam);
        Button reinit = v.findViewById(R.id.reinitialiser);
        final AlertDialog show = constructeur.show();
        valider.setOnClickListener(view -> {
            if (changeParametres(v)) {
                show.dismiss();
            }
        });
        reinit.setOnClickListener(view -> repartZero());
    }

    public void repartZero() {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        SharedPreferences.Editor editeur = pref.edit();
        editeur.clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void locAuto(Boolean check) {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        SharedPreferences.Editor editeur = pref.edit();
        editeur.putBoolean("locAuto", check).apply();
        if (Boolean.TRUE.equals(check)) {
            initialiserLocalisation();
        } else {
            arreterLocalisation();
        }
    }

    public void optionPosition() {
        String[] element = {getString(R.string.loc_auto_txt), getString(R.string.place_mark_txt), getString(R.string.anc_pos_txt), getString(R.string.recherche_com_txt), getString(R.string.recherche_adr_txt)};
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle(R.string.Options_position_menu_titre);
        montrerCacher(true);
        constructeur.setItems(element, (dialog, which) -> {
            String action = element[which];
            dialog.dismiss();
            switch (action) {
                case "Localisation automatique":
                    Toast toast = Toast.makeText(Principal.this, getString(R.string.loc_auto_activ), Toast.LENGTH_SHORT);
                    toast.show();
                    locAuto(true);
                    break;
                case "Placer un marqueur":
                    Toast toast2 = Toast.makeText(Principal.this, R.string.place_mark_indication, Toast.LENGTH_LONG);
                    toast2.show();
                    break;
                case "Anciennes positions":
                    montrerCacher(false);
                    afficheAnciennesPosition();
                    break;
                case "rechercher par commune (hors connexion)":
                    afficheRechercheCommune();
                    break;
                case "rechercher par adresse":
                    montrerCacher(false);
                    afficheChercheAdresse();
                    break;
                default:
                    break;
            }
        });
        constructeur.setCancelable(true);
        AlertDialog alert = constructeur.create();
        alert.show();
    }

    public void troisptitspoints() {
        String[] element = {"Itinéraire"};
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle(R.string.Options_points_titre);
        constructeur.setItems(element, (dialog, which) -> {
            String action = element[which];
            dialog.dismiss();
            switch (action) {
                case "Itinéraire":
                    choixLocomotion();
                    break;
                case "Boussole":
                    boussole();
                    break;
                default:
                    break;
            }
        });
        constructeur.setCancelable(true);
        AlertDialog alert = constructeur.create();
        alert.show();
    }

    public void cherche() {
        ProgressBar chargement = findViewById(R.id.chargement);
        if (chargement.getVisibility() != VISIBLE) {
            chargement.setVisibility(VISIBLE);
            chargement.setProgress(0);
            etatTexte.setText(R.string.chargement_2);
            suprMarqueurs();
            new Recherche_antenne(chargement).execute(Boolean.FALSE);
        }
    }

    public void centrerSurPosition() {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        IMapController mapController = mMap.getController();
        mapController.setZoom(Double.parseDouble(pref.getString("zoom_pos", "15")));
        mapController.setCenter(position.getPosition());
    }

    public void centrerSurAntenne() {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        IMapController mapController = mMap.getController();
        mapController.setZoom(Double.parseDouble(pref.getString("zoom_ant", "15")));
        mapController.setCenter(positionAntenne.getPosition());
    }

    public void listeAntennes() {
        ProgressBar chargement = findViewById(R.id.chargement);
        chargement.setVisibility(VISIBLE);
        chargement.setProgress(0);
        etatTexte.setText(R.string.chargement_2);
        suprMarqueurs();
        new Recherche_antenne(chargement).execute(Boolean.TRUE);
    }

    public void afficheAnciennesPosition() {
        arreterLocalisation();
        AlertDialog.Builder constructeur = new AlertDialog.Builder(Principal.this);
        constructeur.setTitle(getString(R.string.anc_pos_txt));
        List<String> element = new ArrayList<>();
        for (GeoPoint local : anciennesPositions) {
            element.add(textloc(local.getLatitude(), local.getLongitude()));
        }
        String[] listeelements = element.toArray(new String[0]);
        constructeur.setItems(listeelements, (dialogInterface, i) -> {
            position.setSnippet(listeelements[i]);
            position.setPosition((anciennesPositions.get(i)));
            locMoi = position.getSnippet();
            mMap.getController().setCenter(position.getPosition());
            arreterLocalisation();
            suprMarqueurs();
            mMap.invalidate();
        });
        constructeur.show();
    }

    public void afficheRechercheCommune() {
        final EditText edittext = new EditText(Principal.this);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setHint("Numéros du département");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        edittext.setLayoutParams(lp);
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle(getString(R.string.recherche_com_txt));
        constructeur.setView(edittext);
        constructeur.setPositiveButton("Valider", (dialog, whichButton) -> {
            //What ever you want to do with the value
            Editable dep = (edittext.getText());
            if (dep.length() > 0 && Integer.parseInt(dep.toString()) < 95) {
                listeCommunes(Integer.parseInt(dep.toString()));
            } else {
                Toast.makeText(Principal.this, "département non-référencé", Toast.LENGTH_SHORT).show();
            }
        });
        constructeur.show();
    }

    public void afficheChercheAdresse() {
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle(R.string.recherche_adr_txt);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.cherche_adresse, null);
        constructeur.setView(v);
        EditText lat = v.findViewById(R.id.latitudeAdresse);
        EditText lon = v.findViewById(R.id.longitudeAdresse);
        EditText adr = v.findViewById(R.id.Adresse);
        Button val = v.findViewById(R.id.valider);
        final AlertDialog show = constructeur.show();
        val.setOnClickListener(view -> {
            arreterLocalisation();
            rechercheAdresse(lat.getText().toString(), lon.getText().toString(), adr.getText().toString());
            show.dismiss();
        });
        show.show();
    }

    public void choixLocomotion() {
        String[] elements = {"voiture", "vélo", "marche", "fauteuil roulant"};
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle("Moyen de locomotion");
        constructeur.setItems(elements, (dialog, which) -> {
            String moyen = elements[which];
            dialog.dismiss();
            switch (moyen) {
                case "voiture":
                    itineraire(position.getPosition(), positionAntenne.getPosition(), "driving-car");
                    break;
                case "vélo":
                    itineraire(position.getPosition(), positionAntenne.getPosition(), "cycling-regular");
                    break;
                case "marche":
                    itineraire(position.getPosition(), positionAntenne.getPosition(), "foot-walking");
                    break;
                case "fauteuil roulant":
                    itineraire(position.getPosition(), positionAntenne.getPosition(), "wheelchair");
                    break;
                default:
                    break;
            }
        });
        constructeur.show();
    }

    public void voirInfos() {
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle("Informations");
        if (locMoi != null) {
            if (locAntenne != null) {
                constructeur.setMessage(String.format("Votre position: %n%s%n%nPosition de l'antenne:%n%s", locMoi, locAntenne));
            } else {
                constructeur.setMessage(String.format("Votre position: %n%s", locMoi));
            }
            constructeur.setCancelable(true);
            constructeur.show();
        } else {
            Toast.makeText(this, "Aucune information à afficher", Toast.LENGTH_LONG).show();
        }

    }

    public void initCarte() {
        // Création de la carte
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mMap = findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);
        //barre zoom
        ScaleBarOverlay echelle = new ScaleBarOverlay(mMap);
        position = new Marker(mMap);
        position.setIcon(getResources().getDrawable(R.drawable.map_marker));
        position.setAnchor(0.32F, 0.68F);
        position.setPosition(new GeoPoint(48.8701, 2.31658));
        position.setSnippet("Siège IPIC");
        position.setVisible(false);
        positionAntenne = new Marker(mMap);
        positionAntenne.setIcon(getResources().getDrawable(R.drawable.antenne_trouvee));
        positionAntenne.setAnchor(0.32F, 0.68F);
        positionAntenne.setVisible(false);
        mMap.getOverlays().add(new LongPressMapOverlay()); // récupérer le gestionnaire de carte (= caméra)
        mMap.getOverlays().add(echelle);
        mMap.getOverlays().add(position);
        mMap.getOverlays().add(positionAntenne);
        IMapController mapController = mMap.getController();
        mapController.setZoom(8D);//vue initiale
        mapController.setCenter(new GeoPoint(48.8701, 2.31658));

        mMap.invalidate();
    }

    public void initEcouteurs() {
        Button val1 = findViewById(R.id.position);
        Button val2 = findViewById(R.id.trois_petits_points);
        Button val3 = findViewById(R.id.chercher);
        Button val4 = findViewById(R.id.centrer_sur_position);
        Button val5 = findViewById(R.id.centrer_sur_antenne);
        Button val6 = findViewById(R.id.liste_antennes);
        Button val7 = findViewById(R.id.vers_test_wifi);
        com.google.android.material.floatingactionbutton.FloatingActionButton val8 = findViewById(R.id.itineraire);

        val1.setOnClickListener(v -> optionPosition());
        val2.setOnClickListener(v -> troisptitspoints());
        val3.setOnClickListener(v -> cherche());
        val4.setOnClickListener(v -> centrerSurPosition());
        val5.setOnClickListener(v -> centrerSurAntenne());
        val6.setOnClickListener(v -> listeAntennes());
        val7.setOnClickListener(v -> {
            Intent intention = new Intent(this, TestWifi.class);
            startActivity(intention);
        });
        val8.setOnClickListener(v -> afficheInstructions());
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Principal.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(Principal.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void initialiserLocalisation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        } else {
            if (mLocationOverlay != null) {
                mLocationOverlay.enableMyLocation();
            }
            if (locationManager == null) {
                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteres = new Criteria();

                // la précision  : (ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision)
                criteres.setAccuracy(Criteria.ACCURACY_FINE);

                // l'altitude
                criteres.setAltitudeRequired(false);

                // la direction
                criteres.setBearingRequired(false);

                // la vitesse
                criteres.setSpeedRequired(false);

                // la consommation d'énergie demandée
                criteres.setCostAllowed(true);
                criteres.setPowerRequirement(Criteria.POWER_MEDIUM);

                fournisseur = locationManager.getBestProvider(criteres, true);
                Log.d("GPS", "fournisseur : " + fournisseur);
            }

            if (fournisseur != null) {
                // dernière position connue
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("GPS", "no permissions !");
                    return;
                }

                Location localisation = locationManager.getLastKnownLocation(fournisseur);

                if (localisation != null) {
                    // on notifie la localisation
                    IMapController controile = mMap.getController();
                    controile.setZoom(11D);
                    controile.setCenter(new GeoPoint(localisation.getLatitude(), localisation.getLongitude()));
                    ecouteurGPS.onLocationChanged(localisation);
                    position.setPosition(new GeoPoint(localisation));
                    position.setSnippet(textloc(position.getPosition().getLatitude(), position.getPosition().getLongitude()));
                    locMoi = position.getSnippet();
                    anciennesPositions.add(position.getPosition());
                    montrerCacher(false);
                }

                // on configure la mise à jour automatique : au moins 10 mètres et 1 secondes
                locationManager.requestLocationUpdates(fournisseur, 1000, 10, ecouteurGPS);
            }
            else {
                Toast.makeText(this,"on va vous trouver", Toast.LENGTH_SHORT);
            }
        }
    }

    private void arreterLocalisation() {
        if (mLocationOverlay != null) {
            mLocationOverlay.setEnableAutoStop(true);
            position.setVisible(true);
            if (locationManager != null && ecouteurGPS != null) {
                locationManager.removeUpdates(ecouteurGPS);
                mLocationOverlay.disableMyLocation();
                Log.d(TAG, "GPS: désactivé");
            }
        }
    }

    public void suprMarqueurs() {
        for (Marker element : marqueurs) {
            element.setVisible(false);
            mMap.getOverlayManager().remove(element);
        }
        if (itineraireOverlay != null) {
            mMap.getOverlayManager().remove(itineraireOverlay);
        }
        mMap.invalidate();
    }

    public GeoPoint getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            Log.d(TAG, "Address: " + address);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new GeoPoint(location.getLatitude(), location.getLongitude());

            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAddressFromLocation(Double latitude, Double longitude) {
        //TODO: désactiver si pas de wifi (pas de fournisseurs) (vérifier modifs).
        if (Boolean.TRUE.equals(isNetworkAvailable(getApplication()))) {
            Geocoder coder = new Geocoder(this);
            List<Address> address;

            try {
                address = coder.getFromLocation(latitude, longitude, 1);
                Log.d(TAG, "Address: " + address);
                if (address == null || address.isEmpty()) {
                    return null;
                }

                return address.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void rechercheAdresse(String latitude, String longitude, String adresse) {
        arreterLocalisation();
        suprMarqueurs();
        if (!longitude.equals("") && !latitude.equals("")) {
            position.setVisible(true);
            position.setPosition(new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude)));
            position.setSnippet(textloc(position.getPosition().getLatitude(), position.getPosition().getLongitude()));
            locMoi = position.getSnippet();
            anciennesPositions.add(position.getPosition());
            mMap.getController().setCenter(position.getPosition());
            mMap.invalidate();
        } else if (!adresse.equals("")) {
            GeoPoint loc = getLocationFromAddress(adresse);
            if (loc != null) {
                position.setVisible(true);
                position.setPosition(loc);
                position.setSnippet(textloc(position.getPosition().getLatitude(), position.getPosition().getLongitude()));
                locMoi = position.getSnippet();
                anciennesPositions.add(position.getPosition());
                mMap.getController().setCenter(loc);
                mMap.invalidate();
            } else {
                Toast.makeText(this, getString(R.string.erreur_adresse), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.champs_vide_recherche_adresse), Toast.LENGTH_LONG).show();
        }
    }

    public List<Integer> rechercheA(Double latitude, Double longitude) {    //recherche dichotomique
        List<Integer> indexCoordonnees = new ArrayList<>();
        int fin = base1.size() - 1;
        int debut = 0;
        int nombre = (debut + fin) / 2;
        while (fin - debut > 1) {
            if (latitude < parseDouble(base1.get(nombre)[1])) {
                fin = nombre;
                nombre = (debut + fin) / 2;
            } else {
                debut = nombre;
                nombre = (debut + fin) / 2;
            }
        }
        indexCoordonnees.add(nombre);
        fin = base2.size() - 1;
        debut = 0;
        nombre = (debut + fin) / 2;
        while (fin - debut > 1) {
            if (longitude < parseDouble(base2.get(nombre)[0])) {
                fin = nombre;
                nombre = (debut + fin) / 2;
            } else {
                debut = nombre;
                nombre = (debut + fin) / 2;
            }
        }
        indexCoordonnees.add(nombre);
        return (indexCoordonnees);
    }

    public List<Integer> rechercheB() {
        SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
        List<Integer> indexcoordonnee = new ArrayList<>();
        double latitude = position.getPosition().getLatitude();
        double longitude = position.getPosition().getLongitude();
        double distance = 0.05;
        int rayon = pref.getInt("nb_antennes", 2) * 50 + 20;
        Log.d(TAG, "rechercheB debug: " + rayon);
        while (indexcoordonnee.size() < rayon && distance < 2) {
            List<Integer> borneMoins = rechercheA(latitude - distance, longitude - distance);
            List<Integer> bornePlus = rechercheA(latitude + distance, longitude + distance);
            for (Integer indice = borneMoins.get(1); indice <= bornePlus.get(1); indice++) {
                if ((Integer.parseInt(base2.get(indice)[2]) <= bornePlus.get(0)) && (Integer.parseInt(base2.get(indice)[2]) >= borneMoins.get(0))) {
                    indexcoordonnee.add(indice);
                }
            }
            distance = distance + 0.05;
        }
        Log.d(TAG, "rechercheB: " + indexcoordonnee.size() + " " + distance);
        return indexcoordonnee;
    }

    public String type(Integer index) {
        String types = "Panneaux disponibles:";
        if (Integer.parseInt(base3.get(index)[0]) == 1) {
            types += " 2G";
        }
        if (Integer.parseInt(base3.get(index)[1]) == 1) {
            types += " 3G";
        }
        if (Integer.parseInt(base3.get(index)[2]) == 1) {
            types += " 4G";
        }
        if (Integer.parseInt(base3.get(index)[3]) == 1) {
            types += " 5G";
        }

        types+=" fournisseur: "+ base3.get(index)[4];
        return types;
    }

    public void listeCommunes(int departement) {
        //TODO: montrer/cacher + Corse

        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle("Liste des communes");
        int fin = base5.size() - 1;
        int debut = 0;
        int nombre = (debut + fin) / 2;
        while (fin - debut > 1) {    //recherche dichotomique du département
            if (departement < Integer.parseInt(base5.get(nombre)[0])) {
                fin = nombre;
                nombre = (debut + fin) / 2;
            } else {
                debut = nombre;
                nombre = (debut + fin) / 2;
            }
        }
        Log.d(TAG, "listeCommunes: indice dep"+base5.get(nombre)[2]+base5.get(nombre)[3]);
        StringBuilder element = new StringBuilder();
        //cherche toutes les villes du département
        for (int number = Integer.parseInt(base5.get(nombre)[2]); number <= Integer.parseInt(base5.get(nombre)[3]); number++) {
            element.append(base5.get(number)[1]);
            element.append(";");
        }
        arreterLocalisation();
        suprMarqueurs();
        positionAntenne.setVisible(false);
        String[] elements = element.toString().split(";");
        int finalNombre = nombre;
        constructeur.setItems(elements, (dialog, which) -> {
            String commune = elements[which];
            dialog.dismiss();
            arreterLocalisation();
            int machin = Integer.parseInt(base5.get(finalNombre)[2]) + which; //indice de la ville
            machin = Integer.parseInt(base5.get(machin)[5]); //indice dans base4
            Log.d(TAG, "listeCommunes: indice dans base4"+machin);
            int borne1 = 0;
            while (Integer.parseInt(base4.get(machin - borne1)[0]) == Integer.parseInt(base4.get(machin)[0])) {
                borne1++;
            }
            int borne2 = 1;
            while (Integer.parseInt(base4.get(machin + borne2)[0]) == Integer.parseInt(base4.get(machin)[0])) {
                borne2++;
            }
            Integer[] listeBornees = {machin - (borne1), machin + (borne2)};
            Log.d(TAG, "listeCommunes: bornes 1 et 2 "+(machin-borne1)+" "+(machin+borne2)+" "+machin);
            if (listeBornees[1] - listeBornees[0] > 500) {
                AlertDialog.Builder construct = new AlertDialog.Builder(Principal.this);
                construct.setTitle("AVERTISSEMENT");
                construct.setMessage("Nous avons trouvé beaucoup d'antennes, peut-être même trop. \nVotre appareil pourrait ne pas supporter la charge exigée par l'application pour toutes les afficher. \nNous vous recommandons de faire une recherche plus précise en plaçant un marqueur sur la carte.");
                construct.show();
            } else {
                mMap.getController().setCenter(new GeoPoint(
                        Double.parseDouble(base2.get(Integer.parseInt(base4.get(listeBornees[0])[1]))[1]) + 0.001D,
                        Double.parseDouble(base2.get(Integer.parseInt(base4.get(listeBornees[0])[1]))[0])));
                mMap.getController().setZoom(10D);
            }
            suprMarqueurs();
            Marker x;
            Log.d(TAG, "onClick: RECH3" + Arrays.toString(listeBornees));
            for (Integer indice1 = listeBornees[0]; indice1 < listeBornees[1]; indice1++) {
                x = new Marker(mMap);
                x.setPosition(new GeoPoint(
                        Double.parseDouble(base2.get(Integer.parseInt(base4.get(indice1)[1]))[1]),
                        Double.parseDouble(base2.get(Integer.parseInt(base4.get(indice1)[1]))[0])));
                Log.d(TAG, "onClick: RECH4" + x.getPosition() + " " + indice1 + " " + Integer.parseInt(base4.get(indice1)[1]));
                x.setSnippet(String.format("Position: %s; %s; (%s); %s", x.getPosition().getLatitude(), x.getPosition().getLongitude(), commune, type(Integer.parseInt(base4.get(indice1)[1]))));
                x.setIcon(getDrawable(R.drawable.autre_antenne));
                x.setAnchor(0.32F, 0.68F);
                marqueurs.add(x);
                mMap.getOverlays().add(x);
                mMap.invalidate();
            }
            position.setPosition(marqueurs.get(marqueurs.size() - 1).getPosition());
            position.setVisible(false);
            position.setSnippet(textloc(position.getPosition().getLatitude(), position.getPosition().getLongitude()));
            locMoi = position.getSnippet();
            anciennesPositions.add(position.getPosition());
            positionAntenne.setPosition(marqueurs.get(marqueurs.size() - 1).getPosition());
            positionAntenne.setVisible(false);
        });
        constructeur.show();
    }

    public class ChargementListener {


        public void onChargementTermine() {
            etatTexte.setText(R.string.pret);
            donneesJournal.add("|{'-'}|");
        }

        public void onAntenneTrouvee(List<Double> antenneProche, String type, Boolean succes) {
            if (Boolean.TRUE.equals(succes)) {
                montrerCacher(true);
                montrerCacher(false);
                Button centreAnt = findViewById(R.id.centrer_sur_antenne);
                Button toutesAnt = findViewById(R.id.liste_antennes);
                //Button troisptitspoints = findViewById(R.id.trois_petits_points);
                com.google.android.material.floatingactionbutton.FloatingActionButton iti = findViewById(R.id.itineraire);
                iti.setVisibility(VISIBLE);
                //troisptitspoints.setVisibility(VISIBLE);
                centreAnt.setVisibility(VISIBLE);
                toutesAnt.setVisibility(VISIBLE);
                locAntenne = getString(R.string.infos_antenne, antenneProche.get(1), antenneProche.get(0), type, antenneProche.get(3));
                positionAntenne.setVisible(true);
                positionAntenne.setPosition(new GeoPoint(antenneProche.get(1), antenneProche.get(0)));
                positionAntenne.setSnippet(getString(R.string.infos_antenne_snippet, antenneProche.get(1), antenneProche.get(0), antenneProche.get(3), type));
                IMapController mapController = mMap.getController();
                mapController.setCenter(positionAntenne.getPosition());
                mapController.setZoom(11D);
                etatTexte.setText(R.string.pret);
                for (Marker antenne : marqueurs) {
                    mMap.getOverlays().add(antenne);
                }
                Log.d(TAG, "onAntenneTrouvee: FIN" + mMap.getOverlays());
                mMap.invalidate();
                donneesJournal.add(position.getPosition().toString() + positionAntenne.getPosition().toString());
            } else {
                AlertDialog.Builder constructeur = new AlertDialog.Builder(Principal.this);
                constructeur.setTitle("Nous avons une triste nouvelle");
                constructeur.setMessage("Aucune antenne répertoriée à moins de 100km de la position indiquée.");
                constructeur.setNeutralButton("Compris", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                constructeur.show();
            }
        }

    }

    //Chargement données
    private class Chargement_donnees extends AsyncTask<String, Integer, Float> {
        // ProgressBar à mettre à jour
        private final ProgressBar mBarre;

        private final ChargementListener ecouteur = new ChargementListener();

        // constructeur, fournir la ProgressBar concernée

        Chargement_donnees(ProgressBar barre) {
            this.mBarre = barre;
        }

        @Override
        protected Float doInBackground(String... strings) {
            importBaseDeDonnees(base1, "2021-t4-metropole-sites-base_1.csv");
            publishProgress(20);
            importBaseDeDonnees(base2, "2021-t4-metropole-sites-base_2.csv");
            publishProgress(40);
            importBaseDeDonnees(base3, "2021-t4-metropole-sites-base_3.csv");
            publishProgress(60);
            importBaseDeDonnees(base4, "2021-t4-metropole-sites-base_4.csv");
            publishProgress(80);
            importBaseDeDonnees(base5, "2021-t4-metropole-sites-base_5.csv");
            publishProgress(100);
            return (1.2F);
        }

        protected void onProgressUpdate(Integer... progress) {
            mBarre.setProgress(progress[0]);
            if (progress[0] == 100) {
                mBarre.setVisibility(View.GONE);
            }
        }

        protected void onPostExecute(Float ok) {
            ecouteur.onChargementTermine();
        }

        private void importBaseDeDonnees(List<String[]> base, String nom) {
            String csvSplitBy = ";";
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("bases de donnees/" + nom), StandardCharsets.UTF_8));
                String mLine;
                while ((mLine = reader.readLine()) != null) {
                    String[] row = mLine.split(csvSplitBy);
                    base.add(row);
                }
            } catch (IOException e) {
                etatTexte.setText(R.string.bug_donnee);
                Log.e(TAG, "importBaseDeDonnees: ", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        etatTexte.setText(R.string.bug_donnee);
                        Log.e(TAG, "importBaseDeDonnees: ", e);
                    }
                }
            }

        }

    }

    //Recherche
    private class Recherche_antenne extends AsyncTask<Boolean, Integer, Boolean> {
        // ProgressBar à mettre à jour
        private final ProgressBar mBarre;

        private final ChargementListener ecouteur = new ChargementListener();
        // constructeur, fournir la ProgressBar concernée

        Recherche_antenne(ProgressBar barre) {
            this.mBarre = barre;
        }

        @Override
        protected Boolean doInBackground(Boolean... toutesAntennes) {
            suprMarqueurs();
            boolean xy = toutesAntennes[0];
            List<Integer> indexCoordonnees = rechercheB();
            if (Boolean.parseBoolean(Arrays.toString(toutesAntennes))) {
                publishProgress(50);
            } else {
                publishProgress(60);
            }
            List<Integer> nbprem = new ArrayList<>();
            SharedPreferences pref = getSharedPreferences(fichierReglage, Context.MODE_PRIVATE);
            if (pref.getBoolean("2G", true)) {
                nbprem.add(0);
            }
            if (pref.getBoolean("3G", true)) {
                nbprem.add(1);
            }
            if (pref.getBoolean("4G", true)) {
                nbprem.add(2);
            }
            if (pref.getBoolean("5G", true)) {
                nbprem.add(3);
            }
            if (nbprem.size() < 4) {
                for (Iterator<Integer> iterator = indexCoordonnees.iterator(); iterator.hasNext(); ) {
                    Integer element = iterator.next();
                    int x = 0;
                    for (Integer panneau : nbprem) {
                        if (Integer.parseInt(base3.get(element)[panneau]) == 1) {
                            x = 1;
                        }
                    }
                    if (x == 0) {
                        iterator.remove();
                    }
                }
            }
            if (!indexCoordonnees.isEmpty()) {
                publishProgress(65);
                antenneProche = new ArrayList<>();
                double distance = 10000000D; //beaucoup? tout dépend du point de vue
                for (int antenne = 1; antenne < indexCoordonnees.size(); antenne++) {
                    if (distance > position.getPosition().distanceToAsDouble(new GeoPoint(parseDouble(base2.get(indexCoordonnees.get(antenne))[1]), parseDouble(base2.get(indexCoordonnees.get(antenne))[0])))) {
                        distance = position.getPosition().distanceToAsDouble(new GeoPoint(parseDouble(base2.get(indexCoordonnees.get(antenne))[1]), parseDouble(base2.get(indexCoordonnees.get(antenne))[0])));
                        antenneProche = new ArrayList<>();
                        antenneProche.add(Double.parseDouble(base2.get(indexCoordonnees.get(antenne))[0]));//LONGITUDE
                        antenneProche.add(Double.parseDouble(base2.get(indexCoordonnees.get(antenne))[1]));//LATITUDE
                        antenneProche.add((indexCoordonnees.get(antenne)).doubleValue());//indice dans la base de données
                        antenneProche.add(distance);
                        //TODO: optimiser ce bout de code
                    }
                }
                if (xy) {
                    if (!marqueurs.isEmpty()) {
                        suprMarqueurs();
                    }
                    for (Integer element : indexCoordonnees) {
                        GeoPoint pos = new GeoPoint(parseDouble(base2.get(element)[1]), parseDouble(base2.get(element)[0]));
                        if (!positionAntenne.getPosition().equals(pos)) {
                            marqueurs.add(new Marker(mMap));
                            marqueurs.get(marqueurs.size() - 1).setPosition(pos);
                            marqueurs.get(marqueurs.size() - 1).setIcon(getResources().getDrawable(R.drawable.autre_antenne));
                            marqueurs.get(marqueurs.size() - 1).setAnchor(0.32F, 0.68F);
                            marqueurs.get(marqueurs.size() - 1).setSnippet(String.format("Position: %s; %s distance: %sm, %s", marqueurs.get(marqueurs.size() - 1).getPosition().getLatitude(), marqueurs.get(marqueurs.size() - 1).getPosition().getLongitude(), position.getPosition().distanceToAsDouble(pos), type(element)));
                        }
                    }
                    publishProgress(100);
                } else {
                    publishProgress(95);
                    type = type(antenneProche.get(2).intValue());
                    publishProgress(100);
                }
                return (true);
            }

            publishProgress(100);
            Log.e(TAG, "recherche ratée ");
            return (false);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mBarre.setProgress(progress[0]);
            if (progress[0] == 100) {
                mBarre.setVisibility(View.GONE);
            }
        }

        protected void onPostExecute(Boolean ok) {
            ecouteur.onAntenneTrouvee(antenneProche, type, ok);
        }

    }

    public class LongPressMapOverlay extends Overlay {

        @Override
        public void draw(Canvas c, MapView m, boolean shadow) {
            // Rien à dessiner
        }

        @Override
        public boolean onLongPress(MotionEvent event, MapView map) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Projection projection = map.getProjection();
                GeoPoint positionA = (GeoPoint) projection.fromPixels(
                        (int) event.getX(), (int) event.getY());
                position.setPosition(positionA);
                position.setVisible(true);
                position.setSnippet(textloc(position.getPosition().getLatitude(), position.getPosition().getLongitude()));
                locMoi = position.getSnippet();
                anciennesPositions.add(position.getPosition());
                position.setVisible(true);
                arreterLocalisation();
                montrerCacher(true);
                montrerCacher(false);
                suprMarqueurs();
                map.invalidate();
                locMoi = String.format("Latitude: %s %nLongitude: %s %n%s", position.getPosition().getLatitude(), position.getPosition().getLongitude(), getAddressFromLocation(positionA.getLatitude(), positionA.getLongitude()));
            }
            return true;
        }

    }

    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, AD_UNIT_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                Principal.this.interstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        Principal.this.interstitialAd = null;
                        AlertDialog.Builder constructeur = new AlertDialog.Builder(Principal.this);
                        constructeur.setTitle("Nous vous remercions");
                        constructeur.setMessage("Grâce à votre aide, IPIC&cie pourra continuer à prospérer et à vous fournir un service de qualité");
                        constructeur.setNeutralButton("Je suis quelqu'un de bon", (dialogInterface, i) -> preparePub());
                        constructeur.show();
                        Log.d("TAG", "The ad was dismissed.");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        // Called when fullscreen content failed to show.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        Principal.this.interstitialAd = null;
                        Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        Log.d("TAG", "The ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                interstitialAd = null;
                String error = String.format(Locale.getDefault(), "domain: %s, code: %d, message: %s", loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                Log.e(TAG, "onAdFailedToLoad: "+error);
            }
        });
    }

    public void preparePub() {
        if (interstitialAd == null) {
            loadAd();
        }
    }

    public void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            preparePub();
        }
    }

    public String textloc(Double latitude, Double longitude) {
        String adresse = getAddressFromLocation(latitude, longitude);
        if ((adresse != null)) {
            return getString(R.string.text_loc_adresse, latitude, longitude, adresse);
        } else {
            return getString(R.string.text_loc, latitude, longitude);
        }
    }

    public void boussole() {
        boussole = new ImageView(Principal.this);
        boussole.setImageDrawable(getDrawable(R.drawable.boussole));
        AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
        constructeur.setTitle("Boussole");
        constructeur.setView(boussole);
        constructeur.setOnDismissListener(dialog -> sensorManager.unregisterListener(Principal.this));
        constructeur.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        Log.d(TAG, "onSensorChanged: " + event);
        float degree = Math.round(event.values[0]);
        // rotation animation - reverse turn degree degrees
        RotateAnimation ra = new RotateAnimation(
                DegreeStart,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // set the compass animation after the end of the reservation status
        ra.setFillAfter(true);
        // set how long the animation for the compass image will take place
        ra.setDuration(210);
        // Start animation of compass image
        boussole.startAnimation(ra);
        DegreeStart = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // inutile
    }

    public void montrerCacher(Boolean cacher) {
        Button rechercher = findViewById(R.id.chercher);
        Button centrePos = findViewById(R.id.centrer_sur_position);
        Button centreAnt = findViewById(R.id.centrer_sur_antenne);
        Button toutesAnt = findViewById(R.id.liste_antennes);
        Button troisptitspoints = findViewById(R.id.trois_petits_points);
        com.google.android.material.floatingactionbutton.FloatingActionButton iti = findViewById(R.id.itineraire);
        if (Boolean.TRUE.equals(cacher)) {
            //cache: chemin, centrer sur Ant, antennes à Prox, trois ., marqueur antenne
            centreAnt.setVisibility(View.GONE);
            toutesAnt.setVisibility(View.GONE);
            troisptitspoints.setVisibility(View.GONE);
            iti.setVisibility(View.GONE);
            locAntenne = null;
            positionAntenne.setVisible(false);
        } else {
            //affiche: chercher, centrer sur position
            rechercher.setVisibility(VISIBLE);
            centrePos.setVisibility(VISIBLE);
        }
    }

    public void itineraire(GeoPoint debut, GeoPoint arrivee, String moyen) {
        final JSONObject[] chemin = new JSONObject[1];
        String APIkey = "5b3ce3597851110001cf62489ebce80bfc3e4f339a54524a74f0264d";
        String depart = String.format("%s,%s", debut.getLongitude(), debut.getLatitude());
        String arrive = String.format("%s,%s", arrivee.getLongitude(), arrivee.getLatitude());
        String url = String.format("https://api.openrouteservice.org/v2/directions/%s?api_key=%s&start=%s&end=%s", moyen, APIkey, depart, arrive);
        instructIti = new ArrayList<>();
        com.google.android.material.floatingactionbutton.FloatingActionButton iti = findViewById(R.id.itineraire);
        Log.d("Debug itinéraire", url);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(Principal.this);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseGeoJSON(String.valueOf(response));
            itineraireOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMap, null, null, kmlDocument);
            mMap.getOverlays().add(itineraireOverlay);
            mMap.invalidate();
            Toast.makeText(this, getString(R.string.itineraire_dispo), Toast.LENGTH_SHORT).show();
            JSONObject array = null;
            JSONObject array2;
            JSONArray array3;
            Log.d(TAG, "itineraire: "+itineraireOverlay.getItems());
            try {
                array = (JSONObject)response.getJSONArray("features").get(0);
                array2 = array.getJSONObject("properties");
                array2 = (JSONObject) array2.getJSONArray("segments").get(0);
                array3 = array2.getJSONArray("steps");
                for(int indice=0; indice< array3.length();indice++){
                    JSONObject array4 = (JSONObject) array3.get(indice);
                    instructIti.add(array4.getString("instruction"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("itineraire", "ça fonctionne!" + array);
            iti.setVisibility(View.VISIBLE);
            indiceInstruction = 0;

        }, error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Log.d(TAG, "itininéraire: ça ne fonctionne pas");
            }
            error.printStackTrace();
            Toast.makeText(this, getString(R.string.itineraire_pas_dispo), Toast.LENGTH_LONG).show();
        });
        //add above request to queue
        queue.add(req);
        Log.d(TAG, "itineraire: requête envoyée");
    }

    public void checkupdate() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                /*try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE , Principal.this, RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }*/
                AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
                constructeur.setTitle("IMPORTANT");
                constructeur.setMessage("Une mise à jour est disponible, allez vite la télécharger!");
                constructeur.show();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });

    }

    public void afficheInstructions(){
        if (instructIti != null){
            AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
            constructeur.setTitle("Itinéraire");
            LayoutInflater inflater = this.getLayoutInflater();
            View v = inflater.inflate(R.layout.itineraire, null);
            constructeur.setView(v);
            TextView instructions = v.findViewById(R.id.instructions);
            instructions.setText(instructIti.get(indiceInstruction));
            Button precedent = v.findViewById(R.id.précédent);
            Button suivant = v.findViewById(R.id.suivant);
            precedent.setOnClickListener(vue -> {
                if (indiceInstruction>0){
                    indiceInstruction -=1;
                    instructions.setText(instructIti.get(indiceInstruction));
                }
            });
            suivant.setOnClickListener(vue -> {
                if (indiceInstruction + 1 < instructIti.size()){
                    indiceInstruction += 1;
                    instructions.setText(instructIti.get(indiceInstruction));
                }
            });
            constructeur.show();
        }else{
            choixLocomotion();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: app download failed");
            }
        }
    }
}