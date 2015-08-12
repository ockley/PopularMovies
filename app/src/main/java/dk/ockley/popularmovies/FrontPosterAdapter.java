package dk.ockley.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by kv on 29/07/15.
 */
public class FrontPosterAdapter extends ArrayAdapter<String>{
    private Context ctx;

    public FrontPosterAdapter(Context context, ArrayList<String> resource) {
        super(context, 0, resource);
        ctx = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.frontpage_item, parent, false);
        }
        ImageView posterImage = (ImageView) convertView.findViewById(R.id.poster_image);
        String imgUrl = getItem(position);

       if (imgUrl != null) {
           //Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView);
           Picasso.with(ctx).load(imgUrl).into(posterImage);
           Log.d("POPMOVIE", imgUrl + " :: " + posterImage.toString());
        }


        return convertView;
    }
}
