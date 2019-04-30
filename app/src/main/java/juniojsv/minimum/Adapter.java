package juniojsv.minimum;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Adapter extends BaseAdapter {
    private final Context context;
    private final List<App> appsList;

    Adapter(Context context, List<App> appsList) {
        this.context = context;
        this.appsList = appsList;
    }

    @Override
    public int getCount() {
        return appsList.size();
    }

    @Override
    public Object getItem(int position) {
        return appsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return appsList.indexOf(appsList.get(position));
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = View.inflate(context, R.layout.app_view, null);
        }

        ImageView iconView = view.findViewById(R.id.iconView);
        TextView nameView = view.findViewById(R.id.nameView);

        App app = appsList.get(position);

        iconView.setImageDrawable(app.getIcon());
        nameView.setText(app.getPackageLabel());
        return view;
    }
}
