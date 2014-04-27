package com.nilsen340.johnselevator.app;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by john on 27/04/14.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class ElevatorFragmentTest {

    ElevatorFragment fragment = new ElevatorFragment_();
    Elevator elevator = mock(Elevator.class);

    @Before
    public void setUp(){
        when(elevator.getCurrentFloor()).thenReturn(4);
        when(elevator.getMovement()).thenReturn(Elevator.MOVEMENT.STILL);
        fragment.setElevator(elevator);
        RobolectricGradleTestRunner.startFragment(fragment);
    }

    @Test
    public void fragmentInitializesElevator(){
        ElevatorFragment fragment = new ElevatorFragment_();
        RobolectricGradleTestRunner.startFragment(fragment);
        assertNotNull(fragment.getElevator());
    }

    @Test
    public void fragmentRegistersAsListenerToConstructedElevator(){
        ElevatorFragment fragment = new ElevatorFragment_();
        Elevator elevator = fragment.getElevator();
        assertThat(elevator.getListenerCount(), is(0));
        RobolectricGradleTestRunner.startFragment(fragment);
        assertThat(elevator.getListenerCount(), is(1));
    }

    @Test
    public void fragmentRegistersAsListenerToElevatorInSetter(){
        Elevator elevator2 = mock(Elevator.class);
        fragment.setElevator(elevator2);
        verify(elevator2).registerListener(fragment);
    }

    @Test
    public void fragmentUnregistersAsListenerToOldElevatorWhenSettingNew(){
        Elevator elevator2 = mock(Elevator.class);
        fragment.setElevator(elevator2);
        verify(elevator).unregisterListener(fragment);
    }

    @Test
    public void elevatorIndicatorElementsVisible(){
        assertThat(fragment.floorNumber.getVisibility(), is(View.VISIBLE));
        assertThat(fragment.movementIndicator.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void floorIndicatorSetToCorrectNumber(){
        assertThat(fragment.floorNumber.getText().toString(), is("4"));
    }

    @Test
    public void elevatorGetsQueriedOnMovementOnStartUp(){
        verify(elevator).getMovementResource(Elevator.MOVEMENT.STILL);
    }

    @Test
    public void requestFromOutsideButtonSendsRequestToElevatorWhenClicked(){
        fragment.floorSpinner.setSelection(1);
        fragment.requestFromOutside.performClick();
        verify(elevator).requestElevatorToFloor(1);
    }

    @Test
    public void currentFloorChangedEventUpdatesFloorNumber(){
        fragment.currentFloorChanged(1);
        assertThat(fragment.floorNumber.getText().toString(), is("1"));
    }

    @Test
    public void movementChangedEventUpdatesDrawable(){
        when(elevator.getMovementResource(Elevator.MOVEMENT.DOWN)).thenReturn(R.drawable.ic_elevator_down);
        fragment.movementChanged(Elevator.MOVEMENT.DOWN);
        verify(elevator).getMovementResource(Elevator.MOVEMENT.DOWN);
    }
}
