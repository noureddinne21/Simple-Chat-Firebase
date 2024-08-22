package com.nouroeddinne.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
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

public class AdapterSearch extends RecyclerView.Adapter<AdapterSearch.ViewHolder> {

    List<String> listSearchUsers  = new ArrayList<>();
    List<String> listSearchGroups = new ArrayList<>();
    Context context;

    DatabaseReference databaseReferencere;
    FirebaseDatabase db;
    FirebaseUser firebaseUser;

    int VIEW_TYPE_ONE = 1;
    int VIEW_TYPE_TWO = 2;

    String name;
    String img;
    String uid;

    public AdapterSearch(List<String> listSearchGroups , List<String> listSearchUsers , Context context) {

        this.listSearchUsers = listSearchUsers;
        this.listSearchGroups = listSearchGroups;
        this.context = context;

        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }


    @Override
    public int getItemViewType(int position) {

        if (position < listSearchGroups.size()) {
            return VIEW_TYPE_ONE;
        } else {
            return VIEW_TYPE_TWO;
        }

    }

    @NonNull
    @Override
    public AdapterSearch.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ONE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSearch.ViewHolder holder, int position) {

        int currentPosition =holder.getAdapterPosition();

        if (getItemViewType(position) == VIEW_TYPE_ONE) {
            String groupId = listSearchGroups.get(position);

            databaseReferencere.child(Utels.FIREBASE_TABLE_GROUP).child(groupId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){

                        databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){

                                    if (dataSnapshot.child(firebaseUser.getUid()).getValue() != null){
                                        holder.Join.setText("View");
                                        holder.Join.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_arrow_forward_ios_24, 0);
                                    }else {
                                        holder.Join.setText("Join");
                                        holder.Join.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_group_add_24, 0);
                                    }



                                }else {
                                    Log.d("Firebase",  "Groups field does not exist.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Firebase", "loadMessages:onCancelled", databaseError.toException());
                            }

                        });

                        name = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_NAME).getValue());
                        img = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_IMAGE).getValue());
                        uid = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue());

                        if (name.equals("null")){
                            name = "Group";
                        }
                        holder.textName.setText(name);


                        if (img.equals("null")){
                            img="https://firebasestorage.googleapis.com/v0/b/chat-app-a2815.appspot.com/o/images%2Fgroup.png?alt=media&token=fe0edfa2-1410-4636-9d88-32c5a695d590";
                        }
                        Picasso.get().load(img).into(holder.img);

                        databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(listSearchGroups.get(currentPosition)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.numberMember.setText(String.valueOf(snapshot.getChildrenCount()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

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

            holder.Join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(groupId).child(firebaseUser.getUid()).setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                holder.Join.setText("View");
                                holder.Join.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_arrow_forward_ios_24, 0);
                                notifyDataSetChanged();
                            }
                        }
                    });
                }
            });

        } else {

            String userId = listSearchUsers.get(position - listSearchGroups.size());
            databaseReferencere.child(Utels.FIREBASE_TABLE_USERS).child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){

                        databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_FRINDE).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){

                                    if (dataSnapshot.child(firebaseUser.getUid()).getValue() != null){
                                        holder.status.setImageResource(R.drawable.offline);
                                        holder.add.setVisibility(View.GONE);
                                    }else {
                                        holder.status.setVisibility(View.GONE);
                                        holder.add.setVisibility(View.VISIBLE);
                                    }

                                }else {
                                    holder.status.setVisibility(View.GONE);
                                    holder.add.setVisibility(View.VISIBLE);
                                }

                                name = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_NAME).getValue());
                                img = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_IMAGE).getValue());
                                uid = String.valueOf(snapshot.child(Utels.FIREBASE_TABLE_USERS_UID).getValue());

                                if (name.equals("null")){
                                    name = "User";
                                }
                                holder.textName.setText(name);

                                if (img.equals("null")){
                                    img="https://firebasestorage.googleapis.com/v0/b/chat-app-a2815.appspot.com/o/images%2Fprofile-user.png?alt=media&token=dd283d5e-ecb1-43a4-946f-cab51815813d";
                                }
                                Picasso.get().load(img).into(holder.img);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Firebase", "loadMessages:onCancelled", databaseError.toException());
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

            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_FRINDE).child(firebaseUser.getUid()).child(userId).setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                databaseReferencere.child(Utels.FIREBASE_TABLE_MEMBER_GROUP).child(userId).child(firebaseUser.getUid()).setValue("true");
                                holder.status.setImageResource(R.drawable.offline);
                                holder.status.setVisibility(View.VISIBLE);
                                holder.add.setVisibility(View.GONE);
                                notifyDataSetChanged();
                            }
                        }
                    });
                }
            });
        }



    }

    @Override
    public int getItemCount() {
        return listSearchGroups.size()+listSearchUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textName,member,numberMember,add,Join;
        ImageView img,status;
        LinearLayout linear_show;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            member = itemView.findViewById(R.id.textView_member);
            numberMember = itemView.findViewById(R.id.textView_number_member);
            textName = itemView.findViewById(R.id.textView_name);
            Join = itemView.findViewById(R.id.textView_join);
            add = itemView.findViewById(R.id.textView_add);
            img = itemView.findViewById(R.id.imageView);
            status = itemView.findViewById(R.id.imageView_status);
            linear_show = itemView.findViewById(R.id.linear_show);
        }
    }

























}
