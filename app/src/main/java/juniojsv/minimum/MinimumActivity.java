package juniojsv.minimum;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import juniojsv.minimum.Utilities.MoveFileTo;

public class MinimumActivity extends AppCompatActivity implements MinimumInterface{
    public List<App> appsList = new ArrayList<>(0);
    public ListView appsListView;
    public Adapter adapter;
    public ProgressBar loading;
    public static SharedPreferences settings;
    private BroadcastReceiver checkAppsList;
    private TakePhoto takePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences("Settings", MODE_PRIVATE);
        applySettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.minimum_activity);

        //findViewById:
        appsListView = findViewById(R.id.appsListView);
        loading = findViewById(R.id.loading);
        //end

        SearchApps searchApps = new SearchApps(this.getPackageManager(), this);
        adapter =  new Adapter(this, appsList);
        searchApps.execute();

        checkAppsList = new CheckAppsList(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        this.registerReceiver(checkAppsList, intentFilter);

        //onClick
        appsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent appIntent = appsList.get(position).getIntent();
                startActivity(appIntent);
            }
        });
        appsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Uri pkgUri = Uri.parse("package:" + appsList.get(position).getPackageName());
                Intent uninstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, pkgUri);
                startActivity(uninstall);
                return true;
            }
        });
        //end
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.minimum_shortcut, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.dial_shortcut:
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                startActivity(dialIntent);
                break;
            case R.id.camera_shortcut:
                takePhoto = new TakePhoto(this);
                takePhoto.capture();
                break;
            case R.id.setting_shortcut:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Nope
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SettingsActivity.needRestart) {
            SettingsActivity.needRestart = false;
            unregisterReceiver(checkAppsList);
            recreate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO & !Build.MANUFACTURER.equals("LGE")) {

            try {
                MoveFileTo moveFileTo = new MoveFileTo(new File(takePhoto.getCamera().getCameraBitmapPath()), new File(Environment.getExternalStorageDirectory().getPath() + "/Minimum"));
                moveFileTo.execute();
            } catch (Exception error) {
                error.printStackTrace();
            }

        } else if(requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO & Build.MANUFACTURER.equals("LGE")) {

            try {
                File file = new File(takePhoto.getCamera().getCameraBitmapPath());
                //noinspection ResultOfMethodCallIgnored
                file.delete();

            } catch (Exception error) {
                error.printStackTrace();
            }
        }
    }

    private void applySettings() {
        if (settings.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
        }
    }

    @Override
    public void notifyAdapter() {
        if (appsListView.getAdapter() == null)  {
            appsListView.setAdapter(adapter);
        } else adapter.notifyDataSetChanged();
    }

    @Override
    public List<App> getAppsList() {
        return appsList;
    }

    @Override
    public void onSearchAppsStarting() {
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchAppsFinished(List<App> newAppsList) {
        if (appsList.size() != 0) {
            appsList.clear();
        }
        appsList.addAll(newAppsList);
        notifyAdapter();
        loading.setVisibility(View.GONE);
    }
}
