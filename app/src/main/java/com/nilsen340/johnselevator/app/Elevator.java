package com.nilsen340.johnselevator.app;

import android.util.TimeUtils;

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

    public static final int NUM_FLOORS = 8;

    private int currentFloor;
    private int wantedFloor;
    private boolean isServing;
    private MOVEMENT movement;
    private Engine engine;
    private List<Integer> queuedRequests = new ArrayList<Integer>();
    private List<Integer> plannedStops = new ArrayList<Integer>();
    private Set<ElevatorEventListener> listeners = new HashSet<ElevatorEventListener>();
    private int stopWaitTimeInMillis;
    private ExecutorService service;

    public Elevator(Random rand, Engine engine, int stopWaitInMillis){
        currentFloor = rand.nextInt() % NUM_FLOORS;
        movement = MOVEMENT.STILL;
        this.engine = engine;
        this.engine.setListener(this);
        this.stopWaitTimeInMillis = stopWaitInMillis;
        service = Executors.newSingleThreadExecutor();
    }

    public void requestElevatorToFloor(int floorNum) {
        if(isServing && isBetweenCurrentAndWanted(floorNum)){
            plannedStops.add(floorNum);
            return;
        }
        if(isServing){
            queuedRequests.add(floorNum);
            return;
        }
        wantedFloor = floorNum;
        executeRequest(floorNum);
    }

    @Override
    public void wentDownOneFloor() {
        currentFloor--;
        if(isCurrentPlannedStop()){
            performPlannedStop();
        }
        if(wantedFloor < currentFloor){
            engine.goDownOneFloor();
        }
    }

    @Override
    public void wentUpOneFloor() {
        currentFloor++;
        if(isCurrentPlannedStop()){
            performPlannedStop();
        }
        if(wantedFloor > currentFloor) {
            engine.goUpOneFloor();
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

    private void executeRequest(int floorNum){
        isServing = true;
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

    private void performPlannedStop(){
        movement = MOVEMENT.STILL;
        notifyListenersStoppedOnFloorEvent(currentFloor);
        startStopWait();
    }

    private void startStopWait(){
        service.submit(new Runnable(){

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
        service.shutdown();
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

    public boolean isCurrentPlannedStop(){
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

    public void setTimerExecutor(ExecutorService service){
        this.service = service;
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
