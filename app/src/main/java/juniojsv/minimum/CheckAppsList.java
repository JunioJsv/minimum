package juniojsv.minimum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CheckAppsList extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("APPLIST", "CHANGED");
        SearchApps searchApps = new SearchApps(context);
        searchApps.execute();
    }
}
