package ktUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface ktCommon {
	public final String OUT_IP = "112.64.201.138";// 上海市 联通 外网IP
	// TCP 常量
	public final int TCP_PORT = 10010;// TCP端口
	// UDP 常量
	public final int UDP_PORT = 10011;// UDP端口
	// 数据库信息
	public final String USER_NAME = "root";// 数据库用户名
	public final String PASSWORD = "5860";// 数据库密码
	public final String DATABASE_URL = "jdbc:mysql://localhost:3306/ktdtu?characterEncoding=utf8";// 数据库地址
	// 返回信息
	public final String RETURN_EMPTY_DATA = "601"; // http Request请求数据为空
	public final String RETURN_UN_LOGIN = "602"; // 客户端未登录没有权限
	public final String RETURN_UN_PLATFORM = "603"; // 客户端未知来源
	public final String RETURN_OPEN_DATABASE_FAIL = "604"; // 打开数据库失败
	public final String RETURN_HANDLE_DATA_FAIL = "605"; // 处理数据失败
	public final String RETURN_HANDLE_DATA_OK = "606"; // 处理数据成功
	public final String RETURN_EMAIL_FAIL = "607"; // 发送邮件验证失败
	public final String RETURN_EMAIL_AE = "608"; // 邮箱不存在
	public final String RETURN_EMAIL_NE = "609"; // 邮箱不存在

	// 登录返回
	public final String RETURN_APP_LOGIN_UA = "101"; // 此用户不存在
	public final String RETURN_APP_LOGIN_NA = "102"; // 此用户存在 密码错误
	public final String RETURN_APP_LOGIN_NC = "103"; // 验证码不正确
	public final String RETURN_APP_LOGIN_AC = "104"; // 账号未邮箱激活
	public final String RETURN_APP_LOGIN_OK = "106"; // 登陆成功
	//注册返回
	public final String RETURN_APP_GEGISTER_AE = "201"; // 此用户已经存在
	public final String RETURN_APP_GEGISTER_OK = "206"; // 此用户已经存在
	public final String RETURN_SEND_EMAIL_OK = "202"; // 邮件发送成功
	//邮件
	public final String EMAIL_HOST = "smtp.exmail.qq.com"; 
	public final String EMAIL_A = "lidongyang@kingcomtek.com"; 
	public final String EMAIL_P = "Aa68618477"; 
	public final String EMAIL_V_URL = "http://112.64.201.138:10005/ktDTU/VerificationEmail?id=";
	public final String EMAIL_F_URL = "http://112.64.201.138:10005/ktDTU/VerificationForget?id=";
	
	//邮箱验证
	public final String LOGIN_URL = "http://112.64.201.138:10005/ktDTU/DtuLogin.html";
	
	//客户编号起始
	public final String CUSTOM_O_ID = "10860000";
	public final String CUSTOM_RIGHT_ID = "1000000000000";
	
	//操作设备返回
	public final String OP_DEVICE_AE = "301"; //设备编号已经存在
	
	//验证设备合法返回
	public final String AUTH_DEVICE_OK = "406"; //设备合法
	public final String AUTH_DEVICE_NE = "401"; //设备不存在
	public final String AUTH_DEVICE_PF = "402"; //设备密码错误
	
	// 控制台打印
	public static void log(String str) {
		System.out.printf(str);
	}

	// 验证是否登录
	public static boolean isLogin(HttpSession session) {
		log("session" + session.getAttribute("account"));
		if (session.getAttribute("account") == null) {
			return false;
		} else {
			return true;
		}
	}

	// 获取requestJSON字符串
	public static String getJsonFromRequest(HttpServletRequest request) {
		String acceptjson = "";
		try {
			request.setCharacterEncoding("UTF-8");
			BufferedReader br = new BufferedReader(
					new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
			StringBuffer sb = new StringBuffer("");
			String temp;
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			br.close();
			acceptjson = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return acceptjson;
	}

	// 打开数据库
	public static Connection Start_database() {
		String SqlUrl = DATABASE_URL;
		String username = USER_NAME;
		String password = PASSWORD;
		Connection con;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			con = DriverManager.getConnection(SqlUrl, username, password);
			return con;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	//
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String BytesHexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	public static String get_date() {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		String date = dateFormat.format(now);
		return date;
	}

	public static String hexString2String(String src) {
		String temp = "";
		for (int i = 0; i < src.length() / 2; i++) {
			temp = temp + (char) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return temp;
	}

}
