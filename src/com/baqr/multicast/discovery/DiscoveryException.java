package com.baqr.multicast.discovery;

/**
 * Wrapper exception for all kind of low level exceptions that can be thrown by
 * the implementation of the {@link Discovery} class.
 *
 */
@SuppressWarnings("serial")
public class DiscoveryException extends Exception {
    /**
     * Constructs a new {@link DiscoveryException} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param cause the cause of this exception.
     */
    public DiscoveryException(String detailMessage, Exception cause) {
        super(detailMessage, cause);
    }
}
