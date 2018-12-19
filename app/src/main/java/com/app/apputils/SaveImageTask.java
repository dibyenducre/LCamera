package com.app.apputils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.app.lcamera.CameraActivity;

import static com.app.apputils.Constant.MEDIA_TYPE_IMAGE;

public class SaveImageTask extends AsyncTask<Bitmap, Void, Constant.FileSaveStatus> {

    private Context context;

    public SaveImageTask(Context context){
        this.context = context;
    }

    @Override
    protected Constant.FileSaveStatus doInBackground(Bitmap... params) {

        return FileHelper.saveFile(context, MEDIA_TYPE_IMAGE, params[0]);
    }

    @Override
    protected void onPostExecute(Constant.FileSaveStatus fileSaveStatus) {
        if(context instanceof CameraActivity){
            ((CameraActivity)context).fileSaveComplete(fileSaveStatus);
        }
    }
}
