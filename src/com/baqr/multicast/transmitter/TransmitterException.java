package com.baqr.multicast.transmitter;

/**
 * Wrapper exception for all kind of low level exceptions that can be thrown by
 * the implementation of the {@link Transmitter} class.
 *
 */
@SuppressWarnings("serial")
public class TransmitterException extends Exception {
    /**
     * Constructs a new {@link TransmitterException} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param cause the cause of this exception.
     */
    public TransmitterException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
