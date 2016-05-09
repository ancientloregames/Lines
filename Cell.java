package com.ancientlore.lines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

class Cell{
    private Ball ball;
    Rect rect;

    Cell(){
        ball = new Ball();
        rect = new Rect();
    }
    Cell(Rect newRect){
        ball = new Ball();
        rect = newRect;
    }

    public boolean reset(){
        return ball.clear();
    }

    public boolean reserve(Context context, char newColor){
        if (newColor >= '0' && newColor <= '9' && ball.getColor()=='.') {
            ball.setColor(newColor);
            ball.getImage(context, false);
            int halfCellSize=(rect.right-rect.left)/2;
            ball.image =  Bitmap.createScaledBitmap(ball.image,
                    halfCellSize, halfCellSize, false);
            return true;
        } else return false;
    }

    public void apply(Context context){
        ball.getImage(context,true);
        int cellSize=rect.right-rect.left;
        ball.image =  Bitmap.createScaledBitmap(ball.image,
                cellSize, cellSize, false);
    }

    public Ball getBall(){return ball;}
    public void setBall(Context context, char color){
        ball.setColor(color);
        apply(context);
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
0 = 'r' = red
1 = 'g' = green
2 = 'b' = blue
3 = 'c' = cyan
4 = 'm' = magenta
5 = 'y' = yellow
*/