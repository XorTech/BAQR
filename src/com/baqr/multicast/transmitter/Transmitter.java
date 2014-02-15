package com.baqr.multicast.transmitter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.baqr.multicast.internal.AndroidNetworkIntents;

import android.content.Intent;

/**
 * Transmitter class for sending {@link Intent}s through network.
 *
 */
public class Transmitter {
    private String multicastAddress;
    private int port;
    private static final String EXTRA_MESSAGE = "message";

    /**
     * Creates a new {@link Transmitter} instance that will sent {@link Intent}s to
     * the default multicast address and port.
     */
    public Transmitter() {
        this(
            AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
            AndroidNetworkIntents.DEFAULT_PORT
        );
    }

    /**
     * Creates a new {@link Transmitter} instance that will sent {@link Intent}s to
     * the default multicast address and the given port port.
     *
     * @param The destination network port.
     */
    public Transmitter(int port) {
        this(
            AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
            port
        );
    }

    /**
     * Creates a new {@link Transmitter} instance that will sent {@link Intent}s to
     * the given multicast address and port.
     *
     * @param multicastAddress The destination multicast address, e.g. 225.4.5.6.
     * @param port The destination network port.
     */
    public Transmitter(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    /**
     * Sends an {@link Intent} through the network to any listening {@link Discovery}
     * instance.
     *
     * @param intent The intent to send.
     * @throws TransmitterException
     */
    public void transmit(Intent intent) throws TransmitterException {
        MulticastSocket socket = null;

        try {
            socket = createSocket();
            transmit(socket, intent);
        } catch (UnknownHostException exception) {
            throw new TransmitterException("Unknown host", exception);
        } catch (SocketException exception) {
            throw new TransmitterException("Can't create DatagramSocket", exception);
        } catch (IOException exception) {
            throw new TransmitterException("IOException during sending intent", exception);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    protected MulticastSocket createSocket() throws IOException {
        return new MulticastSocket();
    }

    /**
     * Actual (private) implementation that serializes the {@link Intent} and sends
     * it as {@link DatagramPacket}. Used to separate the implementation from the
     * error handling code.
     *
     * @param socket
     * @param intent
     * @throws UnknownHostException
     * @throws IOException
     */
    private void transmit(MulticastSocket socket, Intent intent) throws UnknownHostException, IOException {
        //byte[] data = intent.toUri(0).getBytes();
        
    	// Remove bottom 3 lines for origninal tranmit. 
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        
        // System.out.println(message);
        
        byte[] data = message.getBytes();

        DatagramPacket packet = new DatagramPacket(
            data,
            data.length,
            InetAddress.getByName(multicastAddress),
            port
        );

        socket.send(packet);
    }
}
