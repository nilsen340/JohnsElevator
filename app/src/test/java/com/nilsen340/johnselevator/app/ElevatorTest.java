package com.nilsen340.johnselevator.app;

import com.nilsen340.johnselevator.app.testutil.SynchronousExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.util.List;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
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
    Elevator.ElevatorEventListener listener = mock(Elevator.ElevatorEventListener.class);

    //SUT
    Elevator elevator;
    Elevator synchronizedElevator;

    @Before
    public void setUp(){
        synchronizedEngine.setExecutor(new SynchronousExecutorService());
        when(rand.nextInt()).thenReturn(Elevator.NUM_FLOORS + START_FLOOR);
        elevator = new Elevator(rand, engine, 0);
        elevator.registerListener(listener);
        elevator.setTimerExecutor(new SynchronousExecutorService());

        synchronizedElevator = new Elevator(rand, synchronizedEngine, 0);
        synchronizedElevator.registerListener(listener);
        synchronizedElevator.setTimerExecutor(new SynchronousExecutorService());
    }

    @Test
    public void elevatorStartsOnSpecificFloor(){
        assertThat(elevator.getAbsoluteCurrentFloor(), is(4));
    }

    @Test
    public void elevatorCurrentFloorIsOneLessThenAbsolute(){
        assertThat(elevator.getAbsoluteCurrentFloor(), is(4));
        assertThat(elevator.getCurrentFloor(), is(3));
    }

    @Test
    public void elevatorMovementStartsOnStill(){
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void whenElevatorReceivesFloorRequestBelowCurrentFloorElevatorSignalDown(){
        elevator.requestToFloor(START_FLOOR - 2);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.DOWN));
    }

    @Test
    public void whenElevatorReceivesFloorRequestAboveCurrentFloorElevatorSignalUp(){
        elevator.requestToFloor(START_FLOOR + 2);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.UP));
    }

    @Test
    public void whenElevatorReceivesFloorRequestOneBelowCurrentFloorChanges(){
        synchronizedElevator.requestToFloor(START_FLOOR - 1);
        assertThat(synchronizedElevator.getAbsoluteCurrentFloor(), is(START_FLOOR -1));
    }

    @Test
    public void whenElevatorReceivesFloorRequestTwoBelowFloorChanges(){
        synchronizedElevator.requestToFloor(START_FLOOR - 2);
        assertThat(synchronizedElevator.getAbsoluteCurrentFloor(), is(START_FLOOR - 2));
    }

    @Test
    public void whenElevatorReceivesFloorRequestTwoAboveFloorChanges(){
        synchronizedElevator.requestToFloor(START_FLOOR + 2);
        assertThat(synchronizedElevator.getAbsoluteCurrentFloor(), is(START_FLOOR + 2));
    }

    @Test
    public void movementStillWhenElevatorReachesWantedFloorGoingDown(){
        synchronizedElevator.requestToFloor(START_FLOOR - 1);
        assertThat(synchronizedElevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void movementStillWhenElevatorReachesWantedFloorGoingUp(){
        synchronizedElevator.requestToFloor(START_FLOOR + 1);
        assertThat(synchronizedElevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void whenElevatorRequestedToCurrentFloorMovementStill(){
        elevator.requestToFloor(START_FLOOR);
        assertThat(elevator.getMovement(), is(Elevator.MOVEMENT.STILL));
    }

    @Test
    public void engineWentDownEventChangesCurrentFloor(){
        synchronizedElevator.setWantedFloor(START_FLOOR - 1);
        synchronizedEngine.goDownOneFloor();
        assertThat(synchronizedElevator.getAbsoluteCurrentFloor(), is(START_FLOOR - 1));
    }

    @Test
    public void engineWentUpEventChangesCurrentFloor(){
        synchronizedElevator.setWantedFloor(START_FLOOR - 1);
        synchronizedEngine.goUpOneFloor();
        assertThat(synchronizedElevator.getAbsoluteCurrentFloor(), is(START_FLOOR + 1));
    }

    @Test
    public void elevatorEventListenerGetsNotifiedWhenElevatorStops(){
        synchronizedElevator.requestToFloor(START_FLOOR - 2);
        verify(listener).stoppedOnFloor(START_FLOOR - 2);
    }

    @Test
    public void elevatorEventListenersGetNotifiedWhenMoreThenOneListenerRegistered(){
        Elevator.ElevatorEventListener listener1 = mock(Elevator.ElevatorEventListener.class);
        Elevator.ElevatorEventListener listener2 = mock(Elevator.ElevatorEventListener.class);
        synchronizedElevator.registerListener(listener1);
        synchronizedElevator.registerListener(listener2);

        synchronizedElevator.requestToFloor(START_FLOOR - 2);

        verify(listener1).stoppedOnFloor(START_FLOOR - 2);
        verify(listener2).stoppedOnFloor(START_FLOOR - 2);
    }

    @Test
    public void elevatorEventListenerGetsNotifiedOnceWhenElevatorStops(){
        synchronizedElevator.requestToFloor(START_FLOOR - 2);
        verify(listener).stoppedOnFloor(START_FLOOR - 2);
    }

    @Test
    public void unregisteredElevatorEventListenerDoesNotGetNotified(){
        synchronizedElevator.unregisterListener(listener);
        synchronizedElevator.requestToFloor(START_FLOOR - 2);
        verify(listener, never()).stoppedOnFloor(START_FLOOR - 2);
    }

    @Test
    public void wantedFloorDoesNotChangeWhenIncompatibleRequestReceived() {
        elevator.requestToFloor(START_FLOOR - 3);
        elevator.requestToFloor(START_FLOOR + 2);
        assertThat(elevator.getWantedFloor(), is(START_FLOOR - 3));
    }

    @Test
    public void wantedFloorChangesWhenARequestFurtherDownInMovementDirectionReceived(){
        elevator.requestToFloor(START_FLOOR - 1);
        elevator.requestToFloor(START_FLOOR - 3);
        assertThat(elevator.getWantedFloor(), is(START_FLOOR - 3));
    }

    @Test
    public void wantedFloorChangesWhenARequestFurtherUpInMovementDirectionReceived(){
        elevator.requestToFloor(START_FLOOR + 1);
        elevator.requestToFloor(START_FLOOR + 3);
        assertThat(elevator.getWantedFloor(), is(START_FLOOR + 3));
    }

    @Test
    public void whenWantedFloorChangedDownPreviousWantedIsAStop(){
        elevator.requestToFloor(START_FLOOR - 1);
        elevator.requestToFloor(START_FLOOR - 3);
        elevator.wentDownOneFloor();
        verify(listener).stoppedOnFloor(START_FLOOR - 1);
    }

    @Test
    public void whenWantedFloorChangedUpPreviousWantedIsAStop(){
        elevator.requestToFloor(START_FLOOR + 1);
        elevator.requestToFloor(START_FLOOR + 3);
        elevator.wentUpOneFloor();
        verify(listener).stoppedOnFloor(START_FLOOR + 1);
    }

    @Test
    public void requestsReceivedWhileServingGetQueued(){
        elevator.requestToFloor(START_FLOOR - 3);
        elevator.requestToFloor(START_FLOOR + 1);
        elevator.requestToFloor(START_FLOOR + 2);
        assertThat(elevator.getQueuedRequests().get(0), is(START_FLOOR + 1));
        assertThat(elevator.getQueuedRequests().get(1), is(START_FLOOR + 2));
    }

    @Test
    public void requestsGetsServedInOrder(){
        elevator.requestToFloor(START_FLOOR - 2);
        elevator.requestToFloor(START_FLOOR + 1);

        //elevator goes down to START_FLOOR -2
        elevator.wentDownOneFloor();
        elevator.wentDownOneFloor();
        verify(engine, times(2)).goDownOneFloor();
        verify(listener).stoppedOnFloor(START_FLOOR -2);

        //elevator goes up to START_FLOOR +1
        elevator.wentUpOneFloor();
        elevator.wentUpOneFloor();
        elevator.wentUpOneFloor();
        verify(engine, times(3)).goUpOneFloor();
        verify(listener).stoppedOnFloor(START_FLOOR + 1);
    }

    @Test
    public void ifRequestIsWithinCurrentJourneyThenStopByWhenGoingDown(){
        elevator.requestToFloor(START_FLOOR - 2);
        elevator.requestToFloor(START_FLOOR - 1);
        elevator.wentDownOneFloor();
        verify(listener, times(1)).stoppedOnFloor(START_FLOOR - 1);
        elevator.wentDownOneFloor();
        verify(listener, times(1)).stoppedOnFloor(START_FLOOR - 2);
    }

    @Test
    public void ifRequestIsWithinCurrentJourneyThenStopByWhenGoingUp(){
        elevator.requestToFloor(START_FLOOR + 2);
        elevator.requestToFloor(START_FLOOR + 1);
        elevator.wentUpOneFloor();
        verify(listener, times(1)).stoppedOnFloor(START_FLOOR + 1);
        elevator.wentUpOneFloor();
        verify(listener, times(1)).stoppedOnFloor(START_FLOOR + 2);
    }

    @Test
    public void plannedStopListClearedAfterReachingWantedFloor(){
        elevator.requestToFloor(START_FLOOR - 2);
        elevator.requestToFloor(START_FLOOR - 1);
        elevator.wentDownOneFloor();
        elevator.wentDownOneFloor();
        elevator.requestToFloor(START_FLOOR);
        elevator.wentUpOneFloor();
        elevator.wentUpOneFloor();
        //only stops on way down
        verify(listener, times(1)).stoppedOnFloor(START_FLOOR - 1);
    }

    @Test
    public void stopTimerStopsElevatorForSomeTimeWhenDestintationReached(){
        Elevator elevator = new Elevator(rand, engine, 10);
        elevator.requestToFloor(START_FLOOR - 1);
        elevator.requestToFloor(START_FLOOR + 2);
        elevator.wentDownOneFloor();
        verify(engine, never()).goUpOneFloor();
        verify(engine, timeout(20)).goUpOneFloor();
    }

    @Test
    public void elevatorEventListenerNotifiedWhenElevatorMovesDown(){
        synchronizedElevator.requestToFloor(START_FLOOR - 2);
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, atLeastOnce()).movementChanged(Elevator.MOVEMENT.DOWN);
        inOrder.verify(listener).movementChanged(Elevator.MOVEMENT.STILL);
        inOrder.verify(listener).stoppedOnFloor(START_FLOOR - 2);
    }

    @Test
    public void elevatorEventListenerNotifiedWhenElevatorMovesUp(){
        synchronizedElevator.requestToFloor(START_FLOOR + 2);
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, atLeastOnce()).movementChanged(Elevator.MOVEMENT.UP);
        inOrder.verify(listener).movementChanged(Elevator.MOVEMENT.STILL);
        inOrder.verify(listener).stoppedOnFloor(START_FLOOR + 2);
    }

    @Test
    public void elevatorReturnsCorrectResourceForStillMovement(){
        assertThat(elevator.getMovementResource(Elevator.MOVEMENT.STILL), is(R.drawable.ic_elevator_still));
    }

    @Test
    public void elevatorReturnsCorrectResourceForDownMovement(){
        assertThat(elevator.getMovementResource(Elevator.MOVEMENT.DOWN), is(R.drawable.ic_elevator_down));
    }

    @Test
    public void elevatorReturnsCorrectResourceForUpMovement(){
        assertThat(elevator.getMovementResource(Elevator.MOVEMENT.UP), is(R.drawable.ic_elevator_up));
    }

    @Test
    public void registeredListenersCountIsCorrectWhenOneRegistered(){
        assertThat(elevator.getListenerCount(), is(1));
    }

    @Test
    public void registeredListenersCountIsCorrectWhenTwoRegistered(){
        elevator.registerListener(mock(Elevator.ElevatorEventListener.class));
        assertThat(elevator.getListenerCount(), is(2));
    }

    @Test
    public void wentDownOneFloorTriggersFloorChangeNotification(){
        elevator.wentDownOneFloor();
        //event sends absolute floor - 1
        verify(listener).currentFloorChanged(START_FLOOR - 2);
    }

    @Test
    public void wentUpOneFloorTriggersFloorChangeNotification(){
        elevator.wentUpOneFloor();
        //event sends absolute floor - 1
        verify(listener).currentFloorChanged(START_FLOOR);
    }

    @Test
    public void elevatorReturnsCorrectListOfFloors(){
        List<Integer> floors = Elevator.getAvailableFloors();
        assertThat(floors.get(0), is(0 - Elevator.BASEMENT_FLOORS));
        for(int i = 1; i < Elevator.NUM_FLOORS; i++){
            assertThat(floors.get(i), is(i - Elevator.BASEMENT_FLOORS));
        }
    }

    @Test
    public void pressButtonMakesElevatorExecuteRequestWhenIdle(){
        elevator.pressButton(START_FLOOR - 1);
        verify(engine).goDownOneFloor();
    }

    @Test
    public void pressButtonMakesWantedFloorUpdateWhenIdle(){
        elevator.pressButton(START_FLOOR - 1);
        assertThat(elevator.getWantedFloor(), is(START_FLOOR - 1));
    }

    @Test
    public void pressButtonWhileServingDoesNotChangeWantedFloorForIncompatibleJourney(){
        elevator.requestToFloor(START_FLOOR + 2);
        elevator.pressButton(START_FLOOR - 1);
        assertThat(elevator.getWantedFloor(), is(START_FLOOR + 2));
    }

    @Test
    public void ifButtonPressedElevatorPeopleInElevatorNotificationSent(){
        elevator.pressButton(START_FLOOR - 1);
        verify(listener).peopleInElevator();
    }

    @Test
    public void ifButtonPressedPeopleListAddsNewElement(){
        elevator.pressButton(START_FLOOR - 1);
        assertThat(elevator.getInsideRequests().get(0), is(START_FLOOR - 1));
    }

    @Test
    public void whenInsideRequestFulFilledElevatorInsideRequestsEmpty(){
        elevator.pressButton(START_FLOOR - 1);
        elevator.wentDownOneFloor();
        assertThat(elevator.getInsideRequests().size(), is(0));
    }

    @Test
    public void whenInsideRequestsFulfilledListenersNotified(){
        elevator.pressButton(START_FLOOR - 1);
        elevator.wentDownOneFloor();
        verify(listener).elevatorEmpty();
    }



//    @Test
//    public void requestFromInsideGetsServedFirstWhenWantedFloorReached(){
//        elevator.requestToFloor(START_FLOOR - 1);
//        elevator.requestToFloor(START_FLOOR - 2);
//        elevator.pressButton(START_FLOOR + 1);
//
//        elevator.wentDownOneFloor();
//        elevator.wentUpOneFloor();
//        elevator.wentUpOneFloor();
//        elevator.wentDownOneFloor();
//        elevator.wentDownOneFloor();
//        elevator.wentDownOneFloor();
//
//        InOrder inOrder = inOrder(listener, engine);
//        inOrder.verify(engine).goDownOneFloor();
//        inOrder.verify(listener).stoppedOnFloor(START_FLOOR - 1);
//        inOrder.verify(engine, times(2)).goUpOneFloor();
//        inOrder.verify(listener).stoppedOnFloor(START_FLOOR + 1);
//        inOrder.verify(engine, times(3)).goDownOneFloor();
//        inOrder.verify(listener).stoppedOnFloor(START_FLOOR - 2);
//    }
}
