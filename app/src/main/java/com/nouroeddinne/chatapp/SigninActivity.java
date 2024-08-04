package com.nouroeddinne.chatapp;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class SigninActivity extends AppCompatActivity {

    ImageView imageView;
    EditText name,email,password;
    Button signin,login;
    boolean img = false;
    Uri imageUri;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference databaseReferencere;

    FirebaseStorage fs;
    StorageReference storageReference;

    FirebaseUser firebaseUser;

    SharedPreferences sharedPreferences;

    ActivityResultLauncher<Intent> launchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {

                    if (o!=null&&o.getResultCode()==RESULT_OK){
                        imageUri = o.getData().getData();
                        Picasso.get().load(imageUri).into(imageView);
                        img = true;
                    }else {
                        img = false;
                    }

                }
            });


    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            Intent intent = new Intent(SigninActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView_profile);
        name = findViewById(R.id.editTextText_name_profile);
        email = findViewById(R.id.editTextText_email_signin);
        password = findViewById(R.id.editTextText6_password_signin);
        signin = findViewById(R.id.button_signin_profile);
        login = findViewById(R.id.button5_login_signin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();

        fs = FirebaseStorage.getInstance();
        storageReference = fs.getReference();

        sharedPreferences = getSharedPreferences(Utels.SHAREDPREFERNCES_FILENAME_INFO, Context.MODE_PRIVATE);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseImg();
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = name.getText().toString().trim();
                String e = email.getText().toString().trim();
                String p = password.getText().toString().trim();
                create(n,e,p);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, loginActivity.class);
                startActivity(intent);
            }
        });




    }


    public void choseImg(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launchResult.launch(intent);
    }


    public void create(String name,String email,String password){
        final String[] urlImg = new String[1];
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(auth.getUid()).child(Utels.FIREBASE_TABLE_USERS_NAME).setValue(name);
                    databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(auth.getUid()).child(Utels.FIREBASE_TABLE_USERS_EMAIL).setValue(email);
                    databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(auth.getUid()).child(Utels.FIREBASE_TABLE_USERS_PASSWORD).setValue(password);
                    if (img){
                        UUID randomID = UUID.randomUUID();
                        String image = "images/"+randomID+".jpg";
                        storageReference.child(image).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                StorageReference mystorageReference = fs.getReference(image);
                                mystorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        urlImg[0] = uri.toString();
                                        databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(auth.getUid()).child(Utels.FIREBASE_TABLE_USERS_IMAGE).setValue(urlImg[0]);
                                    }
                                });
                            }
                        });
                    }else {
                        databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(auth.getUid()).child(Utels.FIREBASE_TABLE_USERS_IMAGE).setValue("null");
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Utels.SHAREDPREFERNCES_NAME,name);
                    editor.putString(Utels.SHAREDPREFERNCES_IMG, urlImg[0]);
                    editor.apply();

                    Intent intent = new Intent(SigninActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(SigninActivity.this, "Error creating user", LENGTH_SHORT).show();
                }
            }
        });

    }




















}