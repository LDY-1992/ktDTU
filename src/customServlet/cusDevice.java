package customServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
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
import javax.servlet.http.HttpSession;

import ktDtu.ktDeviceClass;
import ktDtu.ktMapClass;
import ktUtil.ktCommon;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class cusDevice
 */
@WebServlet("/cusDevice")
public class cusDevice extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public cusDevice() {
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

		// 检测登陆
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
		String result = "";

		String action = request.getParameter("type");
		String val = request.getParameter("val");
		String p1 = request.getParameter("p1");
		String p2 = request.getParameter("p2");
		String table_name = session_account + "_device";

		JSONArray jsonarray_data = new JSONArray();

		try {
			if (action.equals("all")) {
				sql = "select count(*) from " + table_name;
				check = con.prepareStatement(sql);
				rs = check.executeQuery();
				long count = 0;
				if (rs.next()) {
					count = rs.getInt(1);
				}

				int n1 = Integer.valueOf(p1);
				int n2 = Integer.valueOf(p2);

				sql = "select * from " + table_name + " order by num desc limit ?,?";
				check = con.prepareStatement(sql);
				check.setInt(1, n2 * (n1 - 1));
				check.setInt(2, n2);
				rs = check.executeQuery();

				JSONObject json_data = new JSONObject();
				while (rs.next()) {
					String tab_num = rs.getString("num");
					String tab_name = rs.getString("name");
					String tab_psw = rs.getString("psw");
					// String tab_status = rs.getString("status");
					String tab_status = "离线";
					ktDeviceClass d = ktMapClass.map.get(tab_num);
					if (d != null) {
						if (d.isOnline()) {
							tab_status = "在线";
						}
					}

					json_data.put("tab_num", tab_num);
					json_data.put("tab_name", tab_name);
					json_data.put("tab_psw", tab_psw);
					json_data.put("tab_status", tab_status);
					jsonarray_data.add(json_data);
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("count", count);
				out_json_data.put("to_tab", "device");

			} else if (action.equals("all_for_g")) {
				sql = "select * from " + table_name;
				check = con.prepareStatement(sql);
				rs = check.executeQuery();
				JSONObject json_data = new JSONObject();
				while (rs.next()) {
					String tab_num = rs.getString("num");
					String tab_name = rs.getString("name");
					json_data.put("tab_num", tab_num);
					json_data.put("tab_name", tab_name);
					jsonarray_data.add(json_data);
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("to_tab", "group");
			} else if (action.equals("single")) {
				sql = "select * from " + table_name + " where num=?";
				check = con.prepareStatement(sql);
				check.setString(1, val);
				rs = check.executeQuery();
				JSONObject json_data = new JSONObject();
				if (rs.next()) {
					String tab_num = rs.getString("num");
					String tab_name = rs.getString("name");
					String tab_psw = rs.getString("psw");
					String tab_status = "离线";
					String tab_mes = rs.getString("mes");
					JSONObject jo = JSONObject.fromObject(tab_mes);
					String c_time = jo.getString("create_date");
					String m_time = jo.getString("last_handle_date");
					String mes = jo.getString("mes");

					ktDeviceClass d = ktMapClass.map.get(tab_num);
					if (d != null) {
						if (d.isOnline()) {
							tab_status = "在线";
						}
					}

					json_data.put("tab_num", tab_num);
					json_data.put("tab_name", tab_name);
					json_data.put("tab_psw", tab_psw);
					json_data.put("tab_status", tab_status);
					json_data.put("c_time", c_time);
					json_data.put("m_time", m_time);
					json_data.put("mes", mes);

					out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
					out_json_data.put("data", json_data);
					out_json_data.put("to_tab", "group");
				}
			} else if (action.equals("query_num")) {
				int n1 = Integer.valueOf(p1);
				int n2 = Integer.valueOf(p2);

				sql = "select * from " + table_name + " where num like ? order by num desc limit ?,?";
				check = con.prepareStatement(sql);
				check.setString(1, "%" + val + "%");
				check.setInt(2, n2 * (n1 - 1));
				check.setInt(3, n2);
				rs = check.executeQuery();

				JSONObject json_data = new JSONObject();
				int n = 0;
				while (rs.next()) {
					n = n + 1;
					String tab_num = rs.getString("num");
					String tab_name = rs.getString("name");
					String tab_psw = rs.getString("psw");
					// String tab_status = rs.getString("status");
					String tab_status = "离线";
					ktDeviceClass d = ktMapClass.map.get(tab_num);
					if (d != null) {
						if (d.isOnline()) {
							tab_status = "在线";
						}
					}

					json_data.put("tab_num", tab_num);
					json_data.put("tab_name", tab_name);
					json_data.put("tab_psw", tab_psw);
					json_data.put("tab_status", tab_status);
					jsonarray_data.add(json_data);
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("count", n);
				out_json_data.put("to_tab", "device");

			} else if (action.equals("query_name")) {
				int n1 = Integer.valueOf(p1);
				int n2 = Integer.valueOf(p2);

				sql = "select * from " + table_name + " where name like ? order by num desc limit ?,?";
				check = con.prepareStatement(sql);
				check.setString(1, "%" + val + "%");
				check.setInt(2, n2 * (n1 - 1));
				check.setInt(3, n2);
				rs = check.executeQuery();

				JSONObject json_data = new JSONObject();
				int n = 0;
				while (rs.next()) {
					n = n + 1;
					String tab_num = rs.getString("num");
					String tab_name = rs.getString("name");
					String tab_psw = rs.getString("psw");
					// String tab_status = rs.getString("status");
					String tab_status = "离线";
					ktDeviceClass d = ktMapClass.map.get(tab_num);
					if (d != null) {
						if (d.isOnline()) {
							tab_status = "在线";
						}
					}

					json_data.put("tab_num", tab_num);
					json_data.put("tab_name", tab_name);
					json_data.put("tab_psw", tab_psw);
					json_data.put("tab_status", tab_status);
					jsonarray_data.add(json_data);
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("count", n);
				out_json_data.put("to_tab", "device");

			} else if (action.equals("query_status")) {
				int n1 = Integer.valueOf(p1);
				int n2 = Integer.valueOf(p2);

				sql = "select * from " + table_name + " order by num desc limit ?,?";
				check = con.prepareStatement(sql);
				check.setInt(1, n2 * (n1 - 1));
				check.setInt(2, n2);
				rs = check.executeQuery();

				JSONObject json_data = new JSONObject();
				int n = 0;
				while (rs.next()) {
					n = n + 1;
					String tab_num = rs.getString("num");
					String tab_name = rs.getString("name");
					String tab_psw = rs.getString("psw");
					// String tab_status = rs.getString("status");
					String tab_status = "离线";
					ktDeviceClass d = ktMapClass.map.get(tab_num);
					if (d != null) {
						if (d.isOnline()) {
							tab_status = "在线";
							json_data.put("tab_num", tab_num);
							json_data.put("tab_name", tab_name);
							json_data.put("tab_psw", tab_psw);
							json_data.put("tab_status", tab_status);
							jsonarray_data.add(json_data);
						}
					}
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("count", n);
				out_json_data.put("to_tab", "device");
			}

			out.print(out_json_data);
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

		// 检测登陆
		HttpSession session = request.getSession();
		String session_account = (String) session.getAttribute("account");
		String session_activation = (String) session.getAttribute("activation");
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
		String result = "";
		try {
			String action = jo.getString("action");
			String table_name = session_account + "_device";

			if (action.equals("add_single")) {
				String d_num = jo.getString("num");
				BigInteger big_a = new BigInteger(session_activation);
				BigInteger big_b = new BigInteger(ktCommon.CUSTOM_RIGHT_ID);
				BigInteger big_c = big_a.multiply(big_b);
				BigInteger big_d = new BigInteger(d_num);
				BigInteger big_e = big_c.add(big_d);

				String num = big_e.toString();
				sql = "select * from " + table_name + " where num=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, num);
				rs = check.executeQuery();
				if (rs.next()) {
					result = ktCommon.OP_DEVICE_AE;
				} else {
					String deviceID = UUID.randomUUID().toString().replace("-", "");
					String d_name = jo.getString("name");
					String d_psw = jo.getString("psw");
					String d_mes = jo.getString("mes");

					Date now = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					String riqi = dateFormat.format(now);

					JSONObject json_data = new JSONObject();
					json_data.put("create_date", riqi);
					json_data.put("last_handle_date", riqi);
					json_data.put("mes", d_mes);

					sql = "insert into " + table_name + " value(?,?,?,?,?,?,?)";
					check = con.prepareStatement(sql);
					check.setString(1, deviceID);
					check.setString(2, num);
					check.setString(3, d_name);
					check.setString(4, d_psw);
					check.setString(5, "离线");
					check.setString(6, "空");
					check.setString(7, json_data.toString());
					check.executeUpdate();

					sql = "insert into device value(?,?,?,?,?,?,?)";
					check = con.prepareStatement(sql);
					check.setString(1, deviceID);
					check.setString(2, num);
					check.setString(3, d_name);
					check.setString(4, d_psw);
					check.setString(5, "离线");
					check.setString(6, session_account);
					check.setString(7, json_data.toString());
					check.executeUpdate();

					result = ktCommon.RETURN_HANDLE_DATA_OK;
				}
				out_json_data.put("status", result);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);
			} else if (action.equals("add_list")) {

				String d_name = jo.getString("name");
				String d_num1 = jo.getString("num1");
				String d_num2 = jo.getString("num2");
				String d_gap = jo.getString("gap");
				String d_psw = jo.getString("psw");
				String d_mes = jo.getString("mes");

				long a1 = Long.parseLong(d_num1);
				long a2 = Long.parseLong(d_num2);
				long gap = Long.parseLong(d_gap);

				result = ktCommon.RETURN_HANDLE_DATA_OK;

				BigInteger big_a = new BigInteger(session_activation);
				BigInteger big_b = new BigInteger(ktCommon.CUSTOM_RIGHT_ID);
				BigInteger big_c = big_a.multiply(big_b);

				while (a1 <= a2) {

					BigInteger big_d = BigInteger.valueOf(a1);
					BigInteger big_e = big_c.add(big_d);

					String num = big_e.toString();
					sql = "select * from " + table_name + " where num=? limit 1";
					check = con.prepareStatement(sql);
					check.setString(1, num);
					rs = check.executeQuery();
					if (rs.next()) {
						result = ktCommon.OP_DEVICE_AE;
						break;
					} else {
						String deviceID = UUID.randomUUID().toString().replace("-", "");

						Date now = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						String riqi = dateFormat.format(now);

						JSONObject json_data = new JSONObject();
						json_data.put("create_date", riqi);
						json_data.put("last_handle_date", riqi);
						json_data.put("mes", d_mes);

						sql = "insert into " + table_name + " value(?,?,?,?,?,?,?)";
						check = con.prepareStatement(sql);
						check.setString(1, deviceID);
						check.setString(2, num);
						check.setString(3, d_name);
						check.setString(4, d_psw);
						check.setString(5, "离线");
						check.setString(6, "空");
						check.setString(7, json_data.toString());
						check.executeUpdate();

						sql = "insert into device value(?,?,?,?,?,?,?)";
						check = con.prepareStatement(sql);
						check.setString(1, deviceID);
						check.setString(2, num);
						check.setString(3, d_name);
						check.setString(4, d_psw);
						check.setString(5, "离线");
						check.setString(6, session_account);
						check.setString(7, json_data.toString());
						check.executeUpdate();

					}
					a1 = a1 + gap;
				}

				out_json_data.put("status", result);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);

			} else if (action.equals("delete_single")) {
				String d_num = jo.getString("num");

				sql = "delete from " + table_name + " where num=?";
				check = con.prepareStatement(sql);
				check.setString(1, d_num);
				check.executeUpdate();

				sql = "delete from device where num=?";
				check = con.prepareStatement(sql);
				check.setString(1, d_num);
				check.executeUpdate();

				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);

			} else if (action.equals("delete_list")) {
				JSONArray json_array = jo.getJSONArray("num");
				for (int i = 0; i < json_array.size(); i++) {

					sql = "delete from " + table_name + " where num=?";
					check = con.prepareStatement(sql);
					check.setString(1, json_array.getString(i));
					check.executeUpdate();

					sql = "delete from device where num=?";
					check = con.prepareStatement(sql);
					check.setString(1, json_array.getString(i));
					check.executeUpdate();
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);

			} else if (action.equals("modify_single_device")) {
				String d_num = jo.getString("num");
				String d_name = jo.getString("name");
				String d_psw = jo.getString("psw");
				String d_mes = jo.getString("mes");

				Date now = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String riqi = dateFormat.format(now);

				sql = "select * from " + table_name + " where num=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, d_num);
				rs = check.executeQuery();

				if (rs.next()) {
					String mes = rs.getString("mes");
					JSONObject json_data = JSONObject.fromObject(mes);
					json_data.put("last_handle_date", riqi);
					json_data.put("mes", d_mes);

					sql = "update " + table_name + " set name=?,psw=?,mes=? where num=? limit 1";
					check = con.prepareStatement(sql);
					check.setString(1, d_name);
					check.setString(2, d_psw);
					check.setString(3, json_data.toString());
					check.setString(4, d_num);
					check.executeUpdate();
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);

			} else {
				out_json_data.put("status", ktCommon.RETURN_EMPTY_DATA);
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
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
