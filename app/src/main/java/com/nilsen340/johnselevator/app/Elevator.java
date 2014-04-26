package com.nilsen340.johnselevator.app;

import java.util.Random;

/**
 * Created by john on 23/04/14.
 */
public class Elevator implements Engine.EngineListener {

    public static final int NUM_FLOORS = 8;

    private int currentFloor;
    private int wantedFloor;
    private MOVEMENT movement;
    private Engine engine;

    public Elevator(Random rand, Engine engine){
        currentFloor = rand.nextInt() % NUM_FLOORS;
        movement = MOVEMENT.STILL;
        this.engine = engine;
        this.engine.setListener(this);
    }

    public int getCurrentFloor(){
        return currentFloor;
    }

    public int getWantedFloor(){
        return wantedFloor;
    }

    public void setWantedFloor(int wantedFloor){
        this.wantedFloor = wantedFloor;
    }

    public MOVEMENT getMovement() {
        return movement;
    }

    public void requestElevatorToFloor(int floorNum) {
        if(movement != MOVEMENT.STILL){
            return;
        }
        wantedFloor = floorNum;
        if(floorNum < currentFloor) {
            movement = MOVEMENT.DOWN;
            engine.goDownOneFloor();
            return;
        }
        if(floorNum > currentFloor) {
            movement = MOVEMENT.UP;
            engine.goUpOneFloor();
        }
    }

    @Override
    public void wentDownOneFloor() {
        currentFloor--;
        if(wantedFloor < currentFloor){
            engine.goDownOneFloor();
        } else {
            wantedFloorReached();
        }
    }

    @Override
    public void wentUpOneFloor() {
        currentFloor++;
        if(wantedFloor > currentFloor){
            engine.goUpOneFloor();
        } else {
            wantedFloorReached();
        }
    }

    private void wantedFloorReached(){
        movement = MOVEMENT.STILL;
    }

    public enum MOVEMENT {
        STILL,
        DOWN,
        UP
    }
}
