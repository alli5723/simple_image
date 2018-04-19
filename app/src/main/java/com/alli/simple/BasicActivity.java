package com.alli.simple;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class BasicActivity extends AppCompatActivity {

    Button capture;
    ImageView imageView;
    String bmpURL = "";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final int CAPTURE_IMAGE_REQUEST_CODE = 1;
    public static final String CURRENT_IMAGE = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capture = (Button)findViewById(R.id.capture);
        imageView = (ImageView)findViewById(R.id.preview);

        if (savedInstanceState != null) {
            bmpURL = savedInstanceState.getString(CURRENT_IMAGE);
            imageView.setImageURI(Uri.parse(bmpURL));
        }

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCamera();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_IMAGE, bmpURL);
        super.onSaveInstanceState(outState);
    }

    private void showCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                showAlert();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_CAMERA);

            }
        } else {
            bmpURL = com.alli.simple.utils.Utils.insertImage(getContentResolver(),
                    null, "Simple_");
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(bmpURL));
            startActivityForResult(intent,CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(BasicActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(BasicActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Image captured and converted", Toast.LENGTH_LONG).show();
                Bitmap bmp = com.alli.simple.utils.Utils.getBitmap(bmpURL, this);
                imageView.setImageBitmap(getModified(bmp));

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Image not captured.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Bitmap getModified(Bitmap b){
        Mat tmp = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(b, tmp);
        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(tmp, b);
        return b;
    }

    static {
        System.loadLibrary("opencv_java3");
    }
}
