package com.nouroeddinne.chatapp;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    ImageView imageView;
    EditText editText;
    Button button;
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
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView_profile);
        editText = findViewById(R.id.editTextText_name_profile);
        button = findViewById(R.id.button_signin_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        fs = FirebaseStorage.getInstance();
        storageReference = fs.getReference();

        sharedPreferences = getSharedPreferences(Utels.SHAREDPREFERNCES_FILENAME_INFO, Context.MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();
        if (extras!=null && extras.getString("addGroup")!=null){
            button.setText("Add New Group");
        }else {
            getInfo();
            button.setText("Save Changes");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString().trim();

                if (extras!=null && extras.getString("addGroup")!=null){
                    addGroup(name);
                }else {
                    edit(name);
                }



            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseImg();
            }
        });
    }


    public void choseImg(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launchResult.launch(intent);
    }


    public void getInfo(){

        databaseReferencere.child("Users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = String.valueOf(snapshot.child("Name").getValue());
                String img = String.valueOf(snapshot.child("img").getValue());

                editText.setText(name);
                if (!img.equals("null")){
                    Picasso.get().load(img).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void edit(String name){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String[] urlImg = new String[1];
        databaseReferencere.child("Users").child(auth.getUid()).child("Name").setValue(name);
        editor.putString(Utels.SHAREDPREFERNCES_NAME,name);

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
                            databaseReferencere.child("Users").child(auth.getUid()).child("img").setValue(urlImg[0]);
                            editor.putString(Utels.SHAREDPREFERNCES_IMG, urlImg[0]);

                        }
                    });
                }
            });
        }
        editor.apply();

    }


    public void addGroup(String name){

        String key = databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).push().getKey();
        final String[] urlImg = new String[1];
        databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).child(key).child("Name").setValue(name);
        databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).child(key).child("uid").setValue(key);
        databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(key).child(auth.getUid()).setValue("true");

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
                            databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).child(key).child("img").setValue(urlImg[0]);
                        }
                    });
                }
            });
        }

    }




















}