package com.example.zone.userProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zone.Model.Blog;
import com.example.zone.PhotoActivity.PostListActivity;
import com.example.zone.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProfile extends AppCompatActivity implements View.OnClickListener{

    private ImageView userProfileImage;
    private TextView usernamee;
    private TextView lastnamee;
    private TextView posts;
    private RecyclerView recyclerView;


    private UserAdaptor userAdaptor;
    private FirebaseUser mUser;
    private String currentUserId;


    private ImageButton photoLib;
    private Button edit;
    private ImageButton profile;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private List<Blog> blogList;
    private FirebaseDatabase mDatabase;

    private TextView toggleToLinearLayout;
    private TextView bio;
    private TextView notifier;
    private TextView followers;
    private TextView Usaves;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth=FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mUser =  FirebaseAuth.getInstance().getCurrentUser();



        mDatabase = FirebaseDatabase.getInstance();


        usernamee = (TextView) findViewById(R.id.usernamee);
        lastnamee = (TextView) findViewById(R.id.lastnamee);
        userProfileImage = (ImageView) findViewById(R.id.profileimage);
        edit = (Button) findViewById(R.id.edit);
        bio = (TextView) findViewById(R.id.bio);
        posts = (TextView) findViewById(R.id.posts);
        notifier =(TextView) findViewById(R.id.notif);


        recyclerView = (RecyclerView) findViewById(R.id.userpostrecycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(linearLayoutManager);

        blogList = new ArrayList<>();

        userAdaptor = new UserAdaptor(UserProfile.this,blogList);
        recyclerView.setAdapter(userAdaptor);





        RetrieveUserInfo();
        getcountpost();
        getcountnotification();
        myposts();
        activities();

        toggleToLinearLayout = (TextView) findViewById(R.id.toggle);
        toggleToLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfile.this,UserOwnPhotoRecyclerActivity.class));
            }
        });

        Usaves = (TextView) findViewById(R.id.saves);
        Usaves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfile.this,UserSavesActivity.class));
            }
        });


    edit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            startActivity(new Intent(UserProfile.this, userEditActivity.class));
        }
    });

    }


    private void activities()
    {

        photoLib= (ImageButton) findViewById(R.id.imagedirectory);
        profile = (ImageButton) findViewById(R.id.Userprofile);

        photoLib.setOnClickListener(this);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {


        switch (v.getId())
        {
            case R.id.imagedirectory:
                startActivity(new Intent(UserProfile.this, PostListActivity.class));
                finish();
                break;

        }

    }






    private void getcountnotification( ) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("notification").child(currentUserId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifier.setText(dataSnapshot.getChildrenCount()+"" );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getcountpost()
       {
           DatabaseReference reference= FirebaseDatabase.getInstance().getReference("MBlog");
           reference.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                   int i=0;
                   for (DataSnapshot snapshot : dataSnapshot.getChildren())
                   {
                       Blog blog = snapshot.getValue(Blog.class);
                       if (blog.getUserid().equals(currentUserId))
                       {
                           i++;
                       }
                       posts.setText(""+i);
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
       }

       private void myposts()
       {
           DatabaseReference reference= FirebaseDatabase.getInstance().getReference("MBlog");
           reference.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                   blogList.clear();
                   for (DataSnapshot snapshot : dataSnapshot.getChildren())
                   {
                       Blog blog = snapshot.getValue(Blog.class);
                       if (blog.getUserid().equals(currentUserId))
                       {
                           blogList.add(blog);
                       }
                   }
                   Collections.reverse(blogList);
                   userAdaptor.notifyDataSetChanged();
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
       }



    private void RetrieveUserInfo() {


        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("MUsers").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("firstname") &&
                                (dataSnapshot.hasChild("image")))) {

                            String retrieveUserName = dataSnapshot.child("firstname").getValue().toString();
                            String retrieveUserlastname = dataSnapshot.child("lastname").getValue().toString();
                            String retrieveUserbio = dataSnapshot.child("bio").getValue().toString();

                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            usernamee.setText("@"+retrieveUserName);
                            lastnamee.setText(" "+retrieveUserlastname);

                            bio.setText("BIO: " + retrieveUserbio);

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        }


                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }






}












