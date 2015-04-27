package com.izzygomez.workr;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;


public class TaskInputScreen extends ActionBarActivity {
    ArrayList<String> passedData = new ArrayList<String>();
    EditText assignmentText;
    EditText completionTimeText;
    EditText dueDateText;
    EditText priorityText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_input_screen);
        assignmentText = (EditText) findViewById(R.id.editText5);
        completionTimeText = (EditText) findViewById(R.id.editText6);
        dueDateText = (EditText) findViewById(R.id.editText7);
        priorityText = (EditText) findViewById(R.id.editText8);

        Bundle extras = getIntent().getExtras();
        if (extras == null){
            return;
        }
        passedData = extras.getStringArrayList("taskData");
        Log.d("passedData", passedData.toString());
        if (passedData.size() != 0){
            assignmentText.setText(passedData.get(0));
            completionTimeText.setText(passedData.get(1));
            dueDateText.setText(passedData.get(2));
            priorityText.setText(passedData.get(3));
        }
    }

    public void onClickSave(View v){
        ArrayList<String> returnData = new ArrayList<String>();
        if (assignmentText.getText().length() == 0){
            returnData.add("Assignment");
        }
        else{
            returnData.add(assignmentText.getText().toString());
        }

        if (completionTimeText.getText().length() == 0){
            returnData.add("1");
        }
        else{
            returnData.add(completionTimeText.getText().toString());

        }
        if (dueDateText.getText().toString().length() == 0){
            Calendar today = Calendar.getInstance();
            returnData.add(Integer.toString(today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.YEAR));
        }
        else{
            returnData.add(dueDateText.getText().toString());
        }
        if (priorityText.getText().toString().length() == 0){
            returnData.add("low");
        }
        else{
            returnData.add(priorityText.getText().toString());
        }
//        returnData.add(assignmentText.getText().toString());
//        returnData.add(completionTimeText.getText().toString());
//        returnData.add(dueDateText.getText().toString());
//        returnData.add(priorityText.getText().toString());

        addToList(returnData);

    }

    public void addToList(ArrayList<String> returnData){
        Log.d("returnData", returnData.toString());
        Intent data = new Intent();
        data.putExtra("returnData", returnData);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_input_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
