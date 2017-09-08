package ktDtu;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import ktUtil.ktCommon;
import net.sf.json.JSONObject;

public class ktDeviceClass {
	// TCP 参数
	Socket socket;
	boolean isTCP = false;
	// UDP参数
	DatagramSocket datagramSocket;
	DatagramPacket packet;
	boolean isUDP = false;

	// WebSocket 前端虚拟设备 非实体
	Session session;
	boolean isWebSocket = false;

	// HTTP 以后扩展内容 未实现
	HttpServletResponse response;
	boolean isHTTP = false;
	// 识别码
	String id;
	// String mac;

	// 心跳
	long h_int = 0;
	Timer timer;

	// TCP
	public ktDeviceClass(Socket s, String i, long h) {
		this.socket = s;
		this.id = i;
		this.isTCP = true;
		this.isUDP = false;
		this.h_int = h;
		if (h != 0) {
			timer = new Timer();
			timer.schedule(new RemindTask(i, s), h * 1000 * 3);
		}

	}

	// UDP
	public ktDeviceClass(DatagramSocket ds, DatagramPacket p, String i, long h) {
		this.datagramSocket = ds;
		this.packet = p;
		this.id = i;
		this.isUDP = true;
		this.isTCP = false;
		this.h_int = h;

		if (h != 0) {
			timer = new Timer();
			timer.schedule(new RemindTask(i, ds), h * 1000 * 3);
		}

	}

	// WebSocket
	public void SetWebSocket(Session r, String i, boolean b) {
		this.session = r;
		this.id = i;
		this.isWebSocket = b;
	}

	// HTTP
	public ktDeviceClass(HttpServletResponse r, String i) {
		this.response = r;
		this.id = i;
		this.isHTTP = true;
	}

	// 判断是否在线
	public boolean isOnline() {
		boolean ison = false;
		if (this.isTCP || this.isUDP || this.isHTTP) {
			ison = true;
		}
		return ison;
	}

	// 重置心跳
	public void resetHeart() {
		if (h_int != 0) {
			timer.cancel();
			timer = new Timer();
			if (socket != null) {
				timer.schedule(new RemindTask(id, socket), h_int * 1000 * 3);
			} else if (datagramSocket != null) {
				timer.schedule(new RemindTask(id, datagramSocket), h_int * 1000 * 3);
			}
		}
	}

	// 模式为TCP
	public void setTCP(Socket s, String i, long h) {
		this.socket = s;
		this.datagramSocket = null;
		this.id = i;
		this.isTCP = true;
		this.isUDP = false;
		this.h_int = h;
		if (h != 0) {
			timer.cancel();
			timer = new Timer();
			timer.schedule(new RemindTask(i, s), h * 1000 * 3);
		}
	}

	// 模式为UDP
	public void setUDP(DatagramSocket ds, DatagramPacket p, String i, long h) {
		this.datagramSocket = ds;
		this.socket = null;
		this.packet = p;
		this.id = i;
		this.isUDP = true;
		this.isTCP = false;
		this.h_int = h;

		if (h != 0) {
			timer.cancel();
			timer = new Timer();
			timer.schedule(new RemindTask(i, ds), h * 1000 * 3);
		}
	}

	// 提示前端状态改变
	public void changeStatus(boolean b) {
		if (session != null) {
			JSONObject out_json_data = new JSONObject();
			out_json_data.put("action", "change");
			out_json_data.put("id", id);

			if (b) {
				out_json_data.put("content", "online");
			} else {
				out_json_data.put("content", "offline");
			}
			try {
				session.getBasicRemote().sendText(out_json_data.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// 心跳timer任务
	class RemindTask extends TimerTask {
		String timer_id;
		Socket timer_socket;
		DatagramSocket timer_datagramSocket;

		RemindTask(String id, Socket s) {
			this.timer_id = id;
			this.timer_socket = s;
		}

		RemindTask(String id, DatagramSocket s) {
			this.timer_id = id;
			this.timer_datagramSocket = s;
		}

		public void run() {
			try {

				if (timer_socket != null) {

					ktMapClass.map_TCPsocket.remove(timer_socket);
					timer_socket.close();
				} else {

					ktMapClass.map_UDPsocket.remove(timer_datagramSocket);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			ktMapClass.map.remove(timer_id);

			timer.cancel(); // Terminate the timer thread
			//changeStatus(false);
		}
	}

	// 类公用方法

	// 解密鉴权
	public static String[] deCode(String str) {
		String[] ss = { "", "", "" };

		try {
			String[] get_arr = str.split(",");

			String get_id = get_arr[1];
			String get_psw = get_arr[2];
			String get_time = get_arr[3];

			ss[0] = get_id.substring(0, 2) + get_id.substring(3, 5) + get_id.substring(6, 8) + get_id.substring(9, 11)
					+ get_id.substring(12, get_id.length());
			ss[1] = get_psw.substring(0, 1) + get_psw.substring(2, 4) + get_psw.substring(5, 7)
					+ get_psw.substring(8, 10) + get_psw.substring(11, get_psw.length());
			ss[2] = get_time;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ss;
	}

	// 验证账号密码
	public static String CheckDeiveAuth(String a, String p) {
		String r = ktCommon.AUTH_DEVICE_NE;
		Connection con = ktCommon.Start_database();
		try {
			if (con != null) {
				String sql = null;
				PreparedStatement check = null;
				ResultSet rs = null;

				sql = "select * from device where num=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, a);
				rs = check.executeQuery();
				String get_num = "";
				String get_psw = "";
				while (rs.next()) {
					get_num = rs.getString("num");
					get_psw = rs.getString("psw");
				}

				if (get_num.length() > 0) {
					if (get_num.equals(a)) {
						if (get_psw.equals(p)) {
							r = ktCommon.AUTH_DEVICE_OK;
						} else {
							r = ktCommon.AUTH_DEVICE_PF;
						}
					} else {
						r = ktCommon.AUTH_DEVICE_NE;
					}
				} else {
					r = ktCommon.AUTH_DEVICE_NE;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return r;
	}

	// 发送数据
	public static void SendData(String i, String c) {
		Connection con = ktCommon.Start_database();
		try {

			if (con != null) {
				String sql = null;
				PreparedStatement check = null;
				ResultSet rs = null;

				sql = "select * from device where num=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, i);
				rs = check.executeQuery();
				String cus = "";
				while (rs.next()) {
					cus = rs.getString("belong");
				}

				String table_device = cus + "_device";
				String table_group = cus + "_group";
				sql = "select * from " + table_device + " where num=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, i);
				rs = check.executeQuery();
				String get_list = "";
				while (rs.next()) {
					get_list = rs.getString("my_group");
				}
				String[] id_arr = get_list.split(",");

				for (int n = 1; n < id_arr.length; n++) {
					String[] str_arr = id_arr[n].split("&");
					sql = "select * from " + table_group + " where id=? limit 1";
					check = con.prepareStatement(sql);
					check.setString(1, str_arr[0]);
					rs = check.executeQuery();
					String get_arr = "";
					while (rs.next()) {
						String get_a = rs.getString("a");
						String get_b = rs.getString("b");

						if (str_arr[1].equals("a")) {
							get_arr = get_b;
						} else {
							get_arr = get_a;
						}
					}

					String[] get_d_arr = get_arr.split(",");
					for (int m = 0; m < get_d_arr.length; m++) {
						ktDeviceClass d = ktMapClass.map.get(get_d_arr[m]);
						if (d != null) {
							if (d.socket != null) {
								OutputStream send_os = d.socket.getOutputStream();
								PrintWriter send_pw = new PrintWriter(send_os);
								send_pw.write(c);
								send_pw.flush();
							} else if (d.datagramSocket != null) {
								d.packet.setData(c.getBytes());
								d.datagramSocket.send(d.packet);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// 给自己发信息
	public static void SendSelfData(String i, String c) {
		try {
			ktDeviceClass d = ktMapClass.map.get(i);
			if (d.isTCP) {
				if (d.socket != null) {
					OutputStream send_os = d.socket.getOutputStream();
					PrintWriter send_pw = new PrintWriter(send_os);
					send_pw.write(c);
					send_pw.flush();
				}
			} else if (d.isUDP) {
				d.packet.setData(c.getBytes());
				d.datagramSocket.send(d.packet);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
