package com.uniquemiban.travelmanager;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Transformation;

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
        if (result != source) {
            source.recycle();
        }
        return result;
    }

    @Override public String key() { return "rect()"; }
}
