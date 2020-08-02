package com.example.zone.userProfile;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zone.R;
import com.squareup.picasso.Picasso;


public class photoDetailActivity extends AppCompatActivity {

    public TextView title;
    public TextView desc;
    public ImageView image;
    private Bundle extras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);


        extras = getIntent().getExtras();

        title = (TextView) findViewById(R.id.postTitleList);
        desc = (TextView) findViewById(R.id.postTextList);
        image = (ImageView) findViewById(R.id.postImageList);


        if (extras != null) {
            title.setText(extras.getString("title"));
            desc.setText(extras.getString("desc"));


            Picasso.get()
                    .load(extras.getString("image"))
                    .into(image);


        }



    }



}
