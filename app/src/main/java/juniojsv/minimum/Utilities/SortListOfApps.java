package juniojsv.minimum.Utilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import juniojsv.minimum.App;

public class SortListOfApps {

    public SortListOfApps(List<App> listToSort) {

        for (App app : listToSort) {
            if (app.getPackageLabel().charAt(0) != Character.toUpperCase(app.getPackageLabel().charAt(0))) {
                app.setPackageLabel(app.getPackageLabel().substring(0, 1).toUpperCase() + app.getPackageLabel().substring(1));
            }
        }

        Collections.sort(listToSort, new Comparator<App>() {
            @Override
            public int compare(App app1, App app2) {
                String app1Label = app1.getPackageLabel();
                String app2Label = app2.getPackageLabel();
                return app1Label.compareTo(app2Label);
            }
        });

    }
}
