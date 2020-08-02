package com.example.zone.userProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.zone.Model.Blog;
import com.example.zone.PhotoActivity.BlogRecyclerAdapter;
import com.example.zone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserSavesActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private List<Blog> blogList;
    private List<String> mysaves;
    private RecyclerView recyclerView;

    private  BlogRecyclerAdapter blogRecyclerAdapter;
    private TextView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_saves);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        back = findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserSavesActivity.this, UserProfile.class));
                finish();
            }
        });

        mUser =  FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();


        recyclerView = (RecyclerView) findViewById(R.id.savesrecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(linearLayoutManager);

        blogList = new ArrayList<>();

        blogRecyclerAdapter = new BlogRecyclerAdapter(UserSavesActivity.this,blogList);
        recyclerView.setAdapter(blogRecyclerAdapter);

        my_saves();
    }

    private void my_saves(){
        mysaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("saves").child(mUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mysaves.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {

                        mysaves.add(snapshot.getKey());

                }
                read_saves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void read_saves() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("MBlog");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                blogList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Blog blog = snapshot.getValue(Blog.class);
                    for (String id : mysaves)
                    if (blog.getPostid().equals(id))
                    {
                        blogList.add(blog);
                    }
                }
              Collections.reverse(blogList);
                blogRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
