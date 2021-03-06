package com.example.android.gutechnetworking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Map<String, Bitmap> mMap;
    private static final String URL_JSON = "http://services.hanselandpetal.com/feeds/flowers.json";
    private static String PHOTOS_BASE_URL = "http://services.hanselandpetal.com/photos/";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mMap = new HashMap<>();

        progressBar = findViewById(R.id.progressbar);

        if(NetManager.isOnline(this))
        {
            FlowerDownloader flowerDownloader = new FlowerDownloader();
            flowerDownloader.execute(NetManager.getURL(URL_JSON));
           // Toast.makeText(this, "You are online", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show();
        }
    }

    private class FlowerDownloader extends AsyncTask<URL, Void, List<FlowerModel>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        
        @Override
        protected List<FlowerModel> doInBackground(URL... urls) {
             List<FlowerModel> = flowerModelList = Parser.parseJson(NetManager.fetchData(urls[0]));

            return flowerModelList;
        }

        @Override
        protected void onPostExecute(List<FlowerModel> flowerModelList) {
            super.onPostExecute(flowerModelList);
            if(flowerModelList != null) {
             //   Toast.makeText(MainActivity.this, flowerModelList, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                updateDisplay(flowerModelList);
            }else
            {

            }
        }
    }

    private void updateDisplay(List<FlowerModel> flowerModelList)
    {
        ListView lstFlowers = findViewById(R.id.lstFlowers);
       // ArrayAdapter<FlowerModel> adapter = new ArrayAdapter<~>(this, android.R.layout.);
        ArrayAdapter<FlowerModel> adapter = new DisplayFlowers(this, 0, flowerModelList);
        lstFlowers.setAdapter(adapter);
    }

    private class DisplayFlowers extends ArrayAdapter<FlowerModel>
    {
        List<FlowerModel> listOfFlowerObjects;


        public DisplayFlowers(Context context, int resource, List<FlowerModel> objects) {
            super(context, resource, objects);
            this.listOfFlowerObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.layout_for_each_flower, parent, false);

            FlowerModel flower = listOfFlowerObjects.get(position);

            TextView tvFlowerName = convertView.findViewById(R.id.tvFlowerName);
            tvFlowerName.setText(flower.getName());


            ImageView ivFlowerImage = convertView.findViewById(R.id.ivFlowerImage);
            ivFlowerImage.setImageBitmap(flower.getBitmap());
            
            
            if(mMap.containsKey((flower.getPhoto()))) {
                ivFlowerImage.setImageBitmap(mMap.get(flower.getPhoto()));
            }else{
                DataSender dataSender = new DataSender(ivFlowerImage,flower);
                new ImageDownloader().execute(dataSender);

            }

            return convertView;
        }
    }
    
    private class ImageDownloader extends AsyncTask<DataSender, Void, Bitmap>
    {
        DataSender dataSender;

        @Override
        protected Bitmap doInBackground(DataSender... dataSenders) {
            dataSender = dataSenders[0];


              URL url = NetManager.getURL(PHOTOS_BASE_URL+dataSender.flower.getPhoto());
            try
            {
              InputStream inputStream = (InputStream) new URL(url.toString()).getContent();
            return BitmapFactory.decodeStream(inputStream);


            }
            catch (IOException e) {
             e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            dataSender.ivFlowerName.setImageBitmap(bitmap);
            mMap.put(dataSender.flower.getPhoto(), bitmap);

        }
    }

    private class DataSender
    {
        ImageView ivFlowerName;
        FlowerModel flower;

        public DataSender(ImageView imageView, FlowerModel flowerModel) {
            this.ivFlowerName = imageView;
            this.flower = flowerModel;
        }
    }
}
