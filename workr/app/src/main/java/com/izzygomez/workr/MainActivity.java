package com.izzygomez.workr;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    boolean deleteMode = false;
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
                if (deleteMode){
                    arg0.getItemAtPosition(position);
                    listItems.remove(position);
                    adapter.notifyDataSetChanged();
                }
//            arg0.getItemAtPosition(position);
            }
        });
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

    public void addToList(List<String> taskInputs){
        String listViewInput = "";
        for (String input: taskInputs){
            listViewInput += input + " ";
        }
        listItems.add(listViewInput);
        adapter.notifyDataSetChanged();
    }

    public void addToListTest(View v){
        List<String> taskInputs = new ArrayList<String>();
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
    }
}
