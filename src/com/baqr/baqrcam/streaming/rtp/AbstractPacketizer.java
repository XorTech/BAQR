package com.baqr.baqrcam.streaming.rtp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Random;

import com.baqr.baqrcam.streaming.rtcp.SenderReport;


/**
 * 
 * Each packetizer inherits from this one and therefore uses RTP and UDP.
 *
 */
abstract public class AbstractPacketizer {

	protected static final int rtphl = RtpSocket.RTP_HEADER_LENGTH;

	protected RtpSocket socket = null;
	protected SenderReport report = null;
	protected InputStream is = null;
	protected byte[] buffer;
	
	protected long ts = 0, intervalBetweenReports = 5000, delta = 0;

	public AbstractPacketizer() throws IOException {
		int ssrc = new Random().nextInt();
		ts = new Random().nextInt();
		socket = new RtpSocket();
		report = new SenderReport();
		socket.setSSRC(ssrc);
		report.setSSRC(ssrc);
	}

	public RtpSocket getRtpSocket() {
		return socket;
	}

	public SenderReport getRtcpSocket() {
		return report;
	}


	public void setSSRC(int ssrc) {
		socket.setSSRC(ssrc);
		report.setSSRC(ssrc);
	}

	public int getSSRC() {
		return socket.getSSRC();
	}

	public void setInputStream(InputStream is) {
		this.is = is;
	}

	public void setTimeToLive(int ttl) throws IOException {
		socket.setTimeToLive(ttl);
	}

	/**
	 * Sets the destination of the stream.
	 * @param dest The destination address of the stream
	 * @param rtpPort Destination port that will be used for RTP
	 * @param rtcpPort Destination port that will be used for RTCP
	 */
	public void setDestination(InetAddress dest, int rtpPort, int rtcpPort) {
		socket.setDestination(dest, rtpPort);
		report.setDestination(dest, rtcpPort);		
	}

	/**
	 * Sets the temporal interval between two RTCP Sender Reports.
	 * Default interval is set to 5 secondes.
	 * Set 0 to disable RTCP.
	 * @param interval The interval in milliseconds
	 */
	public void setSenderReportsInterval(long interval) {
		intervalBetweenReports = interval;
	}
	
	/** Starts the packetizer. */
	public abstract void start() throws IOException;

	/** Stops the packetizer. */
	public abstract void stop();

	/** Updates data for RTCP SR and sends the packet. */
	protected void send(int length) throws IOException {
		socket.commitBuffer(length);
		report.update(length);
	}

	/** For debugging purposes. */
	protected static String printBuffer(byte[] buffer, int start,int end) {
		String str = "";
		for (int i=start;i<end;i++) str+=","+Integer.toHexString(buffer[i]&0xFF);
		return str;
	}

	/** Used in packetizers to estimate timestamps in RTP packets. */
	protected static class Statistics {

		public final static String TAG = "Statistics";
		
		private int count=700, c = 0;
		private float m = 0, q = 0;
		private long elapsed = 0;
		private long start = 0;
		private long duration = 0;
		private long period = 10000000000L;
		private boolean initoffset = false;
		
		public Statistics() {}
		
		public Statistics(int count, int period) {
			this.count = count;
			this.period = period;
		}
		
		public void reset() {
			initoffset = false;
			q = 0; m = 0; c = 0;
			elapsed = 0;
			start = 0;
			duration = 0;
		}
		
		public void push(long value) {
			elapsed += value;
			if (elapsed>period) {
				elapsed = 0;
				long now = System.nanoTime();
				if (!initoffset || (now - start < 0)) {
					start = now;
					duration = 0;
					initoffset = true;
				}
				// Prevents drifting issues by comparing the real duration of the 
				// stream with the sum of all temporal lengths of RTP packets. 
				value += (now - start) - duration;
				//Log.d(TAG, "sum1: "+duration/1000000+" sum2: "+(now-start)/1000000+" drift: "+((now-start)-duration)/1000000+" v: "+value/1000000);
			}
			if (c<5) {
				// We ignore the first 20 measured values because they may not be accurate
				c++;
				m = value;
			} else {
				m = (m*q+value)/(q+1);
				if (q<count) q++;
			}
		}
		
		public long average() {
			long l = (long)m;
			duration += l;
			return l;
		}

	}
	
}
