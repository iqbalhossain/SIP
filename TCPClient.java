
 
import java.io.*;
import java.net.*;
import java.util.*;


public class TCPClient
{
	
	
	public static final String INVITE = 		"INVITE";
	public static final String TRYING = 		"100";
	public static final String RINGING = 		"180";
	public static final String OK = 		"200";
	public static final String ACK = 		"ACK";
	public static final String BYE = 		"BYE";
	public static final String ERROR = 		"ERROR";
	
	
	
	
 public static void main(String argv[]) throws Exception
 {
 
  AudioStreamUDP stream = null;			
  String server_msg;
  String modifiedSentence;
  String client_msg;
  String reply=null;
  Scanner scan = new Scanner(System.in);
  
  
  								 
    
  System.out.print("  \n\n Enter Callee  IP:   ");								
  BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
  String server_IP = inFromUser.readLine();
  
  							
  Socket clientSocket = new Socket(server_IP, 5060);
  
 							
  
  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true); // true -> auto flush
  
  						
  outToServer.println(INVITE);
  	System.out.println("\nClient------------INVITE------------>Server");
  Thread.sleep(2000);
  server_msg = inFromServer.readLine();
  if(server_msg.equals(TRYING)) {
		//System.out.println("\nClient------------TRYING------------>Server");
								
		}	
   else 
		server_msg = inFromServer.readLine();     
		if(server_msg.equals(TRYING)) {
	
					
			server_msg = inFromServer.readLine();      
			if(server_msg.equals(RINGING)) {
	
							
			server_msg = inFromServer.readLine(); 
			if(server_msg.equals(OK)) {
	
				
				System.out.println("\nClient-------------ACK--------------->Server");
				
				outToServer.println(ACK);  			
			
			stream = new AudioStreamUDP();
			int localCLPort = stream.getLocalPort();    
			outToServer.println(localCLPort);         
									
			String localSVport = inFromServer.readLine();		         
			System.out.println("\n Server Port for Voice :  " + localSVport);
			int serverPort=Integer.parseInt(localSVport);
																	
			InetAddress address = clientSocket.getInetAddress();   
			stream.connectTo(address,serverPort);  	
			    
			stream.startStreaming();
			System.out.println("\n -----------------End Call(ENTER)---------------------");
			reply = scan.nextLine();			
			outToServer.println(BYE);
			Thread.sleep(2000);
			System.out.println("\nClient------------------BYE------------------------>Server");
			server_msg = inFromServer.readLine();
			if(server_msg.equals(OK)) {
				System.out.println("\n---------------CALL TERMINATED------------ ");
				stream.stopStreaming();
					stream.close();
				}
					
			}

  		}
  

  }
 }
}