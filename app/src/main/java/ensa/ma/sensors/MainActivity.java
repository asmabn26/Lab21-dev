package ensa.ma.sensors;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ensa.ma.sensors.beans.SensorItem;
import ensa.ma.sensors.ui.sensors.ListSonsorsFragment;

public class MainActivity extends AppCompatActivity
        implements ListSonsorsFragment.OnListFragmentInteractionListener {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_sensors,
                R.id.nav_thermometer,
                R.id.nav_humidity,
                R.id.nav_proximity,
                R.id.nav_magnetic,
                R.id.nav_rotation,
                R.id.nav_compass,
                R.id.nav_share
        )
                .setDrawerLayout(drawer)
                .build();

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                mAppBarConfiguration
        );

        NavigationUI.setupWithNavController(
                navigationView,
                navController
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onListFragmentInteraction(SensorItem item) {
        // Rien à faire ici pour le moment.
    }
}