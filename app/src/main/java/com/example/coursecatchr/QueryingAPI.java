package com.example.coursecatchr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Assumption: The program assumes that you are checking for courses by uploading transcript after completing
// current term. If taken 10 courses, next term will be 2A and they have completed 1B.

public class QueryingAPI extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ListView listView;
    //    ArrayList<String> userCourses = new ArrayList<>(Arrays.asList("MATH116", "ERS315", "GEOG208", "STV205", "ERS215" ,"ENVS200"));
    ArrayList<String> userCourses;
    int termNumber;
    int termAlf;
    String termAlphabet = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_querying_a_p_i);
        userCourses = (ArrayList<String>) getIntent().getStringArrayListExtra("Courses");
        termNumber = userCourses.size()/10 + 1;
        termAlf = userCourses.size() % 10;

        if (termAlf < 5) {
            termAlphabet = "A";
        }else {
            termAlphabet = "B";
        }

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setTitle("Eligible Courses");

        final String currentTerm = termNumber + termAlphabet;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = (ListView)findViewById(R.id.simpleListView);

        final ArrayList<String> listA = new ArrayList<>();
        final ArrayList<String> listB = new ArrayList<>();
        final ArrayList<String> listC = new ArrayList<>();


        final ArrayList<String> listMain = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listMain);
        listView.setAdapter(adapter);


        final ArrayList<String> title = new ArrayList<>();

        mDatabase.child("Courses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    String stringCourses = dataSnapshot.child("prereq").getValue().toString();
                    String[] splitStrings = stringCourses.split(",");
                    boolean isPresent = true;

                    if (splitStrings[0] == ""){

                    }else {
                        for (int dbCourse = 0; dbCourse < splitStrings.length && isPresent; dbCourse++){
                            if (userCourses.contains(splitStrings[dbCourse])){
                            }else {
                                isPresent = false;
                            }
                        }
                    }

                    if (userCourses.contains(dataSnapshot.getKey().toString())){
                        isPresent = false;
                    }

                    String courseTerm = dataSnapshot.child("min_term").getValue().toString();
                    if (courseTerm.compareTo(currentTerm) <= 0){
                        isPresent = true;
                    }else {
                        isPresent = false;
                    }

                    if (isPresent){

                        if (dataSnapshot.child("list").getValue().toString().equals("A")){
                            listA.add(dataSnapshot.getKey().toString());

                        }else if (dataSnapshot.child("list").getValue().toString().equals("B") ){
                            listB.add(dataSnapshot.getKey().toString());
                        }else{
                            listC.add(dataSnapshot.getKey().toString());
                        }

//                        title.add(dataSnapshot.child("descr").getValue().toString());
                    }

                }

                listMain.add("                                            LIST A");
                for (int countA = 0; countA < listA.size(); countA++){
                    listMain.add(listA.get(countA));
                }
                listMain.add("                                            LIST B");
                for (int countB = 0; countB < listB.size(); countB++){
                    listMain.add(listB.get(countB));
                }
                listMain.add("                                            LIST C");
                for (int countC = 0; countC < listC.size(); countC++){
                    listMain.add(listC.get(countC));
                }
                adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Toast.makeText(QueryingAPI.this, title.get(position), Toast.LENGTH_SHORT).show();
            }
        });






    }
}
