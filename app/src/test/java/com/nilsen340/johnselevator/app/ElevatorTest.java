package com.nilsen340.johnselevator.app;

import com.nilsen340.johnselevator.app.testutil.SynchronousExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by john on 23/04/14.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class ElevatorTest {

    private static final int START_FLOOR = 4;
    Random rand = mock(Random.class);
    Engine engine = mock(Engine.class);
    Engine synchronizedEngine = new Engine(0);

    //SUT
    Elevator elevator;
    Elevator synchronizedElevator;

    @Before
    public void setUp(){
        synchronizedEngine.setExecutor(new SynchronousExecutorService());
        when(rand.nextInt()).thenReturn(Elevator.NUM_FLOORS + START_FLOOR);
        elevator = new Elevator(rand, engine);
        synchronizedElevator = new Elevator(rand, synchronizedEngine);
    }

    @Test
    public void elevatorStartsOnSpecificFloor(){
        assertThat(elevator.getCurrentFloor(), is(4));
    }

    @Test
    public void elevatorMovementStartsOnStill(){
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void whenElevatorReceivesFloorRequestBelowCurrentFloorElevatorSignalDown(){
        elevator.requestElevatorToFloor(START_FLOOR - 2);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.DOWN));
    }

    @Test
    public void whenElevatorReceivesFloorRequestAboveCurrentFloorElevatorSignalUp(){
        elevator.requestElevatorToFloor(START_FLOOR + 2);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.UP));
    }

    @Test
    public void whenElevatorReceivesFloorRequestOneBelowCurrentFloorChanges(){
        synchronizedElevator.requestElevatorToFloor(START_FLOOR - 1);
        assertThat(synchronizedElevator.getCurrentFloor(), is(START_FLOOR -1));
    }

    @Test
    public void whenElevatorReceivesFloorRequestTwoBelowFloorChanges(){
        synchronizedElevator.requestElevatorToFloor(START_FLOOR - 2);
        assertThat(synchronizedElevator.getCurrentFloor(), is(START_FLOOR - 2));
    }

    @Test
    public void whenElevatorReceivesFloorRequestTwoAboveFloorChanges(){
        synchronizedElevator.requestElevatorToFloor(START_FLOOR + 2);
        assertThat(synchronizedElevator.getCurrentFloor(), is(START_FLOOR + 2));
    }

    @Test
    public void movementStillWhenElevatorReachesWantedFloorGoingDown(){
        synchronizedElevator.requestElevatorToFloor(START_FLOOR - 1);
        assertThat(synchronizedElevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void movementStillWhenElevatorReachesWantedFloorGoingUp(){
        synchronizedElevator.requestElevatorToFloor(START_FLOOR + 1);
        assertThat(synchronizedElevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void whenElevatorRequestedToCurrentFloorMovementStill(){
        elevator.requestElevatorToFloor(START_FLOOR);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void engineWentDownEventChangesCurrentFloor(){
        synchronizedElevator.setWantedFloor(START_FLOOR - 1);
        synchronizedEngine.goDownOneFloor();
        assertThat(synchronizedElevator.getCurrentFloor(), is(START_FLOOR - 1));
    }

    @Test
    public void engineWentUpEventChangesCurrentFloor(){
        synchronizedElevator.setWantedFloor(START_FLOOR - 1);
        synchronizedEngine.goUpOneFloor();
        assertThat(synchronizedElevator.getCurrentFloor(), is(START_FLOOR + 1));
    }

    @Test
    public void wantedFloorDoesNotChangeWhenIncompatibleRequestReceived() {
        elevator.requestElevatorToFloor(START_FLOOR - 3);
        elevator.requestElevatorToFloor(START_FLOOR + 2);
        assertThat(elevator.getWantedFloor(), is(START_FLOOR - 3));
    }
}
