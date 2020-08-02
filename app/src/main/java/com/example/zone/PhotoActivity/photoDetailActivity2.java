package com.example.zone.PhotoActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zone.R;
import com.example.zone.userProfile.UserProfile;
import com.squareup.picasso.Picasso;

public class photoDetailActivity2 extends AppCompatActivity {
    public TextView title;
    public TextView desc;
    public ImageView image;
    private Bundle extras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail2);

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
