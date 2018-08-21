package com.ancientlore.lines;

import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class ActivitySettings extends Activity implements View.OnClickListener {
    ManagerGame _gm= ManagerGame.getInstance();
    CheckBox panelCB,gridCB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        panelCB=(CheckBox)findViewById(R.id.checkBoxPanel);
        panelCB.setChecked(_gm.isShowNextOnPanel());
        gridCB=(CheckBox)findViewById(R.id.checkBoxGrid);
        gridCB.setChecked(_gm.isShowNextOnGrid());

        Button buttonApply=(Button)findViewById(R.id.buttonApply);
        buttonApply.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonApply:
                ManagerGame _gm= ManagerGame.getInstance();
                _gm.setShowNextOnPanel(panelCB.isChecked());
                _gm.setShowNextOnGrid(gridCB.isChecked());
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
    }
}
