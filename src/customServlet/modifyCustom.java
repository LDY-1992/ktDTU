package customServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ktUtil.ktCommon;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class modifyCustom
 */
@WebServlet("/modifyCustom")
public class modifyCustom extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public modifyCustom() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript;charset=UTF-8");
		PrintWriter out = response.getWriter();
		JSONObject out_json_data = new JSONObject();

		// ¼ì²âµÇÂ½
		HttpSession session = request.getSession();
		String session_account = (String) session.getAttribute("account");
		// String session_activation = (String)
		// session.getAttribute("activation");
		log("session" + session_account);
		if (session_account == null) {
			out_json_data.put("status", ktCommon.RETURN_UN_LOGIN);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
			return;
		}

		Connection con = ktCommon.Start_database();
		if (con == null) {
			out_json_data.put("status", ktCommon.RETURN_OPEN_DATABASE_FAIL);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
			return;
		}

		String sql = null;
		PreparedStatement check = null;
		ResultSet rs = null;
		String action = request.getParameter("type");

		try {
			if (action.equals("all")) {
				String acc = request.getParameter("key");
				sql = "select * from custom where account=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, acc);
				rs = check.executeQuery();

				JSONObject json_data = new JSONObject();
				if (rs.next()) {
					json_data.put("id", rs.getString("id"));
					json_data.put("account", rs.getString("account"));
					json_data.put("cor_name", rs.getString("cor_name"));
					json_data.put("cor_email", rs.getString("cor_email"));
					json_data.put("cor_addr", rs.getString("cor_addr"));
					json_data.put("cor_phone", rs.getString("cor_phone"));
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", json_data);
				out_json_data.put("count", "");
				out.print(out_json_data);

			}

		} catch (Exception e) {
			out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_FAIL);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript;charset=UTF-8");
		PrintWriter out = response.getWriter();
		JSONObject out_json_data = new JSONObject();

		// ¼ì²âµÇÂ½
		HttpSession session = request.getSession();
		String session_account = (String) session.getAttribute("account");
		// String session_activation = (String)
		// session.getAttribute("activation");
		log("session" + session_account);
		if (session_account == null) {
			out_json_data.put("status", ktCommon.RETURN_UN_LOGIN);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
			return;
		}

		String acceptjson = ktCommon.getJsonFromRequest(request);
		if (acceptjson.length() <= 0) {
			out_json_data.put("status", ktCommon.RETURN_EMPTY_DATA);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
			return;
		}

		JSONObject jo = JSONObject.fromObject(acceptjson);

		Connection con = ktCommon.Start_database();
		if (con == null) {
			out_json_data.put("status", ktCommon.RETURN_OPEN_DATABASE_FAIL);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
			return;
		}

		String sql = null;
		PreparedStatement check = null;
		ResultSet rs = null;
		String result="";
		String action = jo.getString("action");
		try {
			if (action.equals("modify_cus")) {
				String get_name = jo.getString("modify_name");
				String get_email = jo.getString("modify_email");
				String get_addr = jo.getString("modify_addr");
				String get_phone = jo.getString("modify_phone");

				sql = "update custom set cor_name=?,cor_email=?,cor_addr=?,cor_phone=? where account=?";
				check = con.prepareStatement(sql);
				check.setString(1, get_name);
				check.setString(2, get_email);
				check.setString(3, get_addr);
				check.setString(4, get_phone);
				check.setString(5, session_account);
				check.executeUpdate();

				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);

			} else if (action.equals("modify_psw")) {
				String old_psw = jo.getString("old_psw");
				String new_psw = jo.getString("new_psw");

				sql = "select * from custom where account=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, session_account);
				rs = check.executeQuery();
				String get_psw = "";
				if (rs.next()) {
					get_psw = rs.getString("pass");
				}

				if (get_psw.equals(old_psw)) {
					sql = "update custom set pass=? where account=?";
					check = con.prepareStatement(sql);
					check.setString(1, new_psw);
					check.setString(5, session_account);
					check.executeUpdate();
					result=ktCommon.RETURN_HANDLE_DATA_OK;
				}else{
					result=ktCommon.RETURN_APP_LOGIN_NA;
				}

				out_json_data.put("status", result);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);

			}else if (action.equals("modify_d_psw")) {
				String new_psw = jo.getString("new_psw");
				sql = "select * from custom where account=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, session_account);
				rs = check.executeQuery();
				String get_mes = "";
				if (rs.next()) {
					get_mes = rs.getString("mes");
				}
				if(get_mes.length()>0){
					JSONObject json_data = JSONObject.fromObject(get_mes);
					json_data.put("device_code", new_psw);
					sql = "update custom set mes=? where account=?";
					check = con.prepareStatement(sql);
					check.setString(1, json_data.toString());
					check.setString(2, session_account);
					check.executeUpdate();
					result=ktCommon.RETURN_HANDLE_DATA_OK;
				}else{
					result=ktCommon.RETURN_APP_LOGIN_UA;
				}
				
				out_json_data.put("status", result);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);
			}

		} catch (Exception e) {
			out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_FAIL);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
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

}
