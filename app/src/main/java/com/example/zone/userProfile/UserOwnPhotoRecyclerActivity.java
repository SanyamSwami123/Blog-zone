package com.example.zone.userProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.zone.Model.Blog;
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

public class UserOwnPhotoRecyclerActivity extends AppCompatActivity {


    private RecyclerView recyclerView;

    private UserOwnPhotoAdaptor userOwnPhotoAdaptor;
    private FirebaseUser mUser;
    private String currentUserId;
    private TextView back;
    private FirebaseAuth mAuth;
    private List<Blog> blogList;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_own_photo_recycler);

        mAuth=FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mUser =  FirebaseAuth.getInstance().getCurrentUser();


        mDatabase = FirebaseDatabase.getInstance();


        recyclerView = (RecyclerView) findViewById(R.id.userpostrecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        blogList = new ArrayList<>();

        userOwnPhotoAdaptor = new UserOwnPhotoAdaptor(UserOwnPhotoRecyclerActivity.this,blogList);
        recyclerView.setAdapter(userOwnPhotoAdaptor);

        myposts();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        back=findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserOwnPhotoRecyclerActivity.this, UserProfile.class));
                finish();
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
                userOwnPhotoAdaptor.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
