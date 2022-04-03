package de.androidcrypto.androidcommonintents;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivityIssue extends AppCompatActivity {
    // examples taken from here:
    // https://developer.android.com/reference/android/provider/MediaStore.Images

    Button btnMain01;
    TextView textView;

    static final int  REQUEST_IMAGE_CAPTURE_FULL_RESOLUTION = 4;
    String currentPhotoPath;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMain01 = findViewById(R.id.btnMain01);
        textView = findViewById(R.id.textView);

        btnMain01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("### 02 take a photo full");
                context = v.getContext();
                dispatchTakePictureIntentFullResolution();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE_FULL_RESOLUTION && resultCode == RESULT_OK) {
            System.out.println("REQUEST_IMAGE_CAPTURE_FULL_RESOLUTION");
            Bitmap bm = BitmapFactory.decodeFile(currentPhotoPath);
            String info = "taken picture height: "  + bm.getHeight() + " width: " + bm.getWidth();
            textView.setText(info);
            galleryAddPic(context, currentPhotoPath);
        }
    }

    private void dispatchTakePictureIntentFullResolution() {
        // https://developer.android.com/reference/android/provider/MediaStore.Images#TaskPath
        System.out.println("dispatchTakePictureIntentFullResolution");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            System.out.println("takePictureIntent.resolveActivity(getPackageManager()) != null");
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                System.out.println("photoFile: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println("IOExeception: " + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                System.out.println("photoFile created");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "de.androidcrypto.androidcommonintents.fileprovider",
                        photoFile);
                System.out.println("uri: " + photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_FULL_RESOLUTION);
            }
        }
    }

    private File createImageFile() throws IOException {
        // https://developer.android.com/training/camera/photobasics#TaskPath
        System.out.println("createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        galleryAddPic(context, currentPhotoPath);
        return image;
    }

    //added the photo file to gallery
    private static void galleryAddPic(Context context, String imagePath) {
        System.out.println("galleryAddPic");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        //Uri contentUri = Uri.fromFile(f);
        Uri contentUri = FileProvider.getUriForFile(context,
                "de.androidcrypto.androidcommonintents.fileprovider",
                f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}