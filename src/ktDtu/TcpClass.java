package ktDtu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import ktUtil.ktCommon;
import net.sf.json.JSONObject;

public class TcpClass extends Thread {
	private ServerSocket serverSocket;

	public TcpClass() {
		try {
			this.serverSocket = new ServerSocket(ktCommon.TCP_PORT);
			System.out.println("����socket�����");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeServerSocket() {
		try {
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (!this.isInterrupted()) {// �߳�δ�ж�ִ��ѭ��
			try {
				Socket socket = serverSocket.accept();
				System.out.println("����IP=" + socket.getInetAddress().toString());

				if (socket != null) {

					// �ߵ�δ��Ȩ����
					Timer timer = new Timer();
					;
					timer.schedule(new CloseTask(socket, timer), 10 * 1000);

					new ProcessSocketData(socket, timer).start();

				}

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	// ����timer����
	class CloseTask extends TimerTask {
		Socket timer_socket;
		Timer timer;

		CloseTask(Socket s, Timer t) {
			this.timer_socket = s;
			this.timer = t;
		}

		public void run() {
			try {
				if (timer_socket != null) {
					timer_socket.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			timer.cancel(); // Terminate the timer thread
		}
	}

}

class ProcessSocketData extends Thread {
	private Socket socket;
	private Timer timer;

	public ProcessSocketData() {
		super();
	}

	public ProcessSocketData(Socket socket, Timer t) {
		this.socket = socket;
		this.timer = t;
	}

	public void run() {
		try {

			BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

			DataInputStream dis = new DataInputStream(bis);
			byte[] bytes = new byte[1]; // һ�ζ�ȡһ��byte
			String ret = "";

			while (dis.read(bytes) != -1) {
				ret += ktCommon.bytesToHexString(bytes) + " ";
				if (dis.available() == 0) { // һ������
					// System.out.println(ret);
					String get = ktCommon.hexString2String(ret.replace(" ", ""));
					System.out.println("get=" + get);

					// ��Ȩ
					if (get.startsWith("Kingcomtek_r")) {
						String[] data = ktDeviceClass.deCode(get);
						String id = data[0];
						String psw = data[1];
						String time = data[2];

						System.out.println("get_id=" + id);
						System.out.println("get_psw=" + psw);
						System.out.println("get_time=" + time);

						if (ktDeviceClass.CheckDeiveAuth(id, psw).equals(ktCommon.AUTH_DEVICE_OK)) {
							System.out.println("��Ȩ������ɹ�");

							// �Ƴ��ر�����
							this.timer.cancel();

							// ����豸��
							ktDeviceClass d = ktMapClass.map.get(id);
							if (d != null) {
								d.setTCP(socket, id, Long.parseLong(time));
							} else {
								d = new ktDeviceClass(socket, id, Integer.parseInt(time));
							}

							// ��Ӷ�Ӧ��ϵ
							ktMapClass.map.put(id, d);
							ktMapClass.map_TCPsocket.put(socket, id);

							//d.changeStatus(true);

						} else {
							System.out.println("��Ȩ�����ʧ��");
							this.socket.close();
						}

					} else if (get.startsWith("Kingcomtek_h")) { // ����
						String get_id = ktMapClass.map_TCPsocket.get(socket);
						if (get_id != null) {
							// ��������
							ktDeviceClass d = ktMapClass.map.get(get_id);
							if (d != null) {
								d.resetHeart();
							}
						}

					} else { // ͸��

						String get_id = ktMapClass.map_TCPsocket.get(socket);

						// ��������
						if (get_id != null) {
							ktDeviceClass d = ktMapClass.map.get(get_id);
							if (d != null) {
								d.resetHeart();
							}

							// �����̷߳��͵�ǰ��
							new SendWebSocket(get_id, get).start();

							// ��������
							ktDeviceClass.SendData(get_id, get);
						}

					}
					ret = "";
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
