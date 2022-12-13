package com.kirkwoodwest;

import com.bitwig.extension.controller.api.DeviceBank;
import com.bitwig.extension.controller.api.Track;

public class TrackItem {

    private final DeviceBank deviceBank;

    private final Track track;
    Boolean exists = false;
    private int selectedDevicePage = 0;

    public TrackItem(Track track, DeviceBank deviceBank) {
        this.track = track;
        this.deviceBank = deviceBank;
    }

    public DeviceBank getDeviceBank() {
        return deviceBank;
    }

    public Track getTrack() {
        return track;
    }

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public int getSelectedDevicePage() {
        return selectedDevicePage;
    }

    public void setSelectedDevicePage(int selectedDevicePage) {
        this.selectedDevicePage = selectedDevicePage;
    }
}
