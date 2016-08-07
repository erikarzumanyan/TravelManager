package com.uniquemiban.travelmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Transformation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PicassoTransformation implements Transformation {

    private int mWidth;

    public PicassoTransformation(int pWidth){
        this.mWidth = pWidth;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int pWidth) {
        mWidth = pWidth;
    }

    
    @Override public Bitmap transform(Bitmap source) {
        double d = (double)source.getWidth()/source.getHeight();
        Bitmap result = Bitmap.createScaledBitmap(source, mWidth, (int)(mWidth/d), true);

//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        if(result.compress(Bitmap.CompressFormat.PNG, 10, out)) {
//            result = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
//        }

        if (result != source) {
            source.recycle();
        }

        return result;
    }

    @Override public String key() { return "rect()"; }
}
