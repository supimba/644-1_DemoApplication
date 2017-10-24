package ch.hevs.android.demoapplication.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.db.async.client.GetClient;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.fragment.account.AccountsFragment;
import ch.hevs.android.demoapplication.ui.fragment.client.ClientsFragment;
import ch.hevs.android.demoapplication.ui.fragment.MainFragment;
import ch.hevs.android.demoapplication.ui.fragment.client.EditClientFragment;
import ch.hevs.android.demoapplication.ui.fragment.transaction.TransactionFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = getClass().getSimpleName();

    public static final String PREFS_NAME = "SharedPrefs";
    public static final String PREFS_USER = "LoggedIn";
    public static final String PREFS_ADM = "UserPermission";
    public static final String PREFS_LNG = "Language";

    private Boolean admin;
    private String loggedInEmail;
    private ClientEntity loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        admin = settings.getBoolean(PREFS_ADM, false);
        loggedInEmail = settings.getString(PREFS_USER, null);

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = MainFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment, "main").commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        prepareDrawerMenu(navigationView.getMenu());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;
        String fragmentTag = null;

        if (id == R.id.nav_clients) {
            fragmentClass = ClientsFragment.class;
            fragmentTag = "clients";
        } else if (id == R.id.nav_accounts) {
            fragmentClass = AccountsFragment.class;
            fragmentTag = "accounts";
        }
        else if (id == R.id.nav_transaction) {
            fragmentClass = TransactionFragment.class;
            fragmentTag = "transaction";
        } else if (id == R.id.nav_client) {
            fragmentTag = "client";
        }
        try {
            if (fragmentTag.equals("client")) {
                fragment = EditClientFragment.newInstance(loggedIn);
            } else {
                fragment = (Fragment) fragmentClass.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment, fragmentTag).addToBackStack("main").commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setActionBarTitle(String title){
        setTitle(title);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getCurrentLocale(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return c.getResources().getConfiguration().getLocales().get(0);
        } else {
            return c.getResources().getConfiguration().locale;
        }
    }

    public void OnLanguageChange(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(new Locale(lang));
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void prepareDrawerMenu(Menu menu) {
        MenuItem client = menu.findItem(R.id.nav_client);
        MenuItem clients = menu.findItem(R.id.nav_clients);

        if(admin) {
            client.setVisible(false);
        } else {
            try {
                loggedIn = new GetClient(getApplicationContext()).execute(loggedInEmail).get();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            clients.setVisible(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.action_logout));
            alertDialog.setCancelable(false);
            alertDialog.setMessage(getString(R.string.logout_msg));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_logout), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logout();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS_NAME, 0).edit();
        editor.remove(PREFS_USER);
        editor.remove(PREFS_ADM);
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
