package juniojsv.minimum;

import java.util.List;

public interface MinimumInterface {

    void notifyAdapter();

    List<App> getAppsList();

    void onSearchAppsStarting();

    void onSearchAppsFinished(List<App> newAppsList);
}
