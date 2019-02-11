package juniojsv.minimum;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

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
