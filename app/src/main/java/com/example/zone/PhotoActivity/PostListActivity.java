package com.example.zone.PhotoActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.zone.Activities.MainActivity;
import com.example.zone.BuildConfig;
import com.example.zone.userProfile.UserProfile;
import com.example.zone.Model.Blog;
import com.example.zone.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostListActivity extends AppCompatActivity implements View.OnClickListener {
    private DatabaseReference mDatabaseReference;
    private RecyclerView recyclerView;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private List<Blog> blogList;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private ImageButton photoLib;
    private ImageButton profile;
    private ImageButton addpost;


    private SwipeRefreshLayout refreshLayout;

    private ShimmerFrameLayout shimmerFrameLayout;


    //listener
    private ChildEventListener childEventListener;

    //add
    private AdView mAdView,mAdView1;
    private InterstitialAd mInterstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        shimmerFrameLayout = findViewById(R.id.shimmer);



        //banner ad first
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //banner ad 2
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView1 = findViewById(R.id.adView1);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);

        //interstitial Add
        intertitialAppLoading();



        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("MBlog");
        mDatabaseReference.keepSynced(true);




        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        blogList = new ArrayList<>();
        postList();


        activities();


        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(new Intent(PostListActivity.this, PostListActivity.class));
                finish();
               refreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add:
                if (mUser != null && mAuth != null) {
                    startActivity(new Intent(PostListActivity.this, AddPostActivity.class));
                }

                break;
            case R.id.action_signout:

                if (mUser != null && mAuth != null) {
                    mAuth.signOut();

                    startActivity(new Intent(PostListActivity.this, MainActivity.class));
                    finish();

                }
                break;

            case R.id.shareapp:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    String shareMessage= "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                   startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                }
        }

        return super.onOptionsItemSelected(item);
    }



   private void postList()
   {

       childEventListener=(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

               Blog blog = dataSnapshot.getValue(Blog.class);

               blogList.add(blog);

               Collections.reverse(blogList);

               blogRecyclerAdapter = new BlogRecyclerAdapter(PostListActivity.this, blogList);
               shimmerFrameLayout.stopShimmer();
               shimmerFrameLayout.setVisibility(View.GONE);

               recyclerView.setVisibility(View.VISIBLE);
               refreshLayout.setVisibility(View.VISIBLE);
               recyclerView.setAdapter(blogRecyclerAdapter);
               blogRecyclerAdapter.notifyDataSetChanged();
           }

           @Override
           public void onChildChanged(DataSnapshot dataSnapshot, String s) {
           }

           @Override
           public void onChildRemoved(DataSnapshot dataSnapshot) {
           }

           @Override
           public void onChildMoved(DataSnapshot dataSnapshot, String s) {
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });

       mDatabaseReference.addChildEventListener(childEventListener);
   }

    private void activities() {

        photoLib = (ImageButton) findViewById(R.id.imagedirectory);
        profile = (ImageButton) findViewById(R.id.Userprofile);
        addpost = (ImageButton) findViewById(R.id.addpost);

        photoLib.setOnClickListener(this);
        profile.setOnClickListener(this);
        addpost.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.imagedirectory:

                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, new RecyclerView.State(), 0);
                break;


            case R.id.Userprofile:
                startActivity(new Intent(PostListActivity.this, UserProfile.class));
                finish();
                break;

            case R.id.addpost:

                startActivity(new Intent(PostListActivity.this, AddPostActivity.class));
                break;

        }


    }


    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();
                }
            });
        }
        else{
        moveTaskToBack(true);
    }
    }


    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmer();

    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmer();
    }
}
