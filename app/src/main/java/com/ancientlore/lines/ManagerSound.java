package com.ancientlore.lines;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.lang.reflect.Field;
import java.util.ArrayList;

class ManagerSound {
    private static volatile ManagerSound instance=null;
    private SoundPool soundPool;
    GameSound[] sounds;

    private ManagerSound(){}
    static ManagerSound getInstance(){
        if (instance==null) {
            synchronized (ManagerSound.class) {
                if (instance == null)
                    instance = new ManagerSound();
            }
        }
        return instance;
    }

    void initialize(Context context){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sounds = getAllSounds(context,R.raw.class);
    }

    private GameSound[] getAllSounds(Context context,Class<?> aClass) throws IllegalArgumentException{
        Field[] fields = aClass.getFields();

        ArrayList<GameSound> res = new ArrayList<>();
        try {
            for(Field field:fields){
                if (field.getName().contains("lines_sound")) {
                    res.add(new GameSound(soundPool.load(context,field.getInt(null),1),
                            field.getName()));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException();/* Exception will only occur on bad class submitted. */
        }
        return res.toArray(new GameSound[res.size()]);
    }

    void playSound(String name) {
        //name = "snake_sound_"+name;
        for (GameSound sound:sounds)
            if (sound.getName().contains(name))
                soundPool.play(sound.getIndex(), 1, 1, 0, 0, 1);
    }
}
