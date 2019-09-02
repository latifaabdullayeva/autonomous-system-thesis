package com.example.autonomoussystemthesis;

import android.app.Application;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

public class AutonomousSystemApp extends Application {

    public void onCreate() {
        super.onCreate();
        Log.d("FLOW", "AutonomousSystemApp");
        setupBeaconManager();
    }

    private void setupBeaconManager() {
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        );
        // BeaconManager.setDebug(true);

        // enables auto battery saving of about 60%
        new BackgroundPowerSaver(this);
    }
}