package com.IPICcie.RESEAU;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private CountDownTimer compte;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bonus = findViewById(R.id.bonus);
        bonus.setOnClickListener(v -> {
            compte.cancel();
            bonus.setVisibility(View.INVISIBLE);
            ImageView image= findViewById(R.id.imageView);
            image.setVisibility(View.INVISIBLE);
            TextView texte= findViewById(R.id.textView2);
            texte.setVisibility(View.VISIBLE);
        });

        compte = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {//nothing to write
            }
            public void onFinish() {
                compte.cancel();
                SharedPreferences pref = getSharedPreferences("Fichier_reglage.xml",Context.MODE_PRIVATE);
                if (pref.getBoolean("bienvenue",false)){
                    Intent intent = new Intent(MainActivity.this, Principal.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this,Bienvenue.class);
                    startActivity(intent);
                }
            }
        }.start();
    }
}


