package com.example.tanvi.histogramtask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.OpenCVLoader;

import butterknife.Bind;

public class MainActivity extends BaseActivity {

    @Bind(R.id.image)
    ImageView sampleImage;

    @Bind(R.id.root)
    LinearLayout root;

    Integer REQUEST_CAMERA = 1 , SELECT_FILE = 0;

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
    }
}
