package com.example.tanvi.histogramtask;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {

    protected String[] requestedPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    protected static final String KEY_BITMAP = "IMAGE_PATH";
    protected static final int SELECT_PICTURE = 1;
    protected static final int PERMISSIONS_REQUEST = 9;

}
