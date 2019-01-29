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
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.appitem, null);
        }

        ImageView iconView = convertView.findViewById(R.id.iconView);
        TextView nameView = convertView.findViewById(R.id.nameView);

        App app = appsList.get(position);

        iconView.setImageDrawable(app.getIcon());
        nameView.setText(app.getName());
        return convertView;
    }
}
