
 
import java.io.*;
import java.net.*;
import java.util.*;


public class TCPServer
{
	
	
	// Message constants
	public static final String INVITE = 		"INVITE";
	public static final String TRYING = 		"100";
	public static final String RINGING = 		"180";
	public static final String OK = 		    "200";
	public static final String ACK = 			"ACK";
	public static final String BYE = 			"BYE";
	public static final String ERROR = 		    "ERROR";
	
	
   public static void main(String argv[]) throws Exception
      {
      	Scanner scan = new Scanner(System.in);	
      	AudioStreamUDP stream = null;
      	String server_msg;							// Variable Declarations
         String client_msg;
         String reply=null;
         String capitalizedSentence;
         ServerSocket welcomeSocket = new ServerSocket(5060);
		 while(true)
         {
     
         	System.out.println("\n\n---------------Server Running--------------------");
         	Socket connectionSocket = welcomeSocket.accept();
           // System.out.println("\n Socket: " + connectionSocket);     // In comming socket printing only
                  										// Retrieve comming streams from socket
            BufferedReader inFromClient =
            new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            PrintWriter outToClient =	new PrintWriter(connectionSocket.getOutputStream(), true); // true -> auto flush
           
            System.out.println("\n----------------Client Connected------------------- ");
            					
            client_msg = inFromClient.readLine();
          	if(client_msg.equals(INVITE))
          		{
            	
            	server_msg = TRYING;					
				outToClient.println(server_msg);
				System.out.println(" \n\n Server-------------------TRYING-------------------->Client  ");
				Thread.sleep(2000);
				server_msg = RINGING;					// 180 Ringing
				outToClient.println(server_msg);
				System.out.println(" \n\n  Server-------------------Ringing-------------------->Client  ");
				Thread.sleep(2000);
				server_msg = OK;						// 200 Ok 
			 	outToClient.println(server_msg);
            	System.out.println(" \n\n  Server-------------------OK-------------------->Client");
            	Thread.sleep(2000);
            	client_msg = inFromClient.readLine();     // receiving ACK from client
				if(client_msg.equals(ACK))
					{
            		System.out.println("\n     ---------------------SESSION INITIATED--------------");
					}
            		 
            	}
                									// here is code for voice 
				System.out.println("\n---------------Start Conversation(ENTER)-------------------");
				reply = scan.nextLine();    	
				stream = new AudioStreamUDP();
				int localSVPort = stream.getLocalPort();    //getting voice port on the server
				outToClient.println(localSVPort);  
				
				String localCLport = inFromClient.readLine();		         // sending voice port to client
				//System.out.println("\n Client port for voice: " + localCLport);
				int clientPort=Integer.parseInt(localCLport);				// parsing for integet port
				
				InetAddress address = connectionSocket.getInetAddress();    // getting ip address of client
				stream.connectTo(address, clientPort);       //Here Clients IP address, clients voice port
					
				System.out.println("\n ------------------------TALK-----------------------------");
				stream.startStreaming();
		           
		        client_msg = inFromClient.readLine();  
				if(client_msg.equals(BYE)){
            		
				stream.stopStreaming();
				server_msg = OK;
				outToClient.println(server_msg);
				System.out.println("\nServer-----------------OK--------------------------->Client");
				stream.close();
			} 
		
		}
	   
  }
}