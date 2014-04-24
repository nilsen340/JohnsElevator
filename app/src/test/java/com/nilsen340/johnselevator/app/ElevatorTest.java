package com.nilsen340.johnselevator.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by john on 23/04/14.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class ElevatorTest {

    Random rand = mock(Random.class);
    Elevator elevator;

    @Before
    public void setUp(){
        when(rand.nextInt()).thenReturn(Elevator.NUM_FLOORS + 4);
        elevator = new Elevator(rand);
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
    public void whenElevatorReceivesFloorRequestBelowCurrentFloorElevatorMovesDown(){
        elevator.requestElevatorToFloor(2);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.DOWN));
    }

    @Test
    public void whenElevatorReceivesFloorRequestAboveCurrentFloorElevatorMovesUp(){
        elevator.requestElevatorToFloor(6);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.UP));
    }

    @Test
    public void whenElevatorRequestedToCurrentFloorMovementStill(){
        elevator.requestElevatorToFloor(4);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }
}
