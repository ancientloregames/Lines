package com.ancientlore.lines;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;

public class ActivityMenu extends Activity implements View.OnClickListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Button buttonResume = (Button)findViewById(R.id.buttonResume);
        buttonResume.setOnClickListener(this);
        final Button buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(this);
        final Button buttonSettings = (Button)findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonResume:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.buttonNewGame:
                ManagerGame _gm = ManagerGame.getInstance();
                _gm.reset();
                ManagerLevel _lm = ManagerLevel.getInstance();
                _lm.reset();
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.buttonSettings:
                Intent i=new Intent(this,ActivitySettings.class);
                startActivity(i);
        }
    }
}
