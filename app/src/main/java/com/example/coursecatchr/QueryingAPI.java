package com.example.coursecatchr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class QueryingAPI extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ListView listView;
    String[] userCourses = {"MATH116", "ERS 315", "GEOG208", "STV205"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_querying_a_p_i);

        ArrayList<String> courseList = (ArrayList<String>) getIntent().getStringArrayListExtra("Courses");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = (ListView)findViewById(R.id.simpleListView);
        final ArrayList<String> list = new ArrayList<>();
        final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        mDatabase.child("Courses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
// dont suggest if course already taken
// dataSnapshot.child("min_term").getValue().toString() == term
                    String stringCourses = dataSnapshot.child("prereq").getValue().toString();
                    String[] splitStrings = stringCourses.split(",");

                  //  if (dataSnapshot.child("prereq").getValue().toString() == userCourses[count]) {
                 //       list.add(dataSnapshot.getKey().toString());
                  //  }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}