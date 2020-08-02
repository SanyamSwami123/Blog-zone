package com.example.zone.userProfile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zone.Model.Blog;
import com.example.zone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.Date;
import java.util.List;


public class UserAdaptor extends RecyclerView.Adapter<UserAdaptor.ViewHolder> {

    private Context context;
    private List<Blog> blogList;


    public UserAdaptor(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_row, parent, false);


        return new UserAdaptor.ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdaptor.ViewHolder holder, final int position) {


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



         holder.button.setOnClickListener(new View.OnClickListener() {
          @Override
            public void onClick(View v) {

              final CharSequence[] options = { "Yes", "No"};
              AlertDialog.Builder builder = new AlertDialog.Builder(context);
              builder.setTitle("Are You Sure...You Want To Delete?");
              builder.setItems(options,new DialogInterface.OnClickListener() {

                  @Override
                  public void onClick(DialogInterface dialog, int which) {

                      if (options[which].equals("Yes")){

                          DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                          Query deleteQuery = ref.child("MBlog").orderByChild("postid").equalTo(blog.getPostid());

                          deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                  for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                      appleSnapshot.getRef().removeValue();
                                  }
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError) {

                              }
                          });

                      }
                      else if (options[which].equals("No")){
                          dialog.dismiss();
                      }

                  }
              });
              builder.show();


          }
          });



    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public   Button button;
        public TextView title;
        public TextView desc;
        public TextView timestamp, likes;
        public ImageView image, like;
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
            button = (Button) view.findViewById(R.id.deleteitem);

            userid = null;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                     Blog blog = blogList.get(position);

                        Intent intent = new Intent(context, photoDetailActivity.class);


                    intent.putExtra("image",blog.getImage());
                    intent.putExtra("desc",blog.getDesc());
                    intent.putExtra("title",blog.getTitle());
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
                    imageView.setImageResource(R.drawable.small_like);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.small_liked);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrLikes(final TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);

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



}
