package com.ancientlore.lines;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;

public class ActivityGame extends Activity {
    GameView gameView;

    ManagerGame _gm;
    ManagerInput _im;
    ManagerLevel _lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        _gm=ManagerGame.getInstance();
        _lm=ManagerLevel.getInstance();
        _im=ManagerInput.getInstance();

        gameView=new GameView(this,displaySize.x,displaySize.y);
        setContentView(gameView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameView.resume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        int x = (int) me.getX();
        int y = (int) me.getY();
        switch (me.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (_im.getMenuButton().getRect().contains(x,y)){
                    Intent i = new Intent(this,ActivityMenu.class);
                    startActivity(i);
                }
                if (_gm.getGlobalState()==GlobalState.PLAYING)
                    _im.handleInput((int) me.getX(),(int) me.getY(),_gm,_lm);
                break;
        }
        return true;
    }
}
