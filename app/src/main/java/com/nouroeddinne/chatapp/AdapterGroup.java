package com.nouroeddinne.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.List;

public class AdapterGroup extends RecyclerView.Adapter<AdapterGroup.ViewHolder>{

    List<String> listGroup  = new ArrayList<>();
    long numberMember;
    Context context;

    DatabaseReference databaseReferencere;
    FirebaseDatabase db;
    FirebaseUser firebaseUser;

    String name;
    String img;
    String uid;

    public AdapterGroup(List<String> listGroup,Context context) {
        this.context = context;
        this.listGroup = listGroup;

        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }



    @NonNull
    @Override
    public AdapterGroup.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int currentPosition =holder.getAdapterPosition();

        databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).child(listGroup.get(currentPosition)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    name = String.valueOf(snapshot.child("Name").getValue());
                    img = String.valueOf(snapshot.child("img").getValue());
                    uid = String.valueOf(snapshot.child("uid").getValue());

                    if (name.equals("null")){
                        name = "Group";
                    }

                    if (img.equals("null")){
                        img="https://firebasestorage.googleapis.com/v0/b/chat-app-a2815.appspot.com/o/images%2Fgroup.png?alt=media&token=fe0edfa2-1410-4636-9d88-32c5a695d590";
                    }

                    Picasso.get().load(img).into(holder.img);
                    holder.textName.setText(name);

                    databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(listGroup.get(currentPosition)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            holder.numberMember.setText(String.valueOf(snapshot.getChildrenCount()));
                            holder.Join.setText("View");
                            holder.Join.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_arrow_forward_ios_24, 0);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    holder.linear_show.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context, ""+listGroup.get(currentPosition), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context,ChatActivity.class);
                            intent.putExtra("to", listGroup.get(currentPosition));
                            intent.putExtra("from", firebaseUser.getUid());
                            intent.putExtra("type", "group");
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

                                            databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(uid).child(firebaseUser.getUid()).removeValue();

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

                }else {
                    Log.d("Firebase", "field does not exist.");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return listGroup.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textName,member,numberMember,Join;
        ImageView img;
        LinearLayout linear_show;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            member = itemView.findViewById(R.id.textView_member);
            numberMember = itemView.findViewById(R.id.textView_number_member);
            textName = itemView.findViewById(R.id.textView_name);
            Join = itemView.findViewById(R.id.textView_join);
            img = itemView.findViewById(R.id.imageView);
            linear_show = itemView.findViewById(R.id.linear_show);
        }
    }
}
