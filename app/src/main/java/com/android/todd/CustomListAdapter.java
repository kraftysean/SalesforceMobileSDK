package com.android.todd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.todd.util.FlushedInputStream;
import com.android.todd.util.ISO8601;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomListAdapter extends ArrayAdapter<FeedItem> {
    private static LayoutInflater inflater = null;
    private final Context context;
    private final List<FeedItem> newsFeed;

    public CustomListAdapter(Context context, ArrayList<FeedItem> items) {

        super(context, -1, items);
        this.context = context;
        this.newsFeed = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (convertView == null)
            rowView = inflater.inflate(R.layout.chatter_list_item, parent, false);
        TextView tvName = (TextView) rowView.findViewById(R.id.tvLine1);
        TextView tvMessage = (TextView) rowView.findViewById(R.id.tvLine2);
        TextView tvTime = (TextView) rowView.findViewById(R.id.tvTime);
        FeedItem feedItem = newsFeed.get(position);
        tvName.setText(feedItem.getName());
        tvMessage.setText(feedItem.getMessage());
        tvTime.setText(makeDatePretty(feedItem.getCreatedDate()));
        ImageView ivIcon = (ImageView) rowView.findViewById(R.id.ivIcon);
        new ImageDownloader(ivIcon).execute(feedItem.getImage());
        Log.d("PHOTO_URL", feedItem.getImage());

        return rowView;
    }

    private String makeDatePretty(String createdDate) {
        Calendar calendar = ISO8601.parse(createdDate);
        PrettyTime pTime = new PrettyTime();
        return pTime.format(calendar);
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownloader(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {

            String urlStr = urls[0];
            Bitmap img = null;

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlStr);
            HttpResponse response;
            try {
                response = client.execute(request);
                HttpEntity entity = response.getEntity();
//                BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
//                InputStream inputStream = bufferedEntity.getContent();
                img = BitmapFactory.decodeStream(new FlushedInputStream(entity.getContent()));
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return img;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}