package com.polar.polarsdkecghrdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsListActivity extends AppCompatActivity
        implements MyAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    List<String> namesList = MainActivity.base.toStringList();
    public void onReturn(View view) {
        finish();
    }

    public void onAddFriend(View view) {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerUpdate();
    }

    public void recyclerUpdate() {
        namesList = MainActivity.base.toStringList();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter<MyAdapter.ViewHolder> adapter = new MyAdapter(namesList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(String name) {
        Intent intent = new Intent(FriendsListActivity.this, AddFriendActivity.class);
        intent.putExtra("ID", name);
        startActivity(intent);
        recyclerUpdate();
    }
}