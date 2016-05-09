package com.ancientlore.lines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class Ball{
    private char color;
    public char getColor(){return color;}
    public void setColor(char newColor){color=newColor;}
    Bitmap image;
    boolean isBig;

    Ball(){
        color='.';
        image = null;
    }

    protected boolean clear(){
        color='.';
        image = null;
        return true;
    }

    protected boolean getImage(Context context, boolean isNewBig){
        isBig=isNewBig;
        if (!isNewBig) {
            switch (color) {
                case '0':
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.red);
                    return true;
                case '1':
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.green);
                    return true;
                case '2':
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.blue);
                    return true;
                case '3':
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.cyan);
                    return true;
                case '4':
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.magenta);
                    return true;
                case '5':
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.yellow);
                    return true;
                default:
                    return false;
            }
        }else {
            switch (color) {
                case '0':case 'r':
                    this.setColor('r');
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.red);
                    return true;
                case '1':case 'g':
                    this.setColor('g');
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.green);
                    return true;
                case '2':case 'b':
                    this.setColor('b');
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.blue);
                    return true;
                case '3':case 'c':
                    this.setColor('c');
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.cyan);
                    return true;
                case '4':case 'm':
                    this.setColor('m');
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.magenta);
                    return true;
                case '5':case 'y':
                    this.setColor('y');
                    image = BitmapFactory.decodeResource
                            (context.getResources(), R.drawable.yellow);
                    return true;
                default:
                    return false;
            }
        }
    }

}
/*
'.' = empty
'0' = reserved red
'1' = reserved green
'2' = reserved blue
'3' = reserved cyan
'4' = reserved magenta
'5' = reserved yellow
'r' = applied red
'g' = applied green
'b' = applied blue
'c' = applied cyan
'm' = applied magenta
'y' = applied yellow
*/
