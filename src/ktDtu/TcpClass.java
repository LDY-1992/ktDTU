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
			System.out.println("开启socket服务端");
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
		while (!this.isInterrupted()) {// 线程未中断执行循环
			try {
				Socket socket = serverSocket.accept();
				System.out.println("接入IP=" + socket.getInetAddress().toString());

				if (socket != null) {

					// 踢掉未鉴权连接
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

	// 心跳timer任务
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
			byte[] bytes = new byte[1]; // 一次读取一个byte
			String ret = "";

			while (dis.read(bytes) != -1) {
				ret += ktCommon.bytesToHexString(bytes) + " ";
				if (dis.available() == 0) { // 一个请求
					// System.out.println(ret);
					String get = ktCommon.hexString2String(ret.replace(" ", ""));
					System.out.println("get=" + get);

					// 鉴权
					if (get.startsWith("Kingcomtek_r")) {
						String[] data = ktDeviceClass.deCode(get);
						String id = data[0];
						String psw = data[1];
						String time = data[2];

						System.out.println("get_id=" + id);
						System.out.println("get_psw=" + psw);
						System.out.println("get_time=" + time);

						if (ktDeviceClass.CheckDeiveAuth(id, psw).equals(ktCommon.AUTH_DEVICE_OK)) {
							System.out.println("鉴权结果：成功");

							// 移除关闭心跳
							this.timer.cancel();

							// 添加设备类
							ktDeviceClass d = ktMapClass.map.get(id);
							if (d != null) {
								d.setTCP(socket, id, Long.parseLong(time));
							} else {
								d = new ktDeviceClass(socket, id, Integer.parseInt(time));
							}

							// 添加对应关系
							ktMapClass.map.put(id, d);
							ktMapClass.map_TCPsocket.put(socket, id);

							//d.changeStatus(true);

						} else {
							System.out.println("鉴权结果：失败");
							this.socket.close();
						}

					} else if (get.startsWith("Kingcomtek_h")) { // 心跳
						String get_id = ktMapClass.map_TCPsocket.get(socket);
						if (get_id != null) {
							// 重置心跳
							ktDeviceClass d = ktMapClass.map.get(get_id);
							if (d != null) {
								d.resetHeart();
							}
						}

					} else { // 透传

						String get_id = ktMapClass.map_TCPsocket.get(socket);

						// 重置心跳
						if (get_id != null) {
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
					ret = "";
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
