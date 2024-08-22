package com.nouroeddinne.chatapp;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class AdpterChat extends RecyclerView.Adapter<AdpterChat.ViewHolder>{

    List<Model> listUsers  = new ArrayList<>();
    Context context;

    DatabaseReference databaseReferencere;
    FirebaseDatabase db;
    FirebaseUser firebaseUser;

    String from,type;
    boolean status = false;
    int fromInt = 1;
    int toInt = 2;

    public AdpterChat(List<Model> listUsers, Context context,String from,String type) {
        this.listUsers = listUsers;
        this.context = context;
        this.from = from;
        this.type = type;

        db = FirebaseDatabase.getInstance();
        databaseReferencere = db.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viwe;

        if (viewType == fromInt){
            viwe = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sender, parent, false);
        }else{
            viwe = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_resever, parent, false);
        }

        return new ViewHolder(viwe);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.text.setText(listUsers.get(position).getMsg());
        if (type.equals("chat")){
            holder.name.setVisibility(View.GONE);
        }else {
            holder.name.setText(listUsers.get(position).getName());
        }
    }


    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView text,name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.textView2);
            name = itemView.findViewById(R.id.textView);
        }
    }

    @Override
    public int getItemViewType(int position) {

//        if (listUsers.get(position).from.equals(from)){
//            status = true;
//            return fromInt;
//        }else {
//            status = false;
//            return toInt;
//        }

        if (listUsers.get(position).from.equals(firebaseUser.getUid())){
            status = true;
            return fromInt;
        }else {
            status = false;
            return toInt;
        }


    }







































}
