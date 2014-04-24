package com.nilsen340.johnselevator.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

public class RobolectricGradleTestRunner extends RobolectricTestRunner {
    public RobolectricGradleTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override protected AndroidManifest getAppManifest(Config config) {
        String manifestProperty = System.getProperty("android.manifest");
        if (config.manifest().equals(Config.DEFAULT) && manifestProperty != null) {
            String resProperty = System.getProperty("android.resources");
            String assetsProperty = System.getProperty("android.assets");
            AndroidManifest manifest = new AndroidManifest(Fs.fileFromPath(manifestProperty), Fs.fileFromPath(resProperty),
                    Fs.fileFromPath(assetsProperty));
            return manifest;
        }
        return super.getAppManifest(config);
    }

    public static void startFragment(Fragment fragment){
        Intent i= new Intent(Robolectric.application, FragmentActivity.class);
        i.putExtra("isTesting", true);
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).withIntent(i)
                .create()
                .start()
                .resume()
                .get();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment,null);
        fragmentTransaction.commit();
    }
}