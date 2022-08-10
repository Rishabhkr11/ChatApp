package rishabh.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText UserName, UserStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;

    private Toolbar SettingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Update Display Picture");
                builder.setMessage("Are you sure you want to update your profile picture?");
                builder.setCancelable(true);
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(SettingsActivity.this);
                    }
                });
                builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RootRef.child("Users").child(currentUserID).child("image").removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(SettingsActivity.this, "Display picture removed successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
                        finish();
                        startActivity(getIntent());
                    }
                });

                AlertDialog dialog = null;

                try {
                    dialog = builder.create();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (dialog != null)
                    dialog.show();
            }
        });
    }

    private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        UserName = (EditText) findViewById(R.id.set_user_name);
        UserStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Display Picture");
                loadingBar.setMessage("Please wait, your display picture is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                userProfileImage.setImageURI(resultUri);

                final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                UploadTask uploadTask = filePath.putFile(resultUri);

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    //  Uri downloadUri = task.getResult();
                                    // Getting image upload ID.
                                    final String downloadURL = task.getResult().toString();
                                    String ImageUploadId = RootRef.push().getKey();

                                    // Adding image upload id s child element into databaseReference.

                                    RootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadURL);
                                    Toast.makeText(SettingsActivity.this, "Image Successfully Updated", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    // Handle failures
                                    // ...
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Error While Uploaded", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        }
    }

    private void UpdateSettings() {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            //Toast.makeText(this, "Please write your UserName...", Toast.LENGTH_SHORT).show();
            UserName.setError("Required");
            UserName.requestFocus();
        } else if (TextUtils.isEmpty(setStatus)) {
            //Toast.makeText(this, "Please write your status...", Toast.LENGTH_SHORT).show();
            UserStatus.setError("Required");
            UserStatus.requestFocus();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("status", setStatus);
            profileMap.put("name", setUserName);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                TastyToast.makeText(getApplicationContext(), "Profile Updated Successfully...", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                            } else {
                                String message = task.getException().toString();
                                TastyToast.makeText(getApplicationContext(), "Error: " + message, TastyToast.LENGTH_LONG, TastyToast.WARNING);
                            }
                        }
                    });
        }
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);
                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        } else {
                            TastyToast.makeText(getApplicationContext(), "Please update your Profile Info...", TastyToast.LENGTH_SHORT, TastyToast.INFO);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}