package com.nilsen340.johnselevator.app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 * Created by john on 27/04/14.
 */
public class ElevatorSpinner extends Spinner {


    public ElevatorSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_item);
        adapter.addAll(Elevator.getAvailableFloors());
        this.setAdapter(adapter);
    }
}
