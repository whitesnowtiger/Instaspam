package com.example.instaspam.Utils;

import android.content.Context;
import android.os.Environment;

import com.example.instaspam.Share.GalleryFragment;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class FilePaths {

    Context context;

    //"storage/emulated/0"

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();
    //public String ROOT_DIR = context.getExternalFilesDir(null).getAbsolutePath();
    //public String ROOT_DIR = context.getExternalFilesDir(null).getPath();


    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/Camera";
    //public String CAMERA = ROOT_DIR + "/Camera";
    public String DOWNLOAD = ROOT_DIR + "/Download";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";



}
