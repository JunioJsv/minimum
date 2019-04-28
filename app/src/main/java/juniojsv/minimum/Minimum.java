package juniojsv.minimum;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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

public class Minimum extends AppCompatActivity {
    static List<App> appsList = new ArrayList<>(0);
    public static ListView appsListView;
    public static Adapter adapter;
    public static ProgressBar progressBar;
    private SearchApps searchApps = new SearchApps(this);
    private TakePhoto takePhoto;
    private MoveFileTo moveFileTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.minimum);

        adapter =  new Adapter(this, appsList);
        appsListView = findViewById(R.id.appsListView);
        progressBar = findViewById(R.id.progressBar);
        searchApps.execute();

        BroadcastReceiver checkAppsList = new CheckAppsList();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        this.registerReceiver(checkAppsList, intentFilter);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.minimum_shortcut, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int dial_id = 2131165236;
        final int camera_id = 2131165220;

        switch (item.getItemId()) {
            case dial_id:
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                startActivity(dialIntent);
                break;
            case camera_id:
                takePhoto = new TakePhoto(this);
                takePhoto.Capture();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO & !Build.MANUFACTURER.equals("LGE")) {

            try {
                moveFileTo = new MoveFileTo(new File(takePhoto.getCamera().getCameraBitmapPath()), new File(Environment.getExternalStorageDirectory().getPath() + "/Minimum"));
                moveFileTo.execute();
            } catch (Exception error) {
                error.printStackTrace();
            }

        } else if(requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO & Build.MANUFACTURER.equals("LGE")) {

            try {
                File file = new File(takePhoto.getCamera().getCameraBitmapPath());
                file.delete();

            } catch (Exception error) {
                error.printStackTrace();
            }
        }
    }

    static void startAdapter(){
        if (appsListView.getAdapter() == null)  {
            appsListView.setAdapter(adapter);
        } else adapter.notifyDataSetChanged();
    }

    static void setAppsList(List<App> appsListCache) {
        if (appsList.size() != 0) {
            appsList.clear();
        }
        appsList.addAll(appsListCache);
    }
}
