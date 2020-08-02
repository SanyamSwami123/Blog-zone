package com.example.zone.userProfile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.zone.Activities.CreateAccountActivity;
import com.example.zone.Activities.MainActivity;
import com.example.zone.PhotoActivity.PostListActivity;
import com.example.zone.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class userEditActivity extends AppCompatActivity {
    private EditText firstName;
    private EditText lastName;
    private EditText bio;
    private ImageButton profilePic;
    private Button createAccountBtn;


    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private StorageReference mFirebaseStorage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private Uri resultUri = null;
    private final static int GALLERY_CODE = 1;
    private TextView back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("MUsers");

        mAuth = FirebaseAuth.getInstance();

        mFirebaseStorage = FirebaseStorage.getInstance().getReference().child("MBlog_Profile_Pics");

        mProgressDialog = new ProgressDialog(this);

        firstName = (EditText) findViewById(R.id.firstNameAct);
        lastName = (EditText) findViewById(R.id.lastNameAct);

        bio = (EditText) findViewById(R.id.bio);
        profilePic = (ImageButton) findViewById(R.id.profilePic);
        createAccountBtn = (Button) findViewById(R.id.createAccoutAct);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });



        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        back = findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userEditActivity.this, UserProfile.class));
                finish();
            }
        });
    }


    private void createNewAccount() {

        final String name = firstName.getText().toString().trim();
        final String lname = lastName.getText().toString().trim();
        final String userBio = bio.getText().toString().trim();



            mProgressDialog.setMessage(" Posting Edits Please Wait");
            mProgressDialog.show();


                                StorageReference imagePath = mFirebaseStorage.child("MBlog_Profile_Pics")
                                        .child(resultUri.getLastPathSegment());

                                imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String userid = mAuth.getCurrentUser().getUid();

                                        DatabaseReference currenUserDb = mDatabaseReference.child(userid);
                                        currenUserDb.child("firstname").setValue(name);
                                        currenUserDb.child("lastname").setValue(lname);
                                        currenUserDb.child("image").setValue(resultUri.toString());
                                        currenUserDb.child("bio").setValue(userBio);

                                        mProgressDialog.dismiss();

                                        //send users to postList
                                        Intent intent = new Intent(userEditActivity.this, UserProfile.class );
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();


                                    }
                                });




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            Uri mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);



        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();

                profilePic.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
