package ktDtu;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ktUtil.ktCommon;

/**
 * Servlet implementation class VerificationEmail
 */
@WebServlet("/VerificationEmail")
public class VerificationEmail extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VerificationEmail() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		String req_id = request.getParameter("id");

		Connection con = ktCommon.Start_database();
		if (con == null) {
			out.print(ktCommon.RETURN_OPEN_DATABASE_FAIL);
			return;
		}
		String sql = null;
		PreparedStatement check = null;
		ResultSet rs = null;
		try {
			sql = "select * from custom where id=?";
			check = con.prepareStatement(sql);
			check.setString(1, req_id);
			rs = check.executeQuery();
			String get_ac = null;
			while (rs.next()) {
				get_ac = rs.getString("activation");
			}

			if (!get_ac.equals(ktCommon.CUSTOM_O_ID)) {
				out.print(
						"<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><div style='text-align: center;margin-top:100px;margin-left:auto;margin-right:auto;width:800px;height:100px;background-color: #f7f7f7;'><h2>邮箱已经激活，请点击下面链接返回登陆</h2><a href='"
								+ ktCommon.LOGIN_URL + "'>返回登陆</a></div></body></html>");

			} else {
				sql = "select * from custom ORDER BY activation desc limit 1 for update";
				check = con.prepareStatement(sql);
				rs = check.executeQuery();

				long n = 0;
				while (rs.next()) {
					n = Integer.parseInt(rs.getString("activation"));
				}
				n = n + 1;
				String ac = "" + n;
				String customID = UUID.randomUUID().toString().replace("-", "");
				sql = "update custom set activation=?,id=? where id=?";
				check = con.prepareStatement(sql);
				check.setString(1, ac);
				check.setString(2, customID);
				check.setString(3, req_id);
				check.executeUpdate();
				out.print(
						"<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><div style='text-align: center;margin-top:100px;margin-left:auto;margin-right:auto;width:800px;height:100px;background-color: #f7f7f7;'><h2>邮箱激活成功，请点击下面链接返回登陆</h2><a href='"
								+ ktCommon.LOGIN_URL + "'>返回登陆</a></div></body></html>");
			}

		} catch (Exception e) {
			out.print(
					"<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><div style='text-align: center;margin-top:100px;margin-left:auto;margin-right:auto;width:800px;height:100px;background-color: #f7f7f7;'><h2>邮箱激活失败，请稍后重试。</h2></div></body></html>");
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

	}

}
