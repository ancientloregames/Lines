package com.ancientlore.lines;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener{
    static SharedPreferences prefs;
    static SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs =this.getSharedPreferences("HiScore", Context.MODE_PRIVATE);
        editor=prefs.edit();
        ((TextView)findViewById(R.id.textViewHiScore)).setText(""+prefs.getInt("HiScore", 0));
        MainActivity.editor.apply();
        final Button buttonPlay = (Button)findViewById(R.id.buttonStart);
        buttonPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonStart:
                Intent i = new Intent(this,GameActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        else return  false;
    }
}