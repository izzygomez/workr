package com.izzygomez.workr;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    boolean deleteMode = false;
    float oldX = Float.NaN;
    float oldY = Float.NaN;
    static final int DELTA = 50;
    enum Direction{LEFT, RIGHT;}
    int lastClickedRow = 10000;
    ArrayList<String> lastClickedRowArray = new ArrayList<String>();
    ArrayList<String> taskInputData = new ArrayList<String>();

//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView taskListView = (ListView) findViewById(R.id.listViewOfTasks);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);

        adapter.notifyDataSetChanged();
        Log.d("listItems",listItems.toString());
        Toast.makeText(this, listItems.toString(), Toast.LENGTH_LONG).show();
        taskListView.setAdapter(adapter);


        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                lastClickedRow = position;
                String[] lastClickedArrayString = arg0.getItemAtPosition(position).toString().split(" ");
                for(String s: lastClickedArrayString){
                    lastClickedRowArray.add(s);
                }

//                if (deleteMode){
//                    arg0.getItemAtPosition(position);
//                    listItems.remove(position);
//                    adapter.notifyDataSetChanged();
//                }
//            arg0.getItemAtPosition(position);
            }
        });

        taskListView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        oldX = event.getX();
                        oldY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (event.getX() - oldX < -DELTA) {
                            //Right now this is basically swipe to delete, but maybe it would be better if it were swipe
                            //to get the delete option so like a delete button appears in line, and then click that to delete
                            //That way tasks are not accidentally deleted.
                            removeLastClickedRow();
                            Log.d("ran", "ran");
                        } else if (event.getX() - oldX > DELTA) {
                            removeLastClickedRow();
                            Log.d("ran", "ran");
                        }
                        else{
                            goToTaskInputScreen();
                        }
                        break;
                    default: return false;
                }
                return false;
            }

        });
    }
    public void goToTaskInputScreen(){
        Intent taskInputIntent = new Intent(this, TaskInputScreen.class);
        taskInputIntent.putExtra("taskData", lastClickedRowArray);
        startActivityForResult(taskInputIntent, 5);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if ((requestCode == 5) &&
                (resultCode == RESULT_OK)) {

            taskInputData = data.getExtras().getStringArrayList("returnData");
            Log.d("taskInputData", taskInputData.toString());
//            listItems.remove(lastClickedRow);
//            adapter.notifyDataSetChanged();
            addToList(taskInputData);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void addToList(ArrayList<String> taskInputs){
        String listViewInput = "";
        for (String input: taskInputs){
            listViewInput += input + " ";
        }
        if(!(listItems.contains(listViewInput))) {
            listItems.add(listViewInput);
            adapter.notifyDataSetChanged();
            lastClickedRowArray = taskInputs;
        }
    }

    public void clickedPlus(View v){
        lastClickedRowArray = new ArrayList<String>();
        goToTaskInputScreen();
//        List<String> taskInputs = new ArrayList<String>();
//        taskInputs.add("This");
//        taskInputs.add("is");
//        taskInputs.add("a");
//        taskInputs.add("test");
//        addToList(taskInputs);
    }

    public void addToListTest(View v){
        ArrayList<String> taskInputs = new ArrayList<String>();
        taskInputs.add("This");
        taskInputs.add("is");
        taskInputs.add("a");
        taskInputs.add("test");
        addToList(taskInputs);
    }

    public void setDeleteMode(View v){
        if (deleteMode){
            deleteMode = false;
        }
        else{
            deleteMode = true;
        }
        Toast.makeText(this, Integer.toString(lastClickedRow), Toast.LENGTH_LONG).show();
    }

    public void removeLastClickedRow(){
        if (lastClickedRow != 10000){
            listItems.remove(lastClickedRow);
            adapter.notifyDataSetChanged();
        }
    }
}
