package com.ancientlore.lines;

class GameSound {
    private int index;
    private String name;

    GameSound(int newIndex, String newName){
        index=newIndex;
        name=newName;
    }

    int getIndex(){return index;}
    String getName(){return name;}
}