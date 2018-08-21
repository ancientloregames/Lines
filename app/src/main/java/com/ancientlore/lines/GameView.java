package com.ancientlore.lines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class GameView extends SurfaceView implements Runnable {
    Thread thread = null;
    private volatile boolean running;
    private int displayX, displayY;
    private int upperPanelY;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;
    private Paint paintInfo;
    private RectF nextBallsPanel;
    private String score_str;

    ManagerGame _gm;
    ManagerInput _im;
    ManagerLevel _lm;
    ManagerSound _sm;

    public GameView(final Context context) {super(context);}
    public GameView(final Context context, final int displayX, final int displayY) {
        super(context);
        upperPanelY=displayY/12;
        this.displayX = displayX;
        this.displayY = displayY;

        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setTextSize(25);
        paint.setColor(Color.BLACK);
        paintInfo = new Paint();
        paintInfo.setTextSize(displayX/21);
        paintInfo.setColor(Color.WHITE);
        paintInfo.setTextAlign(Paint.Align.LEFT);

        _gm=ManagerGame.getInstance();
        _gm.initialize();
        _lm=ManagerLevel.getInstance();
        _lm.initialize(context,displayX,displayY,upperPanelY);
        _im=ManagerInput.getInstance();
        _im.initialize(context,displayX,upperPanelY);
        _sm=ManagerSound.getInstance();
        _sm.initialize(context);

        _gm.setBestScore(ActivityMain.prefs.getInt("HiScore", 0));
        ActivityMain.editor.apply();

        score_str = getResources().getString(R.string.ingame_score);

        final int paddingX=(int)(displayX/2-_lm.getTileSize()*1.5f);
        final int paddingY=(int)(upperPanelY/2-_lm.getTileSize()/2);
        nextBallsPanel=new RectF(paddingX,paddingY,displayX-paddingX,upperPanelY-paddingY);
    }

    @Override
    public void run() {
        while (running){
            _lm.update(_gm,_im,_sm);
            draw();
            try {
                Thread.sleep(51);//17 = 1000(milliseconds)/60(FPS)
            }catch(InterruptedException e){Log.e("Error","Can't sleep on run()");}
        }
    }

    void  draw(){
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            //-----------------
            canvas.drawColor(Color.DKGRAY);
            canvas.drawRect(new Rect(0,0,displayX,upperPanelY),paint);
            canvas.drawText(score_str+": " + _gm.getScore(), paintInfo.getTextSize(),
                    upperPanelY/2+paintInfo.getTextSize()/2, paintInfo);
            if (_gm.isShowNextOnPanel()) {
                canvas.drawRoundRect(new RectF(nextBallsPanel),50f,50f,paintInfo);
                //canvas.drawRect(nextBallsPanel, paintInfo);
                for (int i = 0; i < _lm.getCountNextBalls(); i++) {
                    Point pos = _lm.getNextBall(i);
                    canvas.drawBitmap(_lm.getSprite(_lm.getCell(pos.y, pos.x).getIndex()).getBitmap(),
                            nextBallsPanel.left + i * _lm.getTileSize(), nextBallsPanel.top, paint);
                }
            }

            canvas.drawBitmap(_im.getMenuButton().getBitmap(),
                    _im.getMenuButton().getRect().left,_im.getMenuButton().getRect().top,paint);

            /*if (_gm.isShowPath() && !_gm.isPathShown()){
                Rect tmp;
                for (int i=0;i<_lm.getPath().length;i++) {
                    tmp=_lm.getCell(_lm.getPath()[i].y, _lm.getPath()[i].x).getSmallRect();
                    canvas.drawBitmap(_lm.getSprite(_lm.getCell(_im.getNewY(), _im.getNewX()).getIndex()).getBitmap(),
                            null,tmp,paint);
                }
                _gm.setPathShown(true);
            }*/

            for (int i=0;i<_lm.getMatrixSizeY();i++){
                for (int j=0;j<_lm.getMatrixSizeX();j++) {
                    canvas.drawBitmap(_lm.getSprite(_lm.getTileIndex()).getBitmap(),
                            _lm.getCell(i, j).getRect().left, _lm.getCell(i, j).getRect().top, paint);
                    if(_gm.getGameState()==GameState.SELECTED){
                        if (i==_im.getSelectedY() && j==_im.getSelectedX()){
                            paint.setColor(Color.argb(180,0,0,0));
                            canvas.drawRect(_lm.getCell(_im.getSelectedY(),
                                    _im.getSelectedX()).getRect(), paint);
                            paint.setColor(Color.BLACK);
                        }
                    }
                    if (_lm.getCell(i, j).getState()==CellState.CONTAINS)
                        canvas.drawBitmap(_lm.getSprite(_lm.getCell(i, j).getIndex()).getBitmap(),
                                _lm.getCell(i, j).getRect().left,
                                _lm.getCell(i, j).getRect().top, paint);
                    else if (_gm.isShowNextOnGrid() &&
                            _lm.getCell(i, j).getState()==CellState.CANDIDATE)
                        canvas.drawBitmap(_lm.getSprite(_lm.getCell(i, j).getIndex()).getBitmap(),
                                null, _lm.getCell(i, j).getSmallRect(), paint);
                    if (_gm.isDebug())
                        canvas.drawText(""+_lm.getGridCell(i+1,j+1),
                                _lm.getCell(i, j).getRect().centerX(),
                                _lm.getCell(i, j).getRect().centerY(),paintInfo);
                }
            }
            //-----------------
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    protected void pause(){
        running=false;
        try{
            thread.join();
        }catch (InterruptedException e){
            Log.e("Error","Can't pause");}
    }
    protected void resume(){
        running=true;
        thread = new Thread(this);
        thread.start();
    }
}