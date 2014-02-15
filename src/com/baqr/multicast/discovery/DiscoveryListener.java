package com.baqr.multicast.discovery;

import java.net.InetAddress;

import android.content.Intent;

/**
 * A {@link DiscoveryListener} receives notifications from a {@link Discovery}.
 * Notifications indicate lifecycle related events as well as successfully received
 * {@link Intent}s.
 *
 */
public interface DiscoveryListener {
    /**
     * The {@link Discovery} has been started and is now waiting for incoming
     * {@link Intent}s.
     */
    public void onDiscoveryStarted();

    /**
     * The {@link Discovery} has been stopped.
     */
    public void onDiscoveryStopped();

    /**
     * An unrecoverable error occured. The {@link Discovery} is going to be stopped.
     *
     * @param exception Actual exception that occured in the background thread
     */
    public void onDiscoveryError(Exception exception);

    /**
     * Called when the {@link Discovery} has successfully received an {@link Intent}.
     *
     * @param address The IP address of the sender of the {@link Intent}.
     * @param intent The received {@link Intent}.
     */
    public void onIntentDiscovered(InetAddress address, Intent intent);
}
