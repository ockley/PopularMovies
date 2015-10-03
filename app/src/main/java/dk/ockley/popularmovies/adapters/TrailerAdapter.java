package dk.ockley.popularmovies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import dk.ockley.popularmovies.R;
import dk.ockley.popularmovies.models.Trailer;

/**
 * Created by kv on 08/09/15.
 */
public class TrailerAdapter extends ArrayAdapter<Trailer> {
    private Context ctx;

    public TrailerAdapter(Context context, ArrayList<Trailer> resource) {
        super(context, 0, resource);
        ctx = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trailer trailer = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_list_item, parent, false);
        }

        TextView label = (TextView) convertView.findViewById(R.id.trailer_list_item_label);
        label.setText(trailer.getTitle());

        return convertView;
    }
}
