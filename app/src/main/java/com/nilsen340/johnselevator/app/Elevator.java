package com.nilsen340.johnselevator.app;

import java.util.Random;

/**
 * Created by john on 23/04/14.
 */
public class Elevator {

    public static final int NUM_FLOORS = 8;

    private int currentFloor;
    private MOVEMENT movement;

    public Elevator(Random rand){
        currentFloor = rand.nextInt() % NUM_FLOORS;
        movement = MOVEMENT.STILL;
    }

    public int getCurrentFloor(){
        return currentFloor;
    }

    public MOVEMENT getMovement() {
        return movement;
    }

    public void requestElevatorToFloor(int floorNum) {
        if(floorNum < currentFloor) {
            movement = MOVEMENT.DOWN;
            return;
        }
        if(floorNum > currentFloor) {
            movement = MOVEMENT.UP;
        }
    }

    public enum MOVEMENT {
        STILL,
        DOWN,
        UP
    }
}
