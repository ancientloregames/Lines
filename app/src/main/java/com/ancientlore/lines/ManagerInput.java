package com.ancientlore.lines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

class ManagerInput {
    private static volatile ManagerInput instance=null;
    private GameButton menuButton;
    private int selectedX=0,selectedY=0;
    private int newX=0,newY=0;

    private ManagerInput(){}
    static ManagerInput getInstance(){
        if (instance==null) {
            synchronized (ManagerInput.class) {
                if (instance == null)
                    instance = new ManagerInput();
            }
        }
        return instance;
    }

    void initialize(Context context, final int maxX, final int maxY){
        final int padding=maxY/10;
        Bitmap menuBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.button_menu);
        int ratio = menuBitmap.getWidth()/menuBitmap.getHeight();
        menuBitmap= Bitmap.createScaledBitmap(menuBitmap,maxX/10*ratio,
                (maxY-2*padding),false);
        menuButton=new GameButton("Menu",new Rect(maxX-menuBitmap.getWidth()-padding,
                padding, maxX-padding,menuBitmap.getHeight()+padding),menuBitmap);
    }

    void handleInput(final int x,final int y, ManagerGame _gm, ManagerLevel _lm){
        if (_lm.getGameField().contains(x,y)) {
            final int col=(y-_lm.getGameField().top)/_lm.getTileSize();
            final int row=(x-_lm.getGameField().left)/_lm.getTileSize();
            switch (_gm.getGameState()){
                case GENERATED:
                    if (_lm.getCell(col,row).getState()==CellState.CONTAINS){
                        selectedX=row;
                        selectedY=col;
                        _gm.setGameState(GameState.SELECTED);
                    }
                    break;
                case SELECTED:
                    if (_lm.getCell(col,row).getState()!=CellState.CONTAINS){
                        newX=row;
                        newY=col;
                        _gm.setGameState(GameState.MOVED);
                    }else{
                        selectedX=row;
                        selectedY=col;
                    }
                    break;
            }
        }
    }
    public GameButton getMenuButton() {return menuButton;}
    int getSelectedX() {return selectedX;}
    int getSelectedY() {return selectedY;}
    int getNewX() {return newX;}
    int getNewY() {return newY;}
}
