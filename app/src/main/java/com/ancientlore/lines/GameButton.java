package com.ancientlore.lines;

import android.graphics.Bitmap;
import android.graphics.Rect;

class GameButton {
    private String label;
    private Rect rect;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;

    GameButton(String newLabel, Rect newRect, Bitmap newBitmap){
        label=newLabel;
        rect=newRect;
        bitmap=newBitmap;
    }

    //public String getLabel() {return label;}
    public Rect getRect() {return rect;}
    public Bitmap getBitmap() {return bitmap;}

    /*public void setRect(Rect rect) {this.rect = rect;}
    public void setLabel(String label) {this.label = label;}*/
}
