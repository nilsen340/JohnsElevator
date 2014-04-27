package com.nilsen340.johnselevator.app;


import android.app.Fragment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Random;

/**
 * Created by john on 27/04/14.
 */
@EFragment(R.layout.fragment_elevator)
public class ElevatorFragment extends Fragment implements Elevator.ElevatorEventListener {

    private static final int FLOOR_TO_FLOOR_TIME = 2000;
    private static final int STOP_TIME = 4000;

    @ViewById(R.id.elevatorFloor) TextView floorNumber;
    @ViewById(R.id.movementIndicator) ImageView movementIndicator;
    @ViewById(R.id.request_button) Button requestFromOutside;
    @ViewById(R.id.floor_spinner) ElevatorSpinner floorSpinner;

    private Elevator elevator = new Elevator(new Random(), new Engine(FLOOR_TO_FLOOR_TIME), STOP_TIME);

    @AfterViews
    void registerAsListenerToElevator(){
        elevator.registerListener(this);
    }

    @AfterViews
    void initializeIndicator(){
        floorNumber.setText(elevator.getCurrentFloor() + "");
        movementIndicator.setImageResource(elevator.getMovementResource(elevator.getMovement()));
    }

    @Click(R.id.request_button)
    void clickedRequestFromOutside(){
        elevator.requestElevatorToFloor(floorSpinner.getSelectedItemPosition());
    }

    public void setElevator(Elevator newElevator) {
        elevator.unregisterListener(this);
        elevator = newElevator;
        elevator.registerListener(this);
    }

    public Elevator getElevator(){
        return elevator;
    }

    @Override
    public void stoppedOnFloor(int floor) {

    }

    @Override
    public void currentFloorChanged(int currentFloor) {
        updateFloorNumber(currentFloor);
    }

    @Override
    public void movementChanged(Elevator.MOVEMENT movement) {
        updateMovement(movement);
    }

    @UiThread
    void updateFloorNumber(int floor){
        floorNumber.setText(floor + "");
    }

    @UiThread
    void updateMovement(Elevator.MOVEMENT movement){
        movementIndicator.setImageResource(elevator.getMovementResource(movement));
    }
}
