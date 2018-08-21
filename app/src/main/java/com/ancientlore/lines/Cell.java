package com.ancientlore.lines;

import android.graphics.Rect;

class Cell {
    private int index;
    private String color;
    private Rect rect;
    private Rect smallRect;
    private CellState state;

    Cell(Rect rect){
        index=0;
        color="none";
        this.rect=rect;
        final int padding=(rect.right-rect.left)/4;
        smallRect=new Rect(rect.left+padding,rect.top+padding,
                rect.right-padding,rect.bottom-padding);
        state=CellState.EMPTY;
    }

    public int getIndex() {return index;}
    public String getColor() {return color;}
    public Rect getRect() {return rect;}
    public Rect getSmallRect() {return smallRect;}
    public CellState getState() {return state;}

    public void setIndex(int index) {this.index = index;}
    public void setColor(String color) {this.color = color;}
    public void setRect(Rect rect) {this.rect = rect;}
    public void setState(CellState state) {this.state = state;}
    void setBallCandidate(final int index, final String color){
        this.index=index;
        this.color=color;
        state=CellState.CANDIDATE;
    }
    void setBall(final int index, final String color){
        this.index=index;
        this.color=color;
        state=CellState.CONTAINS;
    }
    void reset(){
        index=0;
        color="none";
        state=CellState.EMPTY;
    }
}
