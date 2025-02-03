package com.example.infotarghe;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.infotarghe.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inizializza il layout con ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura la Toolbar come ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurazione della BottomNavigationView e NavController
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_registra, R.id.navigation_rileva)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Aggiorna dinamicamente titolo e sottotitolo
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            TextView toolbarSubtitle = findViewById(R.id.toolbar_subtitle);

            switch (destination.getId()) {
                case R.id.navigation_home:
                    toolbarTitle.setText("INFOTARGHE");
                    toolbarSubtitle.setText("PROGETTO SISTEMI DIGITALI M");
                    toolbarSubtitle.setVisibility(View.VISIBLE);
                    break;

                case R.id.navigation_rileva:
                    toolbarTitle.setText("SCANNER");
                    toolbarSubtitle.setText("INQUADRA UNA TARGA");
                    toolbarSubtitle.setVisibility(View.VISIBLE);
                    break;

                case R.id.navigation_registra:
                    toolbarTitle.setText("LISTA TARGHE");
                    toolbarSubtitle.setVisibility(View.GONE);
                    break;

                default:
                    toolbarTitle.setText("INFOTARGHE");
                    toolbarSubtitle.setVisibility(View.GONE);
                    break;
            }
        });
    }
}

