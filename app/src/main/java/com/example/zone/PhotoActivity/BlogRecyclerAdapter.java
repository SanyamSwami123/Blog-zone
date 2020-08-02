package com.example.zone.PhotoActivity;

import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;

import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zone.BuildConfig;
import com.example.zone.Model.Blog;
import com.example.zone.R;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Blog> blogList;
    private DatabaseReference RootRef;



    public BlogRecyclerAdapter(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_row, parent, false);


        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Blog blog = blogList.get(position);
        String imageUrl = null;


        holder.title.setText(blog.getTitle());


        java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
        String formattedDate = dateFormat.format(new Date(Long.valueOf(blog.getTimestamp())).getTime());
        holder.timestamp.setText(formattedDate);


        imageUrl = blog.getImage();


        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_profile);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(holder.image);



        isLiked(blog.getPostid(), holder.like);
        nrLikes(holder.likes, blog.getPostid());

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(blog.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(blog.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        RetrieveUserInfo(holder.userimageprofile, holder.username, holder.lastname, blog.getUserid());


        nrnotify(holder.notifier, blog.getUserid());
        isnotify(blog.getPostid(), holder.notify, blog.getUserid());
        holder.notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.notify.getTag().equals("notify")) {
                    FirebaseDatabase.getInstance().getReference().child("notification").child(blog.getUserid())
                            .child(firebaseUser.getUid()).setValue(true);

                } else {
                    FirebaseDatabase.getInstance().getReference().child("notification").child(blog.getUserid())
                            .child(firebaseUser.getUid()).removeValue();

                }
            }
        });


        holder.PopUpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.share:

                                //option 1 for sending/sharing

                                String shareMessage = "\nLet me recommend you this application\n\n";
                                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";

                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, "Blog Zone(Check App In Google Play Store) "
                                        + shareMessage + "///title ="
                                        + holder.title.getText() + getItemId(position));
                                intent.setType("text/plain");
                                if (intent.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(Intent.createChooser(intent, "Send To"));
                                }

                                break;


                            case R.id.delete:


                                final CharSequence[] options = {"Yes", "No"};
                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Are You Sure...You Want To Delete?");
                                builder.setItems(options, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        if (options[which].equals("Yes")) {

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                            Query deleteQuery = ref.child("MBlog").orderByChild("postid").equalTo(blog.getPostid());

                                            deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                                        appleSnapshot.getRef().removeValue();
                                                        Toast.makeText(context, "Post Deleted...", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        } else if (options[which].equals("No")) {
                                            dialog.dismiss();
                                        }

                                    }
                                });
                                builder.show();

                        }
                        return false;
                    }
                });


                if (!blog.getUserid().equals(firebaseUser.getUid())) {
                    popup.getMenu().findItem(R.id.delete).setVisible(false);
                }

                popup.show();
            }
        });





        issaved(blog.getPostid(), holder.save);
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid()).child(blog.getPostid()).setValue(true);

                    Snackbar.make(holder.title, "Post Saved", Snackbar.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid()).child(blog.getPostid()).removeValue();
                    Snackbar.make(holder.image, "Post Removed", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

    }





    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notifier;
        public TextView lastname;
        public TextView title;
        public TextView desc;
        public TextView timestamp, likes, username;
        public ImageView image, like, userimageprofile, notify, PopUpMenu, save;

        String userid;


        public ViewHolder(View view, Context ctx) {
            super(view);

            context = ctx;

            title = (TextView) view.findViewById(R.id.postTitleList);
            desc = (TextView) view.findViewById(R.id.postTextList);
            image = (ImageView) view.findViewById(R.id.postImageList);
            timestamp = (TextView) view.findViewById(R.id.timestampList);
            like = (ImageView) view.findViewById(R.id.like);
            likes = (TextView) view.findViewById(R.id.likes);
            userimageprofile = (ImageView) view.findViewById(R.id.userimageprofile);
            username = (TextView) view.findViewById(R.id.username);
            lastname = (TextView) view.findViewById(R.id.lastname);
            notify = (ImageView) view.findViewById(R.id.notify);
            userid = null;
            notifier = (TextView) view.findViewById(R.id.notifier);
            //popUpMessage
            PopUpMenu = (ImageView) view.findViewById(R.id.popUpMenu);


            save = (ImageView) view.findViewById(R.id.Save);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();

                    Blog blog = blogList.get(position);
                    Intent intent = new Intent(context, photoDetailActivity2.class);


                    intent.putExtra("image", blog.getImage());
                    intent.putExtra("desc", blog.getDesc());
                    intent.putExtra("title", blog.getTitle());

                    context.startActivity(intent);

                }
            });

        }
    }



    private void isLiked(String postid, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void nrLikes(final TextView likes, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void RetrieveUserInfo(final ImageView imageView, final TextView username, final TextView lastname, String userid) {


        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("MUsers").child(userid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("firstname") &&
                                (dataSnapshot.hasChild("image")))) {

                            String retrieveUserName = dataSnapshot.child("firstname").getValue().toString();
                            String retrieveUserlastname = dataSnapshot.child("lastname").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            username.setText("@" + retrieveUserName);
                            lastname.setText(" " + retrieveUserlastname);
                            Picasso.get().load(retrieveProfileImage).into(imageView);
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void isnotify(String postid, final ImageView imageView, String userid) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("notification").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.notified);
                    imageView.setTag("notified");
                } else {
                    imageView.setImageResource(R.drawable.notify);
                    imageView.setTag("notify");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrnotify(final TextView notifier, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("notification").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifier.setText(dataSnapshot.getChildrenCount() + " Users");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void issaved(String postid, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.saved);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}























