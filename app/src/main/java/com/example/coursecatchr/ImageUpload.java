package com.example.coursecatchr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImageUpload extends AppCompatActivity implements PickiTCallbacks, View.OnClickListener {
    public static final int PICKFILE_RESULT_CODE = 1;
    private Bitmap myBitmap;
    private ImageView myImageView;
    private EditText myEditView;
    public static final int WRITE_STORAGE = 100;
    public static final int SELECT_PHOTO = 102;
    public File photo;
    PickiT pickiT;
    public String filePath = "error";

    public class Course {
        String department;
        String number;
    }


    ArrayList<Course> courses = new ArrayList<Course>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        myEditView = findViewById(R.id.editView);
        myImageView = findViewById(R.id.imageView);
        findViewById(R.id.checkText).setOnClickListener(this);
        findViewById(R.id.select_image).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.help).setOnClickListener(this);
        pickiT = new PickiT(this, this, this);

        RelativeLayout relativeLayout = findViewById(R.id.image_upload);
        relativeLayout.setBackgroundColor(Color.rgb(204, 255, 255));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecognition();
                }
                break;
            case R.id.select_image:
                checkPermission(WRITE_STORAGE);
                break;
            case R.id.submit:
                saveData();
                ArrayList<String> courseList = new ArrayList<String>();
                for (int i = 0; i < courses.size(); i++) {
                    courseList.add(courses.get(i).department + courses.get(i).number);
                }
                Intent activity2Intent = new Intent(getApplicationContext(), QueryingAPI.class);
                activity2Intent.putExtra("Courses", courseList);
                startActivity(activity2Intent);
                break;
            case R.id.help:
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

                final View customLayout = getLayoutInflater().inflate(R.layout.help, null);
                builder.setView(customLayout);

                customLayout.setBackgroundColor(Color.rgb(253, 214, 183));

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, 1300);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission(requestCode);
                    break;
                case SELECT_PHOTO:
                    Uri dataUri = data.getData();
                    String path = CommonUtils.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = CommonUtils.resizePhoto(photo, this, dataUri, myImageView);
                    } else {
                        myBitmap = CommonUtils.resizePhoto(photo, path, myImageView);
                    }
                    if (myBitmap != null) {
                        myEditView.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                    }
                    break;
                case PICKFILE_RESULT_CODE:
                    Uri fileUri = data.getData();
                    pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
                    photo = new File(filePath);
                    if (filePath == null) {
                        myBitmap = CommonUtils.resizePhoto(photo, this, fileUri, myImageView);
                    } else {
                        myBitmap = CommonUtils.resizePhoto(photo, filePath, myImageView);
                    }
                    if (myBitmap != null) {
                        myEditView.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                    }
                    break;
            }
        }
    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(myBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(ImageUpload.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveData() {
        String courseList = myEditView.getText().toString();
        courses.clear();
        String[] course = (courseList).split("\\r?\\n?\\s");
        for (int i = 0; i < course.length; i += 2) {
            Course curr = new Course();
            curr.department = course[i];
            if (i+1 < course.length) {
                curr.number = course[i + 1];
            }
            courses.add(curr);
        }
    }

    private void createOutput() {
        for (int i = courses.size() -1; i >= 0; i--) {
            if ((courses.get(i).department.equals("0")) || (courses.get(i).number.equals("0"))
                    || (courses.get(i).department.equals("WKRPT"))) {
                courses.remove(i);
            }
        }
        for (int i = 0; i < courses.size(); i++) {
            myEditView.append(courses.get(i).department + " " + courses.get(i).number + "\n");
        }
    }

    private void processLine(String line1) {
        String line = line1;
        line = line.replaceAll("\\s+","");
        Log.e("f", line);
        int length = line.length();
        if (length < 7 && length > 1) {
            boolean upper = true;
            for (int i = 0; i < length; i++) {
                char curr = line.charAt(i);
                if (!Character.isUpperCase(curr)) {
                    upper = false;
                    break;
                }
            }
            if (upper) {
                if (line.equals("PD") || line.equals("COOP") || line.equals("CR")) {
                    return;
                }
                boolean newC = true;
                for (int i = 0; i < courses.size(); i++) {
                    if (courses.get(i).department == "0") {
                        courses.get(i).department = line;
                        newC = false;
                        break;
                    }
                }
                if (newC) {
                    Course curr = new Course();
                    curr.department = line;
                    curr.number = "0";
                    courses.add(curr);
                }
                return;
            }
            if (length > 2) {
                boolean num = true;
                for (int i = 0; i < length; i++) {
                    char curr = line.charAt(i);
                    if (!Character.isDigit(curr)) {
                        if ((i != 3) || (length != 4)) {
                            num = false;
                            break;
                        }
                    }
                }
                if (num) {
                    boolean newC = true;
                    for (int i = 0; i < courses.size(); i++) {
                        if (courses.get(i).number == "0") {
                            courses.get(i).number = line;
                            newC = false;
                            break;
                        }
                    }
                    if (newC) {
                        Course curr = new Course();
                        curr.department = "0";
                        curr.number = line;
                        courses.add(curr);
                    }
                }
            }
        }
    }

    private void processExtractedText(Text firebaseVisionText) {
        myEditView.setText(null);
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            myEditView.setText(R.string.no_text);
            return;
        }
        for (Text.TextBlock block : firebaseVisionText.getTextBlocks()) {
            String[] block2 = (block.getText()).split("\\r?\\n");
            for (int i = 0; i < block2.length; i++) {
                processLine(block2[i]);
            }
            Log.d("f", block.getText());
        }
        createOutput();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_STORAGE:

                //If the permission request is granted, then...//
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //...call selectPicture//
                    selectPicture();
                    //If the permission request is denied, then...//
                } else {
                    //...display the “permission_request” string//
                    requestPermission(this, requestCode, R.string.permission_request);
                }
                break;
        }
    }

    //Display the permission request dialog//
    public static void requestPermission(final Activity activity, final int requestCode, int msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setMessage(msg);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent permissonIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                permissonIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(permissonIntent, requestCode);
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.setCancelable(false);
        alert.show();
    }

    //Check whether the user has granted the WRITE_STORAGE permission//
    public void checkPermission(int requestCode) {
        switch (requestCode) {
            case WRITE_STORAGE:
                int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                //If we have access to external storage...//
                if (hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                    //...call selectPicture, which launches an Activity where the user can select an image//
                    selectPicture();
                    //If permission hasn’t been granted, then...//
                } else {
                    //...request the permission//
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                }
                break;

        }
    }

    private void selectPicture() {
        photo = CommonUtils.createTempFile(photo);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent = Intent.createChooser(intent, "Choose an image");
        //Start an Activity where the user can choose an image//
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }


    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
            filePath = path;
    }
}