package com.mosisproject.mosisproject.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.model.Event;
import com.mosisproject.mosisproject.model.Friend;
import com.mosisproject.mosisproject.model.User;
import com.google.firebase.database.FirebaseDatabase;



public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextPhone;
    private TextView textViewLogin;

    private ProgressDialog progressDialog;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri selectedImage;

    private static int RESULT_LOAD_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextSurname = (EditText) findViewById(R.id.editTextSurname);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        textViewLogin = (TextView) findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressBar = new ProgressBar(this);

        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final Button buttonSelectPicture=(Button) findViewById(R.id.buttonAddPicture);

        //Choose profile image[ Gallery open ]
        buttonSelectPicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view == buttonRegister) {
            registerUser();
        }
        if(view == textViewLogin) {
            //Start Login activity
            startLoginActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null) {
            //handle the already login user
        }
    }

    public  void startLoginActivity() {
        //Start login activity after registration success
        firebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String surname = editTextSurname.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();

        if(!validateFields(name, surname, phone, email, password)) {
            return;
        }
        //if validations are ok
        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Store user

                            List<Event> events = new ArrayList<>();
                            events.add(new Event("init"));
                          
                            List<Friend> friends= new ArrayList<>();
                            friends.add(new Friend("init"));

                            //Store profile image
                            storeImage();
                            storeUser(name, surname, email, phone,0, friends, events);
                            //saveImage();
                            //Update firebase user profile
                            updateFirebaseUserProfile(name, surname);
                            progressDialog.hide();
                            startLoginActivity();
                        }
                        else {
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void updateFirebaseUserProfile(String name, String surname) {
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name + " " + surname)
                .setPhotoUri(Uri.parse(userId + ".jpg"))
                .build();

        firebaseUser.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Firebase user profile updated.");
                    }
                });
    }


    private void storeUser(String name, String surname, String email, String phone,int points, List<Friend > friends, List<Event> events) { //TODO: URADII da vraca true ili false
        String userId = firebaseAuth.getCurrentUser().getUid();
        User user = new User(userId, name, surname, email, phone, points, friends, events);
        database.getReference("Users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                clearRegistrationFields();
                progressDialog.hide();
                if(task.isSuccessful()) {
                    Log.d(TAG, "storeUser:success");
                    Toast.makeText(RegisterActivity.this, "Registration success", Toast.LENGTH_LONG).show();
                }
                else {
                    //Display failure message
                    Log.w(TAG, "storeUser:failure", task.getException());
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void storeImage() {
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();
        StorageReference sRef = storageReference.child("profile_images/" + userId + ".jpg");

        ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();

        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = sRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(TAG, "storeImage: failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                Log.w(TAG, "storeImage: success");
            }
        });
    }

    /*
    private void saveImage() {
        if(selectedImage != null) {
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "saveImage: success");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "saveImage: failed");
                        }
                    });
        }
    }
    */

    private boolean validateFields(String name,String surname,String phone,String email,String password) {

        if(TextUtils.isEmpty(name)) {
            //name empty
            editTextName.setError("Name required!");
            editTextName.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(surname)) {
            //surname empty
            editTextSurname.setError("Surname required!");
            editTextSurname.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(phone)) {
            //phone empty
            editTextPhone.setError("Phone required");
            editTextPhone.requestFocus();
            return false;
        }

        if(!Patterns.PHONE.matcher(phone).matches()) {
            editTextPhone.setError("Phone invalid!");
            editTextPhone.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(email)) {
            //email empty
            editTextEmail.setError("Email required!");
            editTextEmail.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Email invalid!");
            editTextEmail.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(password)) {
            //password empty
            editTextPassword.setError("Password required!");
            editTextPassword.requestFocus();
            return false;
        }

        if(password.length() < 6) {
            editTextPassword.setError("Password to short!");
            editTextPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void clearRegistrationFields() {
        editTextName.setText("");
        editTextSurname.setText("");
        editTextPhone.setText("");
        editTextEmail.setText("");
        editTextPassword.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
            imageView.setImageURI(selectedImage);

        }
*/
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            selectedImage = data.getData();
            try {
                ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}