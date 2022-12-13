package com.kirkwoodwest;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PluginWindowControlExtension extends ControllerExtension {


    private static final int NUM_DEVICES = 24;
    int NUM_TRACKS = 4;
    int NUM_SENDS = 1;
    private Signal settingOpenWindows;
    private Signal settingCloseWindows;
    private ArrayList<Device> cursorTrackDeviceList = new ArrayList<>();
    private ArrayList<Device> bankDeviceList = new ArrayList<>();
    private Signal settingAllOpenWindows;
    private Signal settingAllCloseWindows;
//    private int NUM_DEVICES = 4;
    private int NUM_LAYERS = 4;
    private DeviceBank deviceBank;

    private ArrayList<TrackItem> trackItems = new ArrayList<>();

    private int pages = 0;
    private int trackBankPage = 0;

    protected PluginWindowControlExtension(final PluginWindowControlExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();
        host.showPopupNotification("Plugin Window Control Initialized");

        TrackBank trackBank = host.createTrackBank(128, 0, 0, true);
        trackBank.itemCount().addValueObserver(itemCount -> {
            double temp = (double) itemCount / (double) 8;
            this.pages = (int) Math.ceil(temp);
        });

        trackBank.scrollPosition().addValueObserver(scrollPosition -> {
            this.trackBankPage = scrollPosition / 8;
        });

        trackBank.canScrollForwards().markInterested();

        for (int i = 0; i < 128; i++) {

            Track track = trackBank.getItemAt(i);
            track.exists().markInterested();
            track.name().markInterested();

            DeviceBank deviceBank = track.createDeviceBank(NUM_DEVICES);
            this.observeDeviceBank(deviceBank);

            TrackItem trackItem = new TrackItem(track, deviceBank);
            trackItems.add(trackItem);

            track.exists().addValueObserver(trackItem::setExists);
        }

        CursorTrack cursorTrack = host.createCursorTrack("Cursor Track", "Cursor Track", 0, 0, true);
        DeviceBank cursorTrackDeviceBank = cursorTrack.createDeviceBank(NUM_DEVICES);
        this.observeDeviceBank(cursorTrackDeviceBank);
        TrackItem cursorTrackItem = new TrackItem(cursorTrack, cursorTrackDeviceBank);

        settingAllOpenWindows = host.getDocumentState().getSignalSetting("Open", "All Channels", "Open All Plugins ");
        settingAllCloseWindows = host.getDocumentState().getSignalSetting("Close", "All Channels", "Close All Plugins");
        settingAllOpenWindows.addSignalObserver(() -> openAllChannelsPlugins(true));
        settingAllCloseWindows.addSignalObserver(() -> openAllChannelsPlugins(false));

        settingOpenWindows = host.getDocumentState().getSignalSetting("Open", "Channel", "Open Plugins");
        settingCloseWindows = host.getDocumentState().getSignalSetting("Close", "Channel", "Close Plugins");
        settingOpenWindows.addSignalObserver(() -> openChannelPlugins(true, cursorTrackItem));
        settingCloseWindows.addSignalObserver(() -> openChannelPlugins(false, cursorTrackItem));
    }

    private void observeDeviceBank(DeviceBank deviceBank) {
        deviceBank.itemCount().markInterested();

        for (int i = 0; i < NUM_DEVICES; i++) {
            Device device = deviceBank.getItemAt(i);
            device.exists().markInterested();
            device.isPlugin().markInterested();
            device.isWindowOpen().markInterested();
            device.name().markInterested();
        }
    }

    private void openAllChannelsPlugins(boolean open) {
        List<TrackItem> trackItems = this.trackItems.stream().filter(TrackItem::getExists).collect(Collectors.toList());

        for (TrackItem trackItem : trackItems) {
            this.openChannelPlugins(open, trackItem);
        }
    }

    private void openChannelPlugins(boolean open, TrackItem trackItem) {
        DeviceBank deviceBank = trackItem.getDeviceBank();
        deviceBank.scrollBy(-trackItem.getSelectedDevicePage());

        double temp = (double) deviceBank.itemCount().get() / (double) 2;
        int pages = (int) Math.ceil(temp);
        this.getHost().println("pages: " + pages);

        for (int i = 0; i < pages; i++) {
            this.getHost().println("page: " + i);

            for (int j = 0; j < NUM_DEVICES; j++) {
                Device device = deviceBank.getItemAt(j);
                this.getHost().println("device: " + j + " " + device.name().get());
                this.getHost().println("device: " + j + " " + device.exists().get());
                if (device.exists().get() && device.isPlugin().get()) {
                    device.isWindowOpen().set(open);
                }
            }
            deviceBank.scrollPageForwards();
            trackItem.setSelectedDevicePage(i + 1);
        }
    }

    @Override
    public void exit() {
        // TODO: Perform any cleanup once the driver exits
        // For now just show a popup notification for verification that it is no longer running.
        getHost().showPopupNotification("Plugin Window Control Exited");
    }

    @Override
    public void flush() {
        // we don't flush no nothing.
    }


}
