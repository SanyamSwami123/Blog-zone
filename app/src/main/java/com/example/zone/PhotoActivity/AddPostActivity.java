package com.example.zone.PhotoActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.zone.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {


    private ImageButton mPostImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitButton;
    private StorageReference mStorage;
    private DatabaseReference mPostDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ProgressDialog mProgress;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1, CAMERA = 2;
    private String pictureFilePath;
    private TextView back;




    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //banner ad first
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mProgress = new ProgressDialog(this);




        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();

        mPostDatabase = FirebaseDatabase.getInstance().getReference().child("MBlog");

        mPostImage = (ImageButton) findViewById(R.id.imageButton);
        mPostTitle = (EditText) findViewById(R.id.postTitleEt);
        mPostDesc = (EditText) findViewById(R.id.descriptionEt);
        mSubmitButton = (Button) findViewById(R.id.submitPost);


        Toast.makeText(this, "Title Limit is Atmost 40 words", Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Description Limit is Atmost 400 words", Toast.LENGTH_LONG).show();

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }


        });


        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();

            }
        });

        //toolbar action
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        back = findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPostActivity.this, PostListActivity.class));
                finish();
            }
        });



    }





    public void selectImage()
    {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options,new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(options[which].equals("Take Photo"))
                {
                    if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        sendTakePictureIntent();
                    }
                }
                else if(options[which].equals("Choose from Gallery"))
                {

                    Intent intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GALLERY_CODE);
                }
                else if(options[which].equals("Cancel"))
                {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

      private void sendTakePictureIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA);

            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.zone.fileprovider",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA);
            }
        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "Zone_image_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mPostImage.setImageURI(mImageUri);

            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }


        if (requestCode == CAMERA && resultCode == RESULT_OK) {
            File imgFile = new  File(pictureFilePath);
            if(imgFile.exists())
            {
                mImageUri = Uri.fromFile(imgFile);
                mPostImage.setImageURI(Uri.fromFile(imgFile));

                CropImage.activity(mImageUri)
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mPostImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }



    private void startPosting() {

        mProgress.setMessage("Posting to blog...");
        mProgress.show();
        mProgress.setCanceledOnTouchOutside(false);

        final String titleVal = mPostTitle.getText().toString().trim();
        final String descVal = mPostDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(titleVal) && !TextUtils.isEmpty(descVal)
                && mImageUri != null) {

            final StorageReference filepath = mStorage.child("MBlog_images").
                    child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri downloadurl = uri.getResult();
                    DatabaseReference newPost = mPostDatabase.push();


                    String postid = mPostDatabase.push().getKey();

                    Map<String, String> dataToSave = new HashMap<>();
                    dataToSave.put("title", titleVal);
                    dataToSave.put("desc", descVal);
                    dataToSave.put("image", downloadurl.toString());
                    dataToSave.put("timestamp", String.valueOf(java.lang.System.currentTimeMillis()));
                    dataToSave.put("userid", mUser.getUid());
                    dataToSave.put("username", mUser.getEmail());
                    dataToSave.put("postid",postid);
                    newPost.setValue(dataToSave);

                    mProgress.dismiss();

                    startActivity(new Intent(AddPostActivity.this, PostListActivity.class));
                    finish();

                }
            });











        }
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }
}
