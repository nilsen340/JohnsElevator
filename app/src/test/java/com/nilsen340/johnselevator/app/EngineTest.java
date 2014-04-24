package com.nilsen340.johnselevator.app;

import com.nilsen340.johnselevator.app.testutil.SynchronousExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Created by john on 24/04/14.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class EngineTest {

    Engine engine = new Engine(0);
    Engine timedEngine = new Engine(10);
    Engine.EngineListener listener = mock(Engine.EngineListener.class);

    @Before
    public void setUp(){
        engine.setExecutor(new SynchronousExecutorService());
        engine.setListener(listener);

        timedEngine.setListener(listener);
    }

    @Test
    public void goDownOneFloorNotifiesWhenDone(){
        engine.goDownOneFloor();
        verify(listener).wentDownOneFloor();
    }

    @Test
    public void goUpOneFloorNotifiesWhenDone(){
        engine.goUpOneFloor();
        verify(listener).wentUpOneFloor();
    }

    @Test
    public void timedEngineTakesTimeToGoDownOneFloor(){
        timedEngine.goDownOneFloor();
        //tests with time... dangerous
        verify(listener, timeout(5).never()).wentDownOneFloor();
        verify(listener, timeout(20)).wentDownOneFloor();
    }

    @Test
    public void timedEngineTakesTimeToGoUpOneFloor(){
        timedEngine.goUpOneFloor();
        verify(listener, timeout(5).never()).wentUpOneFloor();
        verify(listener, timeout(20)).wentUpOneFloor();
    }


}
