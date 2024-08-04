package com.nouroeddinne.chatapp;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference databaseReferencere;
    DatabaseReference databaseReferencere2;
    DatabaseReference rf;
    FirebaseUser firebaseUser;
    FirebaseDatabase db;
    String name;
    RecyclerView recyclerView;
    List<String> listUsers  = new ArrayList<>();;
    RecyclerView.Adapter adapter;
    SharedPreferences sharedPreferences;

    List<String> listsort= new ArrayList<>();
    List<Long> listmsgs= new ArrayList<>();
    //ArrayList<Long> names = new ArrayList<Long>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences(Utels.SHAREDPREFERNCES_FILENAME_INFO, Context.MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        databaseReferencere2 = db.getReference();
        rf = db.getReference();

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
        }


        if (sharedPreferences.getString(Utels.SHAREDPREFERNCES_IMG,null)==null){
            databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(firebaseUser.getUid()).child(Utels.FIREBASE_TABLE_USERS_IMAGE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Utels.SHAREDPREFERNCES_IMG,String.valueOf(snapshot.getValue()));
                    editor.apply();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        Log.d("TAG", "name: "+sharedPreferences.getString(Utels.SHAREDPREFERNCES_NAME,null));
        Log.d("TAG", "img: "+sharedPreferences.getString(Utels.SHAREDPREFERNCES_IMG,null));



        Users();
        adapter = new UsersAdapter(listUsers,HomeActivity.this);
        recyclerView.setAdapter(adapter);



        //tss();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.item1){
            Intent intent = new Intent(HomeActivity.this,ProfileActivity.class);
            startActivity(intent);

        }

        if (item.getItemId()==R.id.item2){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            auth.signOut();
            Intent intent = new Intent(HomeActivity.this, loginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void Users(){
        databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                if (!key.equals(firebaseUser.getUid())){
                    listUsers.add(key);
                    adapter.notifyDataSetChanged();
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
    }


    public void tss(){

        Log.d("TAG",  " name " + name);


        rf.child(Utels.FIREBASE_TABLE_MESSAGES).child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Map to store the largest key for each group
                Map<String, Long> groupMaxKeyMap = new HashMap<>();

                // Process each group
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getKey();
                    Log.d("TAG", "1 groupName " + groupName);

                    Long maxTimestampStr = null;

                    // Iterate through each message in the group
                    for (DataSnapshot messageSnapshot : groupSnapshot.getChildren()) {
                        Long timestampStr = Long.valueOf(messageSnapshot.getKey());
                        Log.d("TAG", "2 timestampStr " + timestampStr);

                        // Validate if the key is a numeric string
                        if (isNumeric(String.valueOf(timestampStr))) {
                            if (maxTimestampStr == null || timestampStr > maxTimestampStr) {
                                maxTimestampStr = timestampStr;
                            }
                        }
                    }

                    Log.d("TAG", "3 maxTimestampStr " + maxTimestampStr);

                    if (maxTimestampStr != null) {
                        groupMaxKeyMap.put(groupName, maxTimestampStr);
                    }
                }

                List<Map.Entry<String, Long>> entryList = new ArrayList<>(groupMaxKeyMap.entrySet());

                // Sort the list by values in descending order
                entryList.sort((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()));

                // Rebuild the map with sorted entries
                Map<String, Long> sortedMap = new LinkedHashMap<>();
                for (Map.Entry<String, Long> entry : entryList) {
                    sortedMap.put(entry.getKey(), entry.getValue());
                }

                // Print the largest timestamp for each group
                Log.d("TAG", "Largest timestamp for each group: ");
                for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
                    Log.d("TAG", "Group: " + entry.getKey() + " - Largest Timestamp: " + entry.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TAG", "Error: " + databaseError.getMessage());

            }
        });

    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }












}