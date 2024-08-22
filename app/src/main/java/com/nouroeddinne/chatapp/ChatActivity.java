package com.nouroeddinne.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editText;
    Button button;

    DatabaseReference databaseReferencere;
    FirebaseDatabase db;
    FirebaseUser firebaseUser;

    String to="",from="",type="";

    RecyclerView.Adapter adapter;
    List<Model> listMsgs  = new ArrayList<>();

    SharedPreferences sharedPreferences;
    String name;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.messageEditText);
        button = findViewById(R.id.sendButton);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Bundle extras = getIntent().getExtras();
        if (extras!=null){
            to = extras.getString("to");
            from = extras.getString("from");
            type = extras.getString("type");
            Log.d("TAG", "onCreate: "+from+" "+to+" "+type);
        }

        sharedPreferences = getSharedPreferences(Utels.SHAREDPREFERNCES_FILENAME_INFO, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(Utels.SHAREDPREFERNCES_NAME,null)==null){
            Log.d("TAG", "onCreate: null name");
            databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(firebaseUser.getUid()).child(Utels.FIREBASE_TABLE_USERS_NAME).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    name= String.valueOf(snapshot.getValue());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Utels.SHAREDPREFERNCES_NAME,name);
                    editor.apply();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }else {
            name = sharedPreferences.getString(Utels.SHAREDPREFERNCES_NAME,null);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(editText.getText().toString().trim());
            }
        });


        Msgs();



    }

    public void sendMsg(String msg){

        long currentTimeMillis = Instant.now().toEpochMilli();

        if (type.equals("group")){

            String key = databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(from).child(to).child(String.valueOf(currentTimeMillis)).getKey();
            Map<String,Object> data = new HashMap<>();
            data.put(Utels.FIREBASE_TABLE_MESSAGES_MSG,msg);
            data.put(Utels.FIREBASE_TABLE_MESSAGES_FROM,from);
            data.put(Utels.FIREBASE_TABLE_USERS_NAME,name);
            databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(from).child(to).child(key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(to).child(key).setValue(data);
                        editText.setText("");
                    }
                }
            });

        }else {

            String key = databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(from).child(to).child(String.valueOf(currentTimeMillis)).getKey();
            Map<String,Object> data = new HashMap<>();
            data.put(Utels.FIREBASE_TABLE_MESSAGES_MSG,msg);
            data.put(Utels.FIREBASE_TABLE_MESSAGES_FROM,from);
            data.put(Utels.FIREBASE_TABLE_USERS_NAME,name);
            databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(from).child(to).child(key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(to).child(from).child(key).setValue(data);
                        editText.setText("");
                    }
                }
            });

        }



    }



    public void Msgs(){

        if (type.equals("group")){

            databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(to).addChildEventListener(new ChildEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    Model m = snapshot.getValue(Model.class);
                    if (m != null) {
                        Toast.makeText(ChatActivity.this, ""+m.getMsg(), Toast.LENGTH_SHORT).show();
                        listMsgs.add(m);
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(listMsgs.size() - 1);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            adapter = new AdpterChat(listMsgs,ChatActivity.this,to,type);
            recyclerView.setAdapter(adapter);

        }else {

            databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(from).child(to).addChildEventListener(new ChildEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    Model m = snapshot.getValue(Model.class);
                    if (m != null) {
                        Toast.makeText(ChatActivity.this, ""+m.getMsg(), Toast.LENGTH_SHORT).show();
                        listMsgs.add(m);
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(listMsgs.size() - 1);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            adapter = new AdpterChat(listMsgs,ChatActivity.this,from,type);
            recyclerView.setAdapter(adapter);

        }





    }






























}