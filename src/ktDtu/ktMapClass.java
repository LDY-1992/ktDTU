package ktDtu;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ktMapClass {
	public static Map<String,ktDeviceClass> map= new HashMap<>();
	public static Map<Socket,String> map_TCPsocket= new HashMap<>();
	public static Map<SocketAddress,String> map_UDPsocket= new HashMap<>();
}
