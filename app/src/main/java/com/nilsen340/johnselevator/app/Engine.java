package com.nilsen340.johnselevator.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by john on 24/04/14.
 */
public class Engine {

    private EngineListener listener;

    private int timeBetweenFloors;
    private ExecutorService service;

    public Engine(int timeBetweenFloors){
        this.timeBetweenFloors = timeBetweenFloors;
        service = Executors.newSingleThreadExecutor();
    }

    public void setListener(EngineListener listener) {
        this.listener = listener;
    }

    public void goDownOneFloor() {
        service.submit(new EngineWork(true));
    }

    public void goUpOneFloor(){
        service.submit(new EngineWork(false));
    }

    public void setExecutor(ExecutorService executor) {
        this.service = executor;
    }

    public interface EngineListener {
        void wentDownOneFloor();
        void wentUpOneFloor();
    }

    private class EngineWork implements Runnable {

        private boolean isGoingDown;

        private EngineWork(boolean isGoingDown){
            this.isGoingDown = isGoingDown;
        }

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep(timeBetweenFloors);
                if(isGoingDown) {
                    listener.wentDownOneFloor();
                } else {
                    listener.wentUpOneFloor();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
