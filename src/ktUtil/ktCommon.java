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
	public final String OUT_IP = "112.64.201.138";// �Ϻ��� ��ͨ ����IP
	// TCP ����
	public final int TCP_PORT = 10010;// TCP�˿�
	// UDP ����
	public final int UDP_PORT = 10011;// UDP�˿�
	// ���ݿ���Ϣ
	public final String USER_NAME = "root";// ���ݿ��û���
	public final String PASSWORD = "5860";// ���ݿ�����
	public final String DATABASE_URL = "jdbc:mysql://localhost:3306/ktdtu?characterEncoding=utf8";// ���ݿ��ַ
	// ������Ϣ
	public final String RETURN_EMPTY_DATA = "601"; // http Request��������Ϊ��
	public final String RETURN_UN_LOGIN = "602"; // �ͻ���δ��¼û��Ȩ��
	public final String RETURN_UN_PLATFORM = "603"; // �ͻ���δ֪��Դ
	public final String RETURN_OPEN_DATABASE_FAIL = "604"; // �����ݿ�ʧ��
	public final String RETURN_HANDLE_DATA_FAIL = "605"; // ��������ʧ��
	public final String RETURN_HANDLE_DATA_OK = "606"; // �������ݳɹ�
	public final String RETURN_EMAIL_FAIL = "607"; // �����ʼ���֤ʧ��
	public final String RETURN_EMAIL_AE = "608"; // ���䲻����
	public final String RETURN_EMAIL_NE = "609"; // ���䲻����

	// ��¼����
	public final String RETURN_APP_LOGIN_UA = "101"; // ���û�������
	public final String RETURN_APP_LOGIN_NA = "102"; // ���û����� �������
	public final String RETURN_APP_LOGIN_NC = "103"; // ��֤�벻��ȷ
	public final String RETURN_APP_LOGIN_AC = "104"; // �˺�δ���伤��
	public final String RETURN_APP_LOGIN_OK = "106"; // ��½�ɹ�
	//ע�᷵��
	public final String RETURN_APP_GEGISTER_AE = "201"; // ���û��Ѿ�����
	public final String RETURN_APP_GEGISTER_OK = "206"; // ���û��Ѿ�����
	public final String RETURN_SEND_EMAIL_OK = "202"; // �ʼ����ͳɹ�
	//�ʼ�
	public final String EMAIL_HOST = "smtp.exmail.qq.com"; 
	public final String EMAIL_A = "lidongyang@kingcomtek.com"; 
	public final String EMAIL_P = "Aa68618477"; 
	public final String EMAIL_V_URL = "http://112.64.201.138:10005/ktDTU/VerificationEmail?id=";
	public final String EMAIL_F_URL = "http://112.64.201.138:10005/ktDTU/VerificationForget?id=";
	
	//������֤
	public final String LOGIN_URL = "http://112.64.201.138:10005/ktDTU/DtuLogin.html";
	
	//�ͻ������ʼ
	public final String CUSTOM_O_ID = "10860000";
	public final String CUSTOM_RIGHT_ID = "1000000000000";
	
	//�����豸����
	public final String OP_DEVICE_AE = "301"; //�豸����Ѿ�����
	
	//��֤�豸�Ϸ�����
	public final String AUTH_DEVICE_OK = "406"; //�豸�Ϸ�
	public final String AUTH_DEVICE_NE = "401"; //�豸������
	public final String AUTH_DEVICE_PF = "402"; //�豸�������
	
	// ����̨��ӡ
	public static void log(String str) {
		System.out.printf(str);
	}

	// ��֤�Ƿ��¼
	public static boolean isLogin(HttpSession session) {
		log("session" + session.getAttribute("account"));
		if (session.getAttribute("account") == null) {
			return false;
		} else {
			return true;
		}
	}

	// ��ȡrequestJSON�ַ���
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

	// �����ݿ�
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
