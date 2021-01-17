package com.example.coursecatchr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

public class QueryingAPI extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mDatabase;
    private ListView listView, listView2;
    ArrayList<String> userCourses;
    ArrayList<Boolean> favourites = new ArrayList<>();
    int termNumber;
    int termAlf;
    String termAlphabet = "";
    final ArrayList<String> list = new ArrayList<>();
    ArrayList<String> fav = new ArrayList<>();
    final ArrayList<String> listMain = new ArrayList<>();

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

        findViewById(R.id.like).setOnClickListener(this);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setTitle("Eligible Courses");

        final String currentTerm = termNumber + termAlphabet;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = (ListView)findViewById(R.id.simpleListView);

        final ArrayList<String> listA = new ArrayList<>();
        final ArrayList<String> listB = new ArrayList<>();
        final ArrayList<String> listC = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listMain);
        listView.setAdapter(adapter);

        final ArrayList<String> descriptionA = new ArrayList<>();
        final ArrayList<String> descriptionB = new ArrayList<>();
        final ArrayList<String> descriptionC = new ArrayList<>();

        final ArrayList<String> description = new ArrayList<>();
        final Context context = this;

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
                            listA.add(dataSnapshot.getKey().toString() + " - " + dataSnapshot.child("descr").getValue().toString());
                            descriptionA.add(dataSnapshot.child("details").getValue().toString());

                        }else if (dataSnapshot.child("list").getValue().toString().equals("B") ){
                            listB.add(dataSnapshot.getKey().toString() + " - " + dataSnapshot.child("descr").getValue().toString());
                            descriptionB.add(dataSnapshot.child("details").getValue().toString());
                        }else{
                            listC.add(dataSnapshot.getKey().toString() + " - " + dataSnapshot.child("descr").getValue().toString());
                            descriptionC.add(dataSnapshot.child("details").getValue().toString());
                        }


                    }

                }
                listMain.add("                                      LIST A");
                description.add("List A");
                favourites.add(false);
                for (int countA = 0; countA < listA.size(); countA++){
                    listMain.add(listA.get(countA));
                    description.add(descriptionA.get(countA));
                    favourites.add(false);
                }
                listMain.add("                                      LIST B");
                description.add("List B");
                favourites.add(false);
                for (int countB = 0; countB < listB.size(); countB++){
                    listMain.add(listB.get(countB));
                    description.add(descriptionB.get(countB));
                    favourites.add(false);
                }
                listMain.add("                                      LIST C");
                description.add("List C");
                favourites.add(false);
                for (int countC = 0; countC < listC.size(); countC++){
                    listMain.add(listC.get(countC));
                    description.add(descriptionC.get(countC));
                    favourites.add(false);
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
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

                builder.setTitle(listMain.get(position));
                builder.setMessage(description.get(position));
                final int pos = position;
                builder.setPositiveButton("I'm Interested", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        favourites.set(pos, true);
                    }
                });

                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public void onClick(View view) {
        fav.clear();
        for (int i = 0; i < listMain.size(); i++) {
            if(favourites.get(i)) {
                fav.add(listMain.get(i));
            }
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.list, null);

        builder.setTitle("Interested");
        listView2 = customLayout.findViewById(R.id.simpleListView2);
        ArrayAdapter adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fav);
        listView2.setAdapter(adapter2);
        final Context context = this;

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        builder.setView(customLayout);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

                builder.setTitle(fav.get(position));
                final String course = fav.get(position);
                final int pos = position;
                builder.setPositiveButton("I'm Not Interested", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for (int i = 0; i < listMain.size(); i++) {
                            if (course == listMain.get(i)) {
                                favourites.set(i, false);
                            }
                        }
                    }
                });

                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });






    }
}
