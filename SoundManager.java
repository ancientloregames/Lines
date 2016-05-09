package com.ancientlore.lines;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

class SoundManager {
    private SoundPool soundPool;
    int move = -1;
    int path_blocked = -1;
    public void loadSound(Context context){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        move = soundPool.load(context,R.raw.move,1);
        path_blocked = soundPool.load(context,R.raw.path_blocked,1);
    }
    public void playSound(String sound) {
        switch (sound) {
            case "move":
                soundPool.play(move, 1, 1, 0, 0, 1);
                break;
            case "path_blocked":
                soundPool.play(path_blocked, 1, 1, 0, 0, 1);
                break;
        }
    }
}
