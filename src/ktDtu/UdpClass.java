package ktDtu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import ktUtil.ktCommon;

public class UdpClass extends Thread {
	private DatagramSocket datagramSocket;

	public UdpClass() {
		try {
			this.datagramSocket = new DatagramSocket(ktCommon.UDP_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		new ProcessDatagramSocketData(datagramSocket).start();
	}

}

class ProcessDatagramSocketData extends Thread {
	private DatagramSocket datagramSocket;

	public ProcessDatagramSocketData(DatagramSocket s) {
		this.datagramSocket = s;
	}

	public void run() {
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[512], 512);
				datagramSocket.receive(packet);
				String get = new String(packet.getData(), 0, packet.getLength());
				System.out.println("get=" + get);
				System.out.println(packet.getAddress() + "/" + packet.getPort() + ":" + get);

				if (get.startsWith("Kingcomtek_r")) { // 鉴权

					String[] data = ktDeviceClass.deCode(get);
					String id = data[0];
					String psw = data[1];
					String time = data[2];

					System.out.println("get_id=" + id);
					System.out.println("get_psw=" + psw);
					System.out.println("get_time=" + time);

					if (ktDeviceClass.CheckDeiveAuth(id, psw).equals(ktCommon.AUTH_DEVICE_OK)) {
						System.out.println("鉴权结果：成功");

						ktDeviceClass d = ktMapClass.map.get(id);
						if (d != null) {
							d.setUDP(datagramSocket, packet, id, Long.parseLong(time));
						} else {
							d = new ktDeviceClass(datagramSocket, packet, id, Integer.parseInt(time));
						}

						
						ktMapClass.map.put(id, d);
						ktMapClass.map_UDPsocket.put(packet.getSocketAddress(), id);
						//d.changeStatus(true);

					} else {
						System.out.println("鉴权结果：失败");
						//datagramSocket.close();
					}

				} else if (get.startsWith("Kingcomtek_h")) { // 心跳
					String get_id = ktMapClass.map_UDPsocket.get(packet.getSocketAddress());
					if (get_id != null) {
						// 重置心跳
						ktDeviceClass d = ktMapClass.map.get(get_id);
						if (d != null) {
							d.resetHeart();
						}
					}

				} else { // 透传
					String get_id = ktMapClass.map_UDPsocket.get(packet.getSocketAddress());
					if (get_id != null) {
						// 重置心跳
						ktDeviceClass d = ktMapClass.map.get(get_id);
						if (d != null) {
							d.resetHeart();
						}
						// 开启线程发送到前端
						new SendWebSocket(get_id, get).start();

						// 发送数据
						ktDeviceClass.SendData(get_id, get);
					}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
