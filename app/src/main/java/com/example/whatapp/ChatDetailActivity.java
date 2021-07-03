package com.example.whatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.whatapp.Adapters.ChatAdapter;
import com.example.whatapp.Models.MessagesModel;
import com.example.whatapp.databinding.ActivityChatDetailActivtyBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailActivtyBinding binding;
    FirebaseAuth  auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailActivtyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
      auth = FirebaseAuth.getInstance();
      database = FirebaseDatabase.getInstance();
      getSupportActionBar().hide();

     final String senderId = auth.getUid();
      String reciveId = getIntent().getStringExtra("userId");
      String userName = getIntent().getStringExtra("userName");
      String profilePic = getIntent().getStringExtra("profilePic");


      binding.userName1.setText(userName);
      Picasso.get().load(profilePic).placeholder(R.drawable.ic_avatar).into(binding.profileImage);

      binding.backArrow.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent intent = new Intent(ChatDetailActivity.this,MainActivity.class);
              startActivity(intent);
          }
      });
    final ArrayList<MessagesModel> messagesModels = new ArrayList<>();
    final ChatAdapter chatAdapter = new ChatAdapter(messagesModels,this);
    binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        final String senderRoom = senderId + reciveId;
        final String receiverRoom = reciveId + senderId;

        database.getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesModels.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren())
                {
                    MessagesModel model = snapshot1.getValue(MessagesModel.class);

                    model.setMessageId(snapshot1.getKey());

                    messagesModels.add(model);
                }

                chatAdapter.notifyDataSetChanged();
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.etMessage.getText().toString();
                final MessagesModel model= new MessagesModel(senderId,message);
                model.setTimestamp(new Date().getTime());
                binding.etMessage.setText("");
                database.getReference().child("chats").child(senderRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats").child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.setting:
                Toast.makeText(ChatDetailActivity.this,"This is Setting",Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                auth.signOut();
                Intent intent = new Intent(ChatDetailActivity.this,SignInActivity.class);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

}