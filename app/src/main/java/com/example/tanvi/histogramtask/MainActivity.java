package com.example.tanvi.histogramtask;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @Bind(R.id.image)
    ImageView sampleImage;

    @Bind(R.id.image_show)
    ImageView histogram_image;

    @Bind(R.id.root)
    LinearLayout root;

    @Bind(R.id.view_details)
    Button view;


    Integer REQUEST_CAMERA = 1 , SELECT_FILE = 0;

    String sum , sum_of_all , average ;

    private Bundle bundle;


    static {
        if (OpenCVLoader.initDebug()) {
            Log.i("TAG", "OpenCV initialize success");
        } else {
            Log.i("TAG", "OpenCV initialize failed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Snackbar.make(root, getResources().getString(R.string.request_permission_rationale), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getResources().getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, requestedPermissions, PERMISSIONS_REQUEST);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(root, getResources().getString(R.string.request_permission_rationale), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getResources().getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(MainActivity.this, requestedPermissions, PERMISSIONS_REQUEST);
                                }
                            }).show();
                }
            }
        }
    }

    @OnClick(R.id.add_image)
    void onClick() {

        final CharSequence[] items = {"Camera" , "Gallery" , "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add an Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Camera")){

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                }else if(items[which].equals("Gallery")){

                    Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"), SELECT_FILE);

                }else if(items[which].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode , int resultCode , Intent data){
        super.onActivityResult(requestCode , resultCode , data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CAMERA ) {

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                sampleImage.setImageBitmap(bmp);

                draw(bmp);

            }else if(requestCode == SELECT_FILE){

                Bitmap bitmap = null;
                Uri selectImg = data.getData();
                sampleImage.setImageURI(selectImg);

                try {
                     bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectImg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                draw(bitmap);
            }
        }
    }


    private void draw(final Bitmap bmp) {

        final Button button = findViewById(R.id.show_histogram);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mat rgba = new Mat();
                Utils.bitmapToMat(bmp, rgba);

                // Get the bitmap size.
                Size rgbaSize = rgba.size();


                // Set the amount of bars in the histogram.
                int histSize = 256;
                MatOfInt histogramSize = new MatOfInt(histSize);

            // Set the height of the histogram and width of the bar.
                int histogramHeight = (int) rgbaSize.height;
                int binWidth = 5;

            // Set the value range.
                MatOfFloat histogramRange = new MatOfFloat(0f, 256f);

            // Create two separate lists: one for colors and one for channels (these will be used as separate datasets).
                Scalar[] colorsRgb = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
                MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};

            // Create an array to be saved in the histogram and a second array, on which the histogram chart will be drawn.
                Mat[] histograms = new Mat[]{new Mat(), new Mat(), new Mat()};
                Mat histMatBitmap = new Mat(rgbaSize, rgba.type());


                for (int i = 0; i < channels.length; i++) {
                    Imgproc.calcHist(Collections.singletonList(rgba), channels[i], new Mat(), histograms[i], histogramSize, histogramRange);
                    Core.normalize(histograms[i], histograms[i], histogramHeight, 0, Core.NORM_INF);
                    for (int j = 0; j < histSize; j++) {
                        Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[i].get(j - 1, 0)[0]));
                        Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[i].get(j, 0)[0]));
                        Imgproc.line(histMatBitmap, p1, p2, colorsRgb[i], 2, 8, 0);
                    }
                }

                for (int i = 0; i < histograms.length; i++) {
                    calculationsOnHistogram(histograms[i]);
                }

                Bitmap histBitmap = Bitmap.createBitmap(histMatBitmap.cols(), histMatBitmap.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(histMatBitmap, histBitmap);
                BitmapHelper.showBitmap(MainActivity.this, histBitmap, histogram_image);

            }
        });
    }

    private void calculationsOnHistogram(Mat histogram) {
        SparseArray<ArrayList<Float>> compartments = HistogramHelper.createCompartments(histogram);
        float sumAll = HistogramHelper.sumCompartmentsValues(compartments);
        float averageAll = HistogramHelper.averageValueOfCompartments(compartments);

        sum = String.valueOf(Core.sumElems(histogram));
        sum_of_all = String.valueOf(sumAll);
        average = String.valueOf(averageAll);

    }


    public void ViewDetails(View view) {

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Histogram Details");
            alertDialog.setMessage("Total Sum : " + sum + "\n"  + "Sum of all Compartments : " + sum_of_all + "\n" + "Average value of all Compartmemts : " + average );
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

    }
}
