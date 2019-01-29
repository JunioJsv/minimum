package juniojsv.minimum;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Minimum extends AppCompatActivity {
    static List<App> appsList = new ArrayList<>(0);
    static ListView appsListView;
    static Adapter adapter;
    static ProgressBar progressBar;
    SearchApps searchApps = new SearchApps(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.minimun);

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
                Uri pkgUri = Uri.parse("package:" + appsList.get(position).getUninstallName());
                Intent uninstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, pkgUri);
                startActivity(uninstall);
                return true;
            }
        });
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
