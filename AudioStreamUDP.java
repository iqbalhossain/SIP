import java.io.*;
import java.net.*;

import javax.sound.sampled.*;

public class AudioStreamUDP {
	
	public static final int BUFFER_VS_FRAMES_RATIO = 16; //32
	public static final boolean DEBUG = false;

	public AudioStreamUDP() throws IOException {
		this.receiverSocket = new DatagramSocket();
		this.senderSocket = new DatagramSocket();
	    
	    format = new AudioFormat(20000, 16, 1, true, true); // 44100
	    this.receiver = new Receiver(receiverSocket, format);
	    this.sender = new Sender(senderSocket, format);
	}
	
	public int getLocalPort() {
		return receiverSocket.getLocalPort();
	}
	
	public synchronized void connectTo(InetAddress remoteAddress, int remotePort) throws IOException {
		sender.connectTo(remoteAddress, remotePort);
	}
	
	public synchronized void startStreaming() {
		receiver.startActivity();
		sender.startActivity();
	}

	public synchronized void stopStreaming() {
		receiver.stopActivity();
		sender.stopActivity();		
	}
	
	public synchronized void close()  {
		if(receiverSocket != null) receiverSocket.close();
		if(senderSocket != null) senderSocket.close();
	}
	
	private DatagramSocket senderSocket, receiverSocket;
	private Receiver receiver = null;
	private Sender sender = null;
	private AudioFormat format;
}

class Receiver implements Runnable{

	Receiver(DatagramSocket socket, AudioFormat format) {
		this.socket = socket;
		this.format = format;
	}
	
    synchronized  void startActivity() {
    	if(receiverThread == null) {
    		receiverThread = new Thread(this);
    		receiverThread.start();
    	}
    }
    
    synchronized void stopActivity() {
    	receiverThread = null;
    }
	
	public void run() {
    	// Make the run method a private matter
    	if(receiverThread != Thread.currentThread()) return;
    	
    	try {
    		initializeLine();
    		
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / AudioStreamUDP.BUFFER_VS_FRAMES_RATIO;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            if(AudioStreamUDP.DEBUG) {
            	System.out.println("bufferLengthInFrames = " + bufferLengthInFrames);
            	System.out.println("bufferLengthInBytes = " + bufferLengthInBytes);
            }
            byte[] data = new byte[bufferLengthInBytes];
            DatagramPacket packet = new DatagramPacket(data, bufferLengthInBytes);
            int numBytesRead = 0;
	    	
	    	line.start();
            int packets = 0;
            while (receiverThread != null) {
            	socket.receive(packet);
                numBytesRead = packet.getLength();
                if(AudioStreamUDP.DEBUG) {
                	System.out.println("Received bytes = " + numBytesRead + ", packets = " + packets++);
                }
                int numBytesRemaining = numBytesRead;
                while (numBytesRemaining > 0 ) {
                    numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                }
            }			  		
    	}
    	catch(Exception e) {
    		// If the thread is blocked in a receive call, an exception is thrown when 
    		// the socket is closed, causing the thread to unblock.
    		// System.out.println("In Receiver: " + e.toString());
    	}
    	finally {
    		this.cleanUp();
    	}
	}
	
	private DatagramSocket socket = null;
	private Thread receiverThread = null;
	private SourceDataLine line = null;
	private AudioFormat format = null;
	
    private void initializeLine() throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Line matching " + info + " not supported.");
            return;
        }
        
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format, line.getBufferSize());
    }
    
    void cleanUp() {
		try {
			if(line != null) {
				line.stop();
				line.close();
			}
		} catch(Exception e) {}
    }
    
    protected void finalize() {
    	this.cleanUp();
    }
}

class Sender implements Runnable {
	
	Sender(DatagramSocket socket, AudioFormat format) {
		this.socket = socket;
		this.format = format;
	}
	
	public void connectTo(InetAddress remoteAddress, int remotePort) throws IOException {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		//socket.connect(new InetSocketAddress(remoteAddress, remotePort));
	}
	
    synchronized  void startActivity() {
    	if(senderThread == null) {
    		senderThread = new Thread(this);
    		senderThread.start();
    	}
    }
    
    synchronized void stopActivity() {
    	senderThread = null;
    }
	
	public void run() {
    	// Make the run method a private matter
    	if(senderThread != Thread.currentThread()) return;
    	
    	try {
    		initializeLine();
            
	        int frameSizeInBytes = format.getFrameSize();
	        int bufferLengthInFrames = line.getBufferSize() / AudioStreamUDP.BUFFER_VS_FRAMES_RATIO;
	        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
	        byte[] data = new byte[bufferLengthInBytes];
	        int numBytesRead;
	        DatagramPacket packet = null;
	    	
	    	line.start();
	    	int packets = 0;
	    	//System.out.println("Ready");
	        while (senderThread != null) {
	            if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
	                break;
	            }
	            packet = new DatagramPacket(data, numBytesRead, remoteAddress, remotePort);
	            socket.send(packet);
	            if(AudioStreamUDP.DEBUG) {
	            	System.out.println("Bytes sent = " + numBytesRead + ", packets = " + packets++);
	            }
	        }			  		
    	}
    	catch(Exception e) {
    		// Exception is thrown if socket is closed before last call to send.
    		// System.out.println("In Sender: " + e.toString());
    	}
    	finally {
    		this.cleanUp();
    	}
	}
	
	private DatagramSocket socket = null;
	private InetAddress remoteAddress = null;
	private int remotePort = 0;
	private Thread senderThread = null;
	private TargetDataLine line = null;
	private AudioFormat format = null;
	
    private void initializeLine() throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Line matching " + info + " not supported.");
            return;
        }
        
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format, line.getBufferSize());
    }
    
    void cleanUp() {
		try {
			if(line != null) {
				line.stop();
				line.close();
			}
		} catch(Exception e) {}
    }
    
    protected void finalize() {
    	this.cleanUp();
    }
}

