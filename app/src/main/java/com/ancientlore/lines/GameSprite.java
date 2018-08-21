package com.ancientlore.lines;

import android.graphics.Bitmap;

class GameSprite {
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Bitmap bitmap;
    private String name;

    GameSprite(){
        bitmap=null;
        name="none";
    }
    GameSprite(Bitmap Bitmap, String Name){
        bitmap=Bitmap;
        name=Name;
    }
}
