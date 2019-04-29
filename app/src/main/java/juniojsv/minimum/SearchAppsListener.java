package juniojsv.minimum;

import java.util.List;

interface SearchAppsListener {

    void onAppsLoadingStarting();

    void onAppsLoadingFinished(List<App> appsList);

}
