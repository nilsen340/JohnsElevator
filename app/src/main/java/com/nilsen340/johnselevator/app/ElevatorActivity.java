package com.nilsen340.johnselevator.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class ElevatorActivity extends Activity {

    ElevatorFragment elevatorFragment = new ElevatorFragment_();
    FragmentManager fragmentManager = this.getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, elevatorFragment)
                    .commit();
        }
    }

}
