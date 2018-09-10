package com.example.mdp.group16controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class screenPersistentData extends AppCompatActivity {

    private static final String TAG = "Data1";
    public static String EXTRA_PERSISTENT_DATA = "persistent_data";
    private RadioGroup selectDataGroup;
    private RadioButton selectData1, selectData2;
    private ImageButton sendPersistentData;
    private EditText outputData;
    private Button DataOne, DataTwo;

    //Radio Group Information
    private int id;
    private RadioButton locateId;
    private String idInText;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static final String PREF_NAME = "prefs";
    private static final String KEY_VALUE1 = "value1";
    private static final String KEY_VALUE2 = "value2";

    Toast toast;

    private void showToastMessage(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_persistent_data);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        selectDataGroup = (RadioGroup) findViewById(R.id.radioButton);
        selectData1 = (RadioButton) findViewById(R.id.dataButton1);
        selectData2 = (RadioButton) findViewById(R.id.dataButton2);
        outputData = (EditText) findViewById(R.id.edit_text_outData);
        sendPersistentData = (ImageButton) findViewById(R.id.button_sendPersistentData);
        DataOne = (Button) findViewById(R.id.button_dataOne);
        DataTwo = (Button) findViewById(R.id.button_dataTwo);

        outputData.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            }
        });

        DataOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load text

                // Persistent data
                String info = sharedPreferences.getString(KEY_VALUE1, "");

                // Create the result Intent and include the Persistent data
                Intent intent = new Intent();
                intent.putExtra(EXTRA_PERSISTENT_DATA, info);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        DataTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load text

                // Persistent data
                String info = sharedPreferences.getString(KEY_VALUE2, "");

                // Create the result Intent and include the Persistent data
                Intent intent = new Intent();
                intent.putExtra(EXTRA_PERSISTENT_DATA, info);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        selectDataGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                id = group.getCheckedRadioButtonId();
                locateId = (RadioButton) group.findViewById(id);
                idInText = locateId.getText().toString().trim();
                showToastMessage(idInText);
            }
        });

        sendPersistentData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = outputData.getText().toString().trim();
                if (data.equals("")) {
                    showToastMessage("Field is empty");
                } else {
                    if (idInText != null) {
                        if (idInText.equals("Saving in Data 1")) {
                            editor.putString(KEY_VALUE1, outputData.getText().toString().trim());
                            editor.apply();
                            outputData.setText(null);
                        } else if (idInText.equals("Saving in Data 2")) {
                            editor.putString(KEY_VALUE2, outputData.getText().toString().trim());
                            editor.apply();
                            outputData.setText(null);
                        } else {
                            showToastMessage("Error in saving");
                        }
                    } else {
                        showToastMessage("Select a slot to save");
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
