package com.baqr.multicast.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import android.content.Intent;

/**
 * Internal class for handling the network connection of the {@link Discovery} class
 * on a background thread.
 *
 */
class DiscoveryThread extends Thread {
    private static final int MAXIMUM_PACKET_BYTES = 102400;
    private static final String EXTRA_MESSAGE = "message";

    private String multicastAddress;
    private int port;
    private MulticastSocket socket;
    private DiscoveryListener listener;

    private volatile boolean running;

    /**
     * Create a new background thread that handles incoming Intents on the given
     * multicast address and port.
     *
     * Do not instantiate this class yourself. Use the {@link Discovery} class
     * instead.
     *
     * @param multicastAddress
     * @param port
     * @param listener
     */
    /* package-private */ DiscoveryThread(String multicastAddress, int port, DiscoveryListener listener) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.listener = listener;
    }

    public void run() {
        running = true;

        listener.onDiscoveryStarted();

        try {
            socket = createSocket();
            receiveIntents();
        } catch(IOException exception) {
            if (running) {
                listener.onDiscoveryError(exception);
            }
        } finally {
            closeSocket();
        }

        listener.onDiscoveryStopped();
    }

    protected MulticastSocket createSocket() throws UnknownHostException, IOException {
        InetAddress address = InetAddress.getByName(multicastAddress);

        MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(address);

        return socket;
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close();
        }
    }

    public void stopDiscovery() {
        running = false;

        closeSocket();
    }

    protected void receiveIntents() throws IOException {
        while (running) {
            DatagramPacket packet = new DatagramPacket(
                new byte[MAXIMUM_PACKET_BYTES], MAXIMUM_PACKET_BYTES
            );

            socket.receive(packet);

			byte[] data = packet.getData();
			int length = packet.getLength();
			
			InputStreamReader input = new InputStreamReader(
			        new ByteArrayInputStream(data), Charset.forName("UTF-8"));

			StringBuilder str = new StringBuilder();
			for (int value; (value = input.read()) != -1; )
			    str.append((char) value);
			    
			String string=str.toString().replaceAll("[^\\p{Print}]",""); 
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_MESSAGE, string);
			
			// String intentUri = new String(data, 0, length);
			// Intent intent = Intent.parseUri(intentUri, 0);
			
			listener.onIntentDiscovered(packet.getAddress(), intent);
        }
    }
}
