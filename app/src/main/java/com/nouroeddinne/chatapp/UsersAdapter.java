package com.nouroeddinne.chatapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    List<String> listUsers  = new ArrayList<>();
    Context context;

    DatabaseReference databaseReferencere;
    FirebaseDatabase db;
    FirebaseUser firebaseUser;

    String name;
    String img;
    String from;
    String uid;
    boolean connected;

    SharedPreferences sharedPreferences;

    Map<String, String> groupMaxKeyMap = new HashMap<>();

    public UsersAdapter(List<String> listUsers, Context context) {
        this.listUsers = listUsers;
        this.context = context;

        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viwe = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(viwe);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

//        Log.d("Firebase", " "+listUsers.size());
        uid="";
        databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(listUsers.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

//                Log.d("Firebase", "uid is ; "+String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue()));

                if (snapshot.exists()){

                    name = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_NAME).getValue());
                    img = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_IMAGE).getValue());
                    uid = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue());
                    connected = (Boolean) snapshot.child("online").getValue();

                    if (name.equals("null")){
                        name = "User";
                    }
                    holder.textName.setText(name);

                    if (img.equals("null")){
                        img="https://firebasestorage.googleapis.com/v0/b/chat-app-a2815.appspot.com/o/images%2Fprofile-user.png?alt=media&token=dd283d5e-ecb1-43a4-946f-cab51815813d";
                    }
                    Picasso.get().load(img).into(holder.img);

                    if (connected){
                        holder.imgStatus.setImageResource(R.drawable.online);
                    }else {
                        holder.imgStatus.setImageResource(R.drawable.offline);
                    }

                    holder.add.setVisibility(View.GONE);

                }else {
                    Log.d("Firebase", "field does not exist.");
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sharedPreferences = context.getSharedPreferences(Utels.SHAREDPREFERNCES_FILENAME_INFO, Context.MODE_PRIVATE);
        from = sharedPreferences.getString(Utels.SHAREDPREFERNCES_NAME,null);

        holder.linear_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, ""+listUsers.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("to", listUsers.get(position));
                intent.putExtra("from", firebaseUser.getUid());
                intent.putExtra("type", "chat");
                context.startActivity(intent);
            }
        });

        holder.linear_show.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Do you want to delete "+name+"?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle OK button click

                                databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_FRINDE).child(firebaseUser.getUid()).child(uid).removeValue();
                                databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_FRINDE).child(uid).child(firebaseUser.getUid()).removeValue();

                                databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(firebaseUser.getUid()).child(uid).removeValue();
                                databaseReferencere.child(Utels.FIREBASE_TABLE_MESSAGES).child(uid).child(firebaseUser.getUid()).removeValue();

                                notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle Cancel button click
                            }
                        })
                        .show();

                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textName,add;
        ImageView img,imgStatus;
        LinearLayout linear_show;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textView_name);
            add = itemView.findViewById(R.id.textView_add);
            img = itemView.findViewById(R.id.imageView);
            imgStatus = itemView.findViewById(R.id.imageView_status);
            linear_show = itemView.findViewById(R.id.linear_show);
        }
    }
















}
