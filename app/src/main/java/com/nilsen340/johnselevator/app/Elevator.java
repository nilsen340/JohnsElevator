package com.nilsen340.johnselevator.app;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by john on 23/04/14.
 */
public class Elevator implements Engine.EngineListener {

    private static final String TAG = "com.nilsen340.johnselevator.Elevator";

    public static final int OVER_GROUND_FLOORS = 7;
    public static final int BASEMENT_FLOORS = 1;
    public static final int NUM_FLOORS = OVER_GROUND_FLOORS + BASEMENT_FLOORS;

    private int currentFloor;
    private int wantedFloor;
    private boolean isServing;
    private MOVEMENT movement;
    private Engine engine;
    private List<Integer> queuedRequests = new ArrayList<Integer>();
    private List<Integer> plannedStops = new ArrayList<Integer>();
    private List<Integer> insideRequests = new ArrayList<Integer>();
    private Set<ElevatorEventListener> listeners = new HashSet<ElevatorEventListener>();
    private int stopWaitTimeInMillis;
    private ExecutorService service;

    public Elevator(Random rand, Engine engine, int stopWaitInMillis){
        currentFloor = Math.abs(rand.nextInt() % NUM_FLOORS);
        movement = MOVEMENT.STILL;
        this.engine = engine;
        this.engine.setListener(this);
        this.stopWaitTimeInMillis = stopWaitInMillis;
        service = Executors.newSingleThreadExecutor();
    }

    public void requestToFloor(int floor) {
        Log.d(TAG, "request to floor " + floor + " current: " + currentFloor + " movement: " + movement + " isServing: " + isServing);
        if(!isServing){
            wantedFloor = floor;
            executeRequest(floor);
        } else {
            handleRequestWhileServing(floor);
        }
    }

    public void pressButton(int floor) {
        notifyListenersPeopleInElevatorEvent();
        insideRequests.add(floor);
        requestToFloor(floor);
    }

    @Override
    public void wentDownOneFloor() {
        currentFloor--;
        notifyListenersCurrentFloorChanged();
        Log.d(TAG, "engine notified we're on floor: " + currentFloor);
        if(isCurrentPlannedStop()){
            performPlannedStop();
            return;
        }
        if(wantedFloor < currentFloor){
            executeRequest(wantedFloor);
        }
    }

    @Override
    public void wentUpOneFloor() {
        currentFloor++;
        notifyListenersCurrentFloorChanged();
        if(isCurrentPlannedStop()){
            performPlannedStop();
            return;
        }
        if(wantedFloor > currentFloor) {
            executeRequest(wantedFloor);
        }
    }

    private void executeRequest(int floorNum){
        isServing = true;
        if(floorNum < currentFloor) {
            movement = MOVEMENT.DOWN;
            notifyListenersMovementChangedEvent(MOVEMENT.DOWN);
            Log.d(TAG, "notify engine to take us down... ");
            engine.goDownOneFloor();
        }
        if(floorNum > currentFloor) {
            movement = MOVEMENT.UP;
            notifyListenersMovementChangedEvent(MOVEMENT.UP);
            engine.goUpOneFloor();
        }
    }

    private void handleRequestWhileServing(int floorNum) {
        if(isBetweenCurrentAndWanted(floorNum)){
            Log.d(TAG, "adding floor " + floorNum + " to planned stops");
            plannedStops.add(floorNum);
            return;
        }

        if(isFurtherAlongTheJourney(floorNum)){
            plannedStops.add(wantedFloor);
            wantedFloor = floorNum;
            return;
        }
        queuedRequests.add(floorNum);
    }

    private void performPlannedStop(){
        movement = MOVEMENT.STILL;
        notifyListenersMovementChangedEvent(MOVEMENT.STILL);
        handleInsideRequests();
        notifyListenersStoppedOnFloorEvent();
        startStopWait();
    }

    private void startStopWait(){
        service.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(stopWaitTimeInMillis);
                    notifyStopTimeUp();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void notifyStopTimeUp(){
        if(currentFloor == wantedFloor){
            handleWantedFloorReached();
        } else {
            executeRequest(wantedFloor);
        }
    }

    private void handleWantedFloorReached(){
        plannedStops = new ArrayList<Integer>();
        if(!queuedRequests.isEmpty()){
            wantedFloor = queuedRequests.get(0);
            queuedRequests.remove(0);
            executeRequest(wantedFloor);
        } else {
            isServing = false;
        }
    }

    private void handleInsideRequests() {
        for(Integer insideRequest : new ArrayList<Integer>(insideRequests)){
            if(insideRequest == currentFloor){
                insideRequests.remove(insideRequests.indexOf(insideRequest));
            }
        }
        if(insideRequests.size() == 0){
            notifyListenersElevatorEmptyEvent();
        }
    }

    private boolean isBetweenCurrentAndWanted(int floor){
        if(!isServing){
            return false;
        }

        if(wantedFloor > currentFloor){
            return floor < wantedFloor && floor > currentFloor;
        }

        return floor > wantedFloor && floor < currentFloor;
    }

    private boolean isCurrentPlannedStop(){
        if(currentFloor == wantedFloor){
            return true;
        }
        for(Integer plannedStop : plannedStops){
            if(plannedStop == currentFloor){
                return true;
            }
        }
        return false;
    }


    private boolean isFurtherAlongTheJourney(int floorNum) {
        return (movement == MOVEMENT.UP && wantedFloor < floorNum)
                || (movement == MOVEMENT.DOWN && wantedFloor > floorNum);
    }

    private void notifyListenersStoppedOnFloorEvent(){
        Log.d(TAG, "notifying stop to " + listeners.size() + " listeners");
        for(ElevatorEventListener listener : listeners){
            listener.stoppedOnFloor(currentFloor);
        }
    }

    private void notifyListenersCurrentFloorChanged(){
        for(ElevatorEventListener listener : listeners){
            listener.currentFloorChanged(currentFloor - BASEMENT_FLOORS);
        }
    }

    private void notifyListenersMovementChangedEvent(MOVEMENT movement){
        for(ElevatorEventListener listener : listeners){
            listener.movementChanged(movement);
        }
    }

    private void notifyListenersPeopleInElevatorEvent(){
        for(ElevatorEventListener listener : listeners){
            listener.peopleInElevator();
        }
    }

    private void notifyListenersElevatorEmptyEvent(){
        for(ElevatorEventListener listener : listeners){
            listener.elevatorEmpty();
        }
    }

    /**
     * gets current floor
     * @return current floor
     */
    public int getCurrentFloor() {
        return currentFloor - BASEMENT_FLOORS;
    }

    /**
     * gets current floor when floor count starts on 0
     * @return absolute current floor
     */
    public int getAbsoluteCurrentFloor(){
        return currentFloor;
    }

    public int getWantedFloor(){
        return wantedFloor;
    }

    public void setWantedFloor(int wantedFloor){
        this.wantedFloor = wantedFloor;
    }

    public void setTimerExecutor(ExecutorService service){
        this.service = service;
    }

    public MOVEMENT getMovement() {
        return movement;
    }

    public int getMovementResource(MOVEMENT movement){
        switch(movement){
            case DOWN: return R.drawable.ic_elevator_down;
            case UP: return R.drawable.ic_elevator_up;
            default: return R.drawable.ic_elevator_still;
        }
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

    public int getListenerCount() {
        return listeners.size();
    }

    public static List<Integer> getAvailableFloors() {
        List<Integer> floors = new ArrayList<Integer>();
        for(int i = 0; i < NUM_FLOORS; i++){
            floors.add(i -  BASEMENT_FLOORS);
        }
        return floors;
    }

    public List<Integer> getInsideRequests() {
        return insideRequests;
    }

    public interface ElevatorEventListener{
        void stoppedOnFloor(int floor);
        void currentFloorChanged(int currentFloor);
        void movementChanged(MOVEMENT movement);
        void peopleInElevator();
        void elevatorEmpty();
    }

    public enum MOVEMENT {
        STILL,
        DOWN,
        UP
    }
}