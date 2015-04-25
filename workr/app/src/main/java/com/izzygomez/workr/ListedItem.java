package com.izzygomez.workr;

import java.util.ArrayList;

/**
 * Created by Jordan on 4/25/2015.
 */
public class ListedItem {
    private String name;
    private String estimatedTime;
    private String date;
    private String priority;
    private boolean selected = false;

    public ListedItem(String name, String estimatedTime, String date, String priority){
        this.name = name;
        this.estimatedTime = estimatedTime;
        this.date = date;
        this.priority = priority;
    }

    public boolean isSelected(){
       return selected;
    }

    public ArrayList<String> returnArrayList(){
        ArrayList<String> array = new ArrayList<String>();
        array.add(this.name);
        array.add(this.estimatedTime);
        array.add(this.date);
        array.add(this.priority);
        return array;
    }

    public void toggleSelection(){
        if (selected){
            selected = false;
        }
        else{
            selected = true;
        }
    }

    @Override
    public String toString(){
        return name + " : " + estimatedTime + " : " + date + " : " + priority;
    }
}
