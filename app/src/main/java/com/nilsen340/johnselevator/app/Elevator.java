package com.nilsen340.johnselevator.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by john on 23/04/14.
 */
public class Elevator implements Engine.EngineListener {

    public static final int NUM_FLOORS = 8;

    private int currentFloor;
    private int wantedFloor;
    private MOVEMENT movement;
    private Engine engine;
    private List<Integer> queuedRequests = new ArrayList<Integer>();
    private List<Integer> plannedStops = new ArrayList<Integer>();
    private Set<ElevatorEventListener> listeners = new HashSet<ElevatorEventListener>();

    public Elevator(Random rand, Engine engine){
        currentFloor = rand.nextInt() % NUM_FLOORS;
        movement = MOVEMENT.STILL;
        this.engine = engine;
        this.engine.setListener(this);
    }

    public void requestElevatorToFloor(int floorNum) {
        if(movement != MOVEMENT.STILL && isBetweenCurrentAndWanted(floorNum)){
            plannedStops.add(floorNum);
            return;
        }
        if(movement != MOVEMENT.STILL){
            queuedRequests.add(floorNum);
            return;
        }
        wantedFloor = floorNum;
        executeRequest(floorNum);
    }

    private boolean isBetweenCurrentAndWanted(int floor){
        if(movement == MOVEMENT.UP){
            return floor < wantedFloor && floor > currentFloor;
        }
        if(movement == MOVEMENT.DOWN){
            return floor > wantedFloor && floor < currentFloor;
        }
        return false;
    }

    private void executeRequest(int floorNum){

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
        if(isCurrentPlannedStop()){
            performPlannedStop();
            executeRequest(wantedFloor);
        }
        if(wantedFloor < currentFloor){
            engine.goDownOneFloor();
        } else {
            wantedFloorReached();
        }
    }

    @Override
    public void wentUpOneFloor() {
        currentFloor++;
        if(isCurrentPlannedStop()){
            performPlannedStop();
            executeRequest(wantedFloor);
        }
        if(wantedFloor > currentFloor){
            engine.goUpOneFloor();
        } else {
            wantedFloorReached();
        }
    }

    private void wantedFloorReached(){
        performPlannedStop();
        plannedStops = new ArrayList<Integer>();
        if(!queuedRequests.isEmpty()){
            wantedFloor = queuedRequests.get(0);
            queuedRequests.remove(0);
            executeRequest(wantedFloor);
        }
    }

    private void performPlannedStop(){
        movement = MOVEMENT.STILL;
        notifyListenersStoppedOnFloorEvent(currentFloor);
    }

    public boolean isCurrentPlannedStop(){
        for(Integer plannedStop : plannedStops){
            if(plannedStop == currentFloor){
                return true;
            }
        }
        return false;
    }

    private void notifyListenersStoppedOnFloorEvent(int floor){
        for(ElevatorEventListener listener : listeners){
            listener.stoppedOnFloor(floor);
        }
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

    public List<Integer> getQueuedRequests() {
        return queuedRequests;
    }

    public void registerListener(ElevatorEventListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(ElevatorEventListener listener) {
        listeners.remove(listener);
    }

    public interface ElevatorEventListener{
        void stoppedOnFloor(int floor);
    }

    public enum MOVEMENT {
        STILL,
        DOWN,
        UP
    }
}
