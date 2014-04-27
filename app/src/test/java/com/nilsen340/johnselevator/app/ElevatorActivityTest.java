package com.nilsen340.johnselevator.app;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by john on 23/04/14.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class ElevatorActivityTest {

    ElevatorActivity activity;

    @Before
    public void setUp(){
        Intent i= new Intent(Robolectric.application, ElevatorActivity_.class);
        i.putExtra("isTesting", true);
        activity = Robolectric.buildActivity(ElevatorActivity_.class).withIntent(i)
                .create().start().visible().get();
    }

    @Test
    public void fragmentGetsLoaded(){
        assertThat(activity.elevatorFragment.isVisible(), is(true));
    }

}
