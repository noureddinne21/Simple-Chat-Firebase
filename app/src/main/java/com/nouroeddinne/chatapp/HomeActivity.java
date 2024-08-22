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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.SearchView;
import android.widget.TextView;
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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference databaseReferencere;
    //DatabaseReference databaseReferencere2;
    //DatabaseReference rf;
    FirebaseUser firebaseUser;
    FirebaseDatabase db;
    String name;
    RecyclerView recyclerView;
    List<String> listUsers  = new ArrayList<>();;
    List<String> listGroups  = new ArrayList<>();;
    List<String> listname = new ArrayList<>();;
    List<Long> listMsg  = new ArrayList<>();;
    RecyclerView.Adapter adapterSearch;
    SharedPreferences sharedPreferences;

//    List<String> listsort= new ArrayList<>();
//    List<Long> listmsgs= new ArrayList<>();
    //ArrayList<Long> names = new ArrayList<Long>();

    TabLayout tabLayout;
    ViewPager2 viewPager;
    PagerAdapter adapter;

    //SearchView search_bar;

    TextView textView;

    DatabaseReference userStatusRef;


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

//        search = findViewById(R.id.button_search);
//        editText = findViewById(R.id.editTextText);

        textView = findViewById(R.id.textView_result);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viwepager);
        recyclerView = findViewById(R.id.recyclerView_search);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences(Utels.SHAREDPREFERNCES_FILENAME_INFO, Context.MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        //databaseReferencere2 = db.getReference();
        //rf = db.getReference();

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

//        Log.d("TAG", "name: "+sharedPreferences.getString(Utels.SHAREDPREFERNCES_NAME,null));
//        Log.d("TAG", "img: "+sharedPreferences.getString(Utels.SHAREDPREFERNCES_IMG,null));


        userStatusRef = FirebaseDatabase.getInstance().getReference(Utels.FIREBASE_TABLE_USERS).child(auth.getUid()).child("online");
        DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        presenceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    // When connected, set status to true
                    userStatusRef.setValue(true);

                    // Set up a method to update status to false when the user disconnects
                    userStatusRef.onDisconnect().setValue(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        orderBy();








//        search.setOnClickListener(view -> {
//
//            Users(editText.getText().toString());
//
//        });







    }


    @Override
    protected void onStop() {
        super.onStop();
        userStatusRef.onDisconnect().setValue(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    search(query);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true; // Allow the action to expand
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                orderBy();
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                return true; // Allow the action to collapse
            }
        });

        return true;

        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
////
//        if (item.getItemId()==R.id.search_bar){
////            Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
////            //search
////
//////            search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//////                @Override
//////                public boolean onQueryTextSubmit(String query) {
//////
//////
//////
//////                    return false;
//////                }
//////
//////                @Override
//////                public boolean onQueryTextChange(String newText) {
//////                    //    adapter.getFilter().filter(newText);
//////                    return false;
//////                }
//////            });
////
//        }

        if (item.getItemId()==R.id.addGroup){
            //add group
            Intent intent = new Intent(HomeActivity.this,ProfileActivity.class);
            intent.putExtra("addGroup","addGroup");
            startActivity(intent);

        }

        if (item.getItemId()==R.id.item1){
            Intent intent = new Intent(HomeActivity.this,ProfileActivity.class);
            startActivity(intent);

        }

        if (item.getItemId()==R.id.item2){
            userStatusRef.onDisconnect().setValue(false);
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





    @SuppressLint("NotifyDataSetChanged")
    public void orderBy(){

        databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listUsers.clear();
                listGroups.clear();
                listname.clear();
                listMsg.clear();

                if (dataSnapshot.exists()){

                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                        String groupName = groupSnapshot.getKey();
                        //Log.d("Firebase", "1 groupName " + groupName);
                        listname.add(groupName);

                        Long maxTimestampStr = null;

                        for (DataSnapshot messageSnapshot : groupSnapshot.getChildren()) {
                            Long timestampStr = Long.valueOf(messageSnapshot.getKey());
                            //Log.d("Firebase", "2 timestampStr " + timestampStr);

                            if (isNumeric(String.valueOf(timestampStr))) {
                                if (maxTimestampStr == null || timestampStr > maxTimestampStr) {
                                    maxTimestampStr = timestampStr;
                                }
                            }
                        }
                        //Log.d("Firebase", "3 maxTimestampStr " + maxTimestampStr);
                        listMsg.add(maxTimestampStr);
                    }

                    Map<Long, String> orderMap = new HashMap<>();
                    for (int i = 0; i < listMsg.size(); i++) {
                        orderMap.put(listMsg.get(i),listname.get(i));
                    }

                    Collections.sort(listMsg,Collections.reverseOrder());

                    for (Long key : listMsg) {
                        String value = orderMap.get(key);
                        databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(value).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    listUsers.add(value);
                                    if (adapter!=null){
                                        adapter.notifyDataSetChanged();
                                    }

                                }else {
                                    Log.d("Firebase", "field does not exist.");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        Log.d("Firebase uid ; ", "value ; "+value);

                    }

                    Log.d("Firebase", " "+listUsers.size());



                    }else {
                    Log.d("Firebase", "Timestamp field does not exist.");
                }

                databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_FRINDE).child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot s : snapshot.getChildren()) {
                            if (s.exists()){
                                if (!listUsers.contains(s.getKey())){
                                    listUsers.add(s.getKey());
                                    if (adapter!=null){
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }else {
                                Log.d("Firebase", "frend member field does not exist.");
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        listGroups.clear();
                        listname.clear();
                        listMsg.clear();

                        if (dataSnapshot.exists()){

                            for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                                String groupName = groupSnapshot.getKey();
                                //Log.d("Firebase", "1 groupName " + groupName);
                                listname.add(groupName);

                                Long maxTimestampStr = null;

                                for (DataSnapshot messageSnapshot : groupSnapshot.getChildren()) {
                                    Long timestampStr = Long.valueOf(messageSnapshot.getKey());

                                    if (isNumeric(String.valueOf(timestampStr))) {
                                        if (maxTimestampStr == null || timestampStr > maxTimestampStr) {
                                            maxTimestampStr = timestampStr;
                                        }
                                    }
                                }
                                listMsg.add(maxTimestampStr);
                            }

                            Map<Long, String> orderMap = new HashMap<>();
                            for (int i = 0; i < listMsg.size(); i++) {
                                orderMap.put(listMsg.get(i),listname.get(i));
                            }

                            Collections.sort(listMsg,Collections.reverseOrder());

                            for (Long key : listMsg) {
                                String value = orderMap.get(key);
                                if (!listGroups.contains(key)){
                                    databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).child(value).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                listGroups.add(value);
                                                if (adapter!=null){
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }else {
                                                Log.d("Firebase", "field does not exist.");
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }

                            Log.d("Firebase", " "+listGroups.size());

                        }else {
                            Log.d("Firebase", "Timestamp field does not exist.");
                        }

                        databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {

                                        if (groupSnapshot.child(auth.getUid()).getValue() != null){
                                            String groupName = groupSnapshot.getKey();
                                            if (!listGroups.contains(groupName)){
                                                listGroups.add(groupName);
                                                if (adapter!=null){
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }
                                }else {
                                    Log.d("Firebase",  "Groups field does not exist.");
                                }

                                Log.d("Firebase",  "size "+(listGroups.size()+listUsers.size()));

                                if (listGroups.size()+listUsers.size() == 0){
                                    textView.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }else{
                                    textView.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }

                                adapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
                                adapter.addTab(new ModelPager("Chat", BlankFragment.newInstance("Chat", listUsers)));
                                adapter.addTab(new ModelPager("Group", BlankFragment.newInstance("Group", listGroups)));
                                viewPager.setAdapter(adapter);

                                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                                    tab.setText(adapter.getTabName(position));
                                }).attach();

                                adapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Firebase", "loadMessages:onCancelled", databaseError.toException());
                            }

                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle potential errors
                        Log.d("Firebase", "loadMessages:onCancelled", databaseError.toException());
                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
                Log.d("Firebase", "loadMessages:onCancelled", databaseError.toException());
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



    public void search(String text) {

        listUsers.clear();
        listGroups.clear();
        // Ensure you are ordering by a valid field, like "name"
        Query query = databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).orderByChild(Utels.FIREBASE_TABLE_USERS_NAME).startAt(text).endAt(text + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!listGroups.contains(String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue()))){
                            listGroups.add(String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue()));
                            if(adapterSearch!=null){
                                adapterSearch.notifyDataSetChanged();
                            }
                        }
                    }
                } else {
                    Log.d("Firebase", "No matching records found.");
                }

                Query query = databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).orderByChild(Utels.FIREBASE_TABLE_USERS_NAME).startAt(text).endAt(text + "\uf8ff");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (!listUsers.contains(String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue()))){
                                    if(!snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue().equals(auth.getUid())){
                                        listUsers.add(String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue()));
                                        if(adapterSearch!=null){
                                            adapterSearch.notifyDataSetChanged();
                                        }
                                    }

                                }
                            }
                        } else {
                            Log.d("Firebase", "No matching records found.");
                        }

                        if (listUsers.size()+listGroups.size() == 0){
                            recyclerView.setVisibility(View.GONE);
                            textView.setVisibility(View.VISIBLE);
                        }else {
                            recyclerView.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.GONE);

                        }

                        adapterSearch = new AdapterSearch(listGroups,listUsers,getApplication());
                        recyclerView.setAdapter(adapterSearch);
                        adapterSearch.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "Search failed: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Search failed: " + databaseError.getMessage());
            }
        });

    }








































}