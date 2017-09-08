package ktDtu;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ktUtil.ktCommon;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class customLogin
 */
@WebServlet("/customLogin")
public class customLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public customLogin() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		// 获取HTTP请求数据
		String acceptjson = ktCommon.getJsonFromRequest(request);
		if (acceptjson.length() <= 0) {
			out.print(ktCommon.RETURN_EMPTY_DATA);
			return;
		}
		JSONObject jo = JSONObject.fromObject(acceptjson);
		String action = jo.getString("action");
		Connection con = ktCommon.Start_database();
		if (con == null) {
			out.print(ktCommon.RETURN_OPEN_DATABASE_FAIL);
			return;
		}
		String sql = null;
		PreparedStatement check = null;
		ResultSet rs = null;
		String result = null;

		try {
			// 注册
			if (action.equals("register")) {
				log("进入注册流程");
				// 事务执行数据库操作
				con.setAutoCommit(false);
				// plan ID json_plan_string
				String customID = UUID.randomUUID().toString().replace("-", "");
				String register_account = jo.getString("register_account");
				String register_psw = jo.getString("register_psw");
				String register_name = jo.getString("register_name");
				String register_email = jo.getString("register_email");
				String register_addr = jo.getString("register_addr");
				String register_phone = jo.getString("register_phone");

				sql = "select * from custom where account=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, register_account);
				rs = check.executeQuery();
				if (rs.next()) {
					result = ktCommon.RETURN_APP_GEGISTER_AE;
				} else {

					sql = "select * from custom where cor_email=? limit 1";
					check = con.prepareStatement(sql);
					check.setString(1, register_email);
					rs = check.executeQuery();
					if (rs.next()) {
						result = ktCommon.RETURN_EMAIL_AE;
					} else {
						// 日期
						JSONObject json_data = new JSONObject();

						Date now = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						String riqi = dateFormat.format(now);

						json_data.put("create_date", riqi);
						json_data.put("last_ip", request.getRemoteAddr());
						json_data.put("login_count", "0");
						json_data.put("device_code", customID.substring(0, 8));

						String activation = ktCommon.CUSTOM_O_ID;
						sql = "insert into custom value(?,?,?,?,?,?,?,?,?)";
						check = con.prepareStatement(sql);
						check.setString(1, customID);
						check.setString(2, register_account);
						check.setString(3, register_psw);
						check.setString(4, register_name);
						check.setString(5, register_email);
						check.setString(6, register_addr);
						check.setString(7, register_phone);
						check.setString(8, activation);
						check.setString(9, json_data.toString());
						check.executeUpdate();

						String table_device = register_account + "_device";
						String table_group = register_account + "_group";

						sql = "CREATE TABLE " + table_device
								+ " (id char(50) NOT NULL,num char(50) NOT NULL,name char(50) NOT NULL,psw char(50) NOT NULL,status char(50) NOT NULL,my_group text NOT NULL,mes varchar(2000) NOT NULL,PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
						check.executeUpdate(sql);

						sql = "CREATE TABLE " + table_group
								+ " (id bigint(50) NOT NULL AUTO_INCREMENT,name char(50) NOT NULL,a text NOT NULL,b text NOT NULL,mes varchar(2000) NOT NULL,PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=12726000 DEFAULT CHARSET=utf8";
						check.executeUpdate(sql);

						// 发送邮件
						String[] to_emails = { register_email };
						String email_content = "<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><h2>金讯科技透传云邮箱验证，请点击下面链接进行验证：</h2><a href='"
								+ ktCommon.EMAIL_V_URL + customID + "'>点击此链接完成邮箱验证</a></body></html>";
						EmailManager email = new EmailManager(ktCommon.EMAIL_HOST, ktCommon.EMAIL_A, ktCommon.EMAIL_P);
						boolean b = email.sendMail(ktCommon.EMAIL_A, to_emails, null, "金训科技透传云邮箱验证", email_content, "");

						if (b) {
							result = ktCommon.RETURN_APP_GEGISTER_OK;
						} else {
							result = ktCommon.RETURN_EMAIL_FAIL;
							con.rollback();
						}
					}
				}
				out.print(result);
				con.commit();
			} else if (action.equals("login")) {
				String login_account = jo.getString("login_account");
				String login_psw = jo.getString("login_psw");
				String login_code = jo.getString("login_code");

				String v_code = (String) request.getSession().getAttribute("v_code");
				log("VV_code=" + v_code);
				if (!login_code.equals(v_code)) {
					result = ktCommon.RETURN_APP_LOGIN_NC;
				} else {
					sql = "select * from custom where account=? limit 1";
					check = con.prepareStatement(sql);
					check.setString(1, login_account);
					rs = check.executeQuery();
					result = ktCommon.RETURN_APP_LOGIN_UA;
					while (rs.next()) {
						String password = rs.getString("pass");
						String activation = rs.getString("activation");
						String mes = rs.getString("mes");
						JSONObject json_data = JSONObject.fromObject(mes);
						String d_code = json_data.getString("device_code");
						if (activation.equals(ktCommon.CUSTOM_O_ID)) {
							result = ktCommon.RETURN_APP_LOGIN_AC;
						} else if (login_psw.equals(password)) {
							result = ktCommon.RETURN_APP_LOGIN_OK + "," + rs.getString("account") + "," + d_code + ","
									+ activation;
							request.getSession().setAttribute("account", rs.getString("account"));
							request.getSession().setAttribute("id", rs.getString("id"));
							request.getSession().setAttribute("activation", activation);
							request.getSession().setMaxInactiveInterval(12 * 60 * 60);
						} else {
							result = ktCommon.RETURN_APP_LOGIN_NA;
						}
					}
				}
				out.print(result);
			}

		} catch (Exception e) {
			out.print(ktCommon.RETURN_HANDLE_DATA_FAIL);
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				con.setAutoCommit(true);
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
