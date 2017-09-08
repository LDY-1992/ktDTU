package customServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 * Servlet implementation class cusGroup
 */
@WebServlet("/cusGroup")
public class cusGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public cusGroup() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
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
		String result = "";

		String action = request.getParameter("type");
		String val = request.getParameter("val");
		String p1 = request.getParameter("p1");
		String p2 = request.getParameter("p2");
		String table_name = session_account + "_group";

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

				sql = "select * from " + table_name + " order by id desc limit ?,?";
				check = con.prepareStatement(sql);
				check.setInt(1, n2 * (n1 - 1));
				check.setInt(2, n2);
				rs = check.executeQuery();

				JSONObject json_data = new JSONObject();
				while (rs.next()) {
					String tab_id = rs.getString("id");
					String tab_name = rs.getString("name");
					String get_a = rs.getString("a");
					String get_b = rs.getString("b");

					String[] as = get_a.split(",");
					String[] bs = get_b.split(",");

					json_data.put("tab_num", tab_id);
					json_data.put("tab_name", tab_name);
					json_data.put("a_account", as.length);
					json_data.put("b_account", bs.length);

					jsonarray_data.add(json_data);
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("count", count);
				out_json_data.put("to_tab", "group");
			} else if (action.equals("single")) {
				sql = "select * from " + table_name + " where id=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, val);
				rs = check.executeQuery();
				JSONObject json_data = new JSONObject();
				if (rs.next()) {
					json_data.put("id", rs.getString("id"));
					json_data.put("name", rs.getString("name"));
					json_data.put("a", rs.getString("a"));
					json_data.put("b", rs.getString("b"));
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", json_data);
				out_json_data.put("count", 0);
				out_json_data.put("to_tab", "group");
			} else if (action.equals("query_name")) {
				int n1 = Integer.valueOf(p1);
				int n2 = Integer.valueOf(p2);
				sql = "select * from " + table_name + " where name like ? order by name desc limit ?,?";
				check = con.prepareStatement(sql);
				check.setString(1, "%" + val + "%");
				check.setInt(2, n2 * (n1 - 1));
				check.setInt(3, n2);
				rs = check.executeQuery();
				
				JSONObject json_data = new JSONObject();
				int n = 0;
				while (rs.next()) {
					n = n + 1;
					String tab_id = rs.getString("id");
					String tab_name = rs.getString("name");
					String get_a = rs.getString("a");
					String get_b = rs.getString("b");

					String[] as = get_a.split(",");
					String[] bs = get_b.split(",");

					json_data.put("tab_num", tab_id);
					json_data.put("tab_name", tab_name);
					json_data.put("a_account", as.length);
					json_data.put("b_account", bs.length);

					jsonarray_data.add(json_data);
				}
				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", jsonarray_data);
				out_json_data.put("count", n);
				out_json_data.put("to_tab", "group");

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
		// TODO Auto-generated method stub
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
		String result = "";
		try {
			String action = jo.getString("action");
			String table_group = session_account + "_group";
			String table_device = session_account + "_device";
			if (action.equals("add_group")) {
				// ÊÂÎñÖ´ÐÐÊý¾Ý¿â²Ù×÷
				con.setAutoCommit(false);

				String get_name = jo.getString("name");

				Date now = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String riqi = dateFormat.format(now);

				JSONObject json_data = new JSONObject();
				json_data.put("create_date", riqi);
				json_data.put("last_handle_date", riqi);

				String get_a = jo.getString("a");
				String get_b = jo.getString("b");

				sql = "insert into " + table_group + "(name,a,b,mes) value(?,?,?,?)";
				check = con.prepareStatement(sql);
				check.setString(1, get_name);
				check.setString(2, get_a);
				check.setString(3, get_b);
				check.setString(4, json_data.toString());
				check.executeUpdate();

				String[] as = get_a.split(",");
				String[] bs = get_b.split(",");

				sql = "select * from " + table_group + " where name=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, get_name);
				rs = check.executeQuery();
				long id = 0;
				if (rs.next()) {
					id = rs.getLong("id");
				}

				for (int i = 0; i < as.length; i++) {
					sql = "update " + table_device + " set my_group=CONCAT(my_group,?) where num=?";
					check = con.prepareStatement(sql);
					check.setString(1, "," + id + "&a");
					check.setString(2, as[i]);
					check.executeUpdate();
				}
				for (int i = 0; i < bs.length; i++) {
					sql = "update " + table_device + " set my_group=CONCAT(my_group,?) where num=?";
					check = con.prepareStatement(sql);
					check.setString(1, "," + id + "&b");
					check.setString(2, bs[i]);
					check.executeUpdate();
				}

				result = ktCommon.RETURN_HANDLE_DATA_OK;

				out_json_data.put("status", result);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);
				con.commit();
			} else if (action.equals("delete_single")) {
				con.setAutoCommit(false);
				String g_n = jo.getString("num");

				sql = "select * from " + table_group + " where id=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, g_n);
				rs = check.executeQuery();
				String d_a = "";
				String d_b = "";
				while (rs.next()) {
					d_a = rs.getString("a");
					d_b = rs.getString("b");
				}

				if (d_a.length() > 0) {
					String[] as = d_a.split(",");
					for (int i = 0; i < as.length; i++) {
						sql = "update " + table_device + " set my_group=REPLACE(my_group,?,'') where num=?";
						check = con.prepareStatement(sql);
						check.setString(1, "," + g_n + "&a");
						check.setString(2, as[i]);
						check.executeUpdate();
					}
				}

				if (d_b.length() > 0) {
					String[] as = d_b.split(",");
					for (int i = 0; i < as.length; i++) {
						sql = "update " + table_device + " set my_group=REPLACE(my_group,?,'') where num=?";
						check = con.prepareStatement(sql);
						check.setString(1, "," + g_n + "&b");
						check.setString(2, as[i]);
						check.executeUpdate();
					}
				}

				sql = "delete from " + table_group + " where id=?";
				check = con.prepareStatement(sql);
				check.setString(1, g_n);
				check.executeUpdate();
				con.commit();

				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
				out_json_data.put("data", "");
				out_json_data.put("count", "");
				out.print(out_json_data);
			} else if (action.equals("modify_group")) {
				con.setAutoCommit(false);
				String g_n = jo.getString("id");
				String get_name = jo.getString("name");
				String get_a = jo.getString("a");
				String get_b = jo.getString("b");

				// É¾³ýÖ®Ç°
				sql = "select * from " + table_group + " where id=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, g_n);
				rs = check.executeQuery();
				String d_a = "";
				String d_b = "";
				while (rs.next()) {
					d_a = rs.getString("a");
					d_b = rs.getString("b");
				}

				if (d_a.length() > 0) {
					String[] as = d_a.split(",");
					for (int i = 0; i < as.length; i++) {
						sql = "update " + table_device + " set my_group=REPLACE(my_group,?,'') where num=?";
						check = con.prepareStatement(sql);
						check.setString(1, "," + g_n + "&a");
						check.setString(2, as[i]);
						check.executeUpdate();
					}
				}

				if (d_b.length() > 0) {
					String[] as = d_b.split(",");
					for (int i = 0; i < as.length; i++) {
						sql = "update " + table_device + " set my_group=REPLACE(my_group,?,'') where num=?";
						check = con.prepareStatement(sql);
						check.setString(1, "," + g_n + "&b");
						check.setString(2, as[i]);
						check.executeUpdate();
					}
				}

				// Ìí¼Ó
				String[] as = get_a.split(",");
				String[] bs = get_b.split(",");
				for (int i = 0; i < as.length; i++) {
					sql = "update " + table_device + " set my_group=CONCAT(my_group,?) where num=?";
					check = con.prepareStatement(sql);
					check.setString(1, "," + g_n + "&a");
					check.setString(2, as[i]);
					check.executeUpdate();
				}
				for (int i = 0; i < bs.length; i++) {
					sql = "update " + table_device + " set my_group=CONCAT(my_group,?) where num=?";
					check = con.prepareStatement(sql);
					check.setString(1, "," + g_n + "&b");
					check.setString(2, bs[i]);
					check.executeUpdate();
				}

				sql = "update " + table_group + " set name=?,a=?,b=? where id=?";
				check = con.prepareStatement(sql);
				check.setString(1, get_name);
				check.setString(2, get_a);
				check.setString(3, get_b);
				check.setString(4, g_n);
				check.executeUpdate();

				con.commit();

				out_json_data.put("status", ktCommon.RETURN_HANDLE_DATA_OK);
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
				con.setAutoCommit(true);
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
