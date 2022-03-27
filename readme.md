Android issue:

https://stackoverflow.com/questions/71638604/android-photo-taken-with-camera-intent-isnt-showing-up-in-gallery

Tags: android, java, android-camera, android-gallery

Android photo taken with camera intent isn't showing up in gallery

I have created an Android sample app that should take a photo with the
build-in camera app ("MediaStore.ACTION_IMAGE_CAPTURE") and make
the photo accessible with the systems MediaGallery.

The app is taking a photo and creates a tempFile - the height and width 
of the taken photo is printedt out in a textview.

Unfortunately the photo isn't showing up in the gallery. I've tested the app 
with real devices (Samsung A5 (Android 9) and Samsung A51 (Android 11)).

Any help is appreciated.

AndroidManifest:

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
package="de.androidcrypto.androidcommonintents">
<uses-feature android:name="android.hardware.camera"
android:required="true" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<application
android:requestLegacyExternalStorage="true"
android:allowBackup="true"
android:icon="@mipmap/ic_launcher"
android:label="@string/app_name"
android:roundIcon="@mipmap/ic_launcher_round"
android:supportsRtl="true"
android:theme="@style/Theme.AndroidCommonIntents">
<activity
android:name=".MainActivity"
android:exported="true">
<intent-filter>
<action android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
<intent-filter>
<action android:name="android.media.action.IMAGE_CAPTURE" />
<category android:name="android.intent.category.DEFAULT" />
</intent-filter>
</activity>
<provider
android:name="androidx.core.content.FileProvider"
android:authorities="de.androidcrypto.androidcommonintents.fileprovider"
android:exported="false"
android:grantUriPermissions="true">
<meta-data
android:name="android.support.FILE_PROVIDER_PATHS"
android:resource="@xml/file_paths" />
</provider>
</application>
</manifest>

file_paths.xml:

<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="my_images" path="Pictures" />
</paths>

MainActivity.java (I've noticed the deprecation of startActivityForResult):

package de.androidcrypto.androidcommonintents;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
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

activity_main.xml:

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:app="http://schemas.android.com/apk/res-auto"
xmlns:tools="http://schemas.android.com/tools"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:orientation="vertical"
tools:context=".MainActivity">

    <Button
        android:id="@+id/btnMain01"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        android:text="take a photo" />

    <TextView
        android:id="@+id/textView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="8dp"
        android:text="photo taking informations" />

</LinearLayout>

build.gradle:

plugins {
id 'com.android.application'
}

android {
compileSdk 31

    defaultConfig {
        applicationId "de.androidcrypto.androidcommonintents"
        minSdk 21
        targetSdk 31
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.5.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.3'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
}

