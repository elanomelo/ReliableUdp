import java.io.*;
import java.net.*;
import java.util.*;


/**
 * 
 * @author Elano
 *
 */
public class ReliableUdpReceiver {	
	
	private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 100;
	private static final int PACKET_MAX_SIZE = 1024;
	
	private static String START_FLAG = "HELLO";	
	private static String FINISH_FLAG = "BYE";
	private static String CLOSE_FLAG = "FIN";
	private static String ACKNOWLEDGE_FLAG = "ACK";
	
	static HashMap<Integer,InetAddress> pool = new HashMap<Integer,InetAddress>();	
	static HashMap<String,LinkedList<DatagramPacket>> clientPackets = new HashMap<>();
	public static void main(String[] args) throws Exception {
		int port = 8053;
	
		Random random = new Random();
		DatagramSocket socket = new DatagramSocket(port);
		
		while(true) {
			DatagramPacket request = new DatagramPacket(new byte[PACKET_MAX_SIZE], PACKET_MAX_SIZE);
			socket.receive(request);
			if (random.nextDouble() < LOSS_RATE) {
				System.out.println(" Packet Loss.");
				continue;
			}
			Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
			printData(request);
			
			InetAddress clientHost = request.getAddress();
			int clientPort = request.getPort();
			String message = extractMessage(request);
			
			if(message.equals(START_FLAG)) {
				initLocalBuffer(request, clientHost);
				byte[] messageReply = "HELLO ACK".getBytes();	
				send(messageReply, socket, clientHost, clientPort);
			}else if(message.equals(FINISH_FLAG)) {				
				send(CLOSE_FLAG.getBytes(),socket,clientHost,clientPort);
				LinkedList<DatagramPacket> dataBuff = clientPackets.get(getHash(request));
				String realMessage = "";
				for(int i = 0;i < dataBuff.size();i++) {
					realMessage += new String(dataBuff.get(i).getData());
				}
				System.out.println(realMessage);
				System.out.println("Mensagem: " + realMessage);
			}else {
				LinkedList<DatagramPacket> dataBuff = clientPackets.get(getHash(request));
				dataBuff.add(request);
				send(ACKNOWLEDGE_FLAG.getBytes(),socket,clientHost,clientPort);
			}			
							
		}
	}
	
	private static void initLocalBuffer(DatagramPacket request,InetAddress clientHost) {
		if(pool.get(request.getPort()) == null) {
			pool.put(request.getPort(), clientHost);
		}
		if(clientPackets.get(getHash(request)) == null) {
			clientPackets.put(getHash(request), new LinkedList<DatagramPacket>());
		}		
	}
	
	private static String getHash(DatagramPacket request) {
		return String.format("%s.%s", request.getAddress(),request.getPort());
	}
	
	private static void send(byte[] messageReply,DatagramSocket socket,InetAddress clientHost,int clientPort) throws IOException {
		DatagramPacket reply = new DatagramPacket(messageReply, messageReply.length, clientHost, clientPort);
		socket.send(reply);
		System.out.println(" Reply sent.");		
	}
	
	private static void printData(DatagramPacket request) throws Exception {		
		// Print host address and data received from it.
		System.out.println("Received from " + request.getAddress().getHostAddress() + ": " + extractMessage(request));
	}
	private static String extractMessage(DatagramPacket request) throws Exception {
		// Obtain references to the packet's array of bytes.
		byte[] buf = request.getData();
		// Wrap the bytes in a byte array input stream,
		// so that you can read the data as a stream of bytes.
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		// Wrap the byte array output stream in an input stream reader,
		// so you can read the data as a stream of characters.
		InputStreamReader isr = new InputStreamReader(bais);
		// Wrap the input stream reader in a bufferred reader,
		// so you can read the character data a line at a time.
		// (A line is a sequence of chars terminated by any combination of \r and \n.)
		BufferedReader br = new BufferedReader(isr);
		// The message data is contained in a single line, so read this line.	
		// The message data is contained in a single line, so read this line.
		String line = br.readLine();
		return new String(line).trim();
	}

}
