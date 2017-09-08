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
 * Servlet implementation class VerificationForget
 */
@WebServlet("/VerificationForget")
public class VerificationForget extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VerificationForget() {
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
		String req_key = request.getParameter("k");

		Connection con = ktCommon.Start_database();
		if (con == null) {
			out.print(ktCommon.RETURN_OPEN_DATABASE_FAIL);
			return;
		}
		String sql = null;
		PreparedStatement check = null;
		ResultSet rs = null;
		try {

			String customID = UUID.randomUUID().toString().replace("-", "");
			sql = "update custom set id=?,pass=? where id=?";
			check = con.prepareStatement(sql);
			check.setString(1, customID);
			check.setString(2, req_key);
			check.setString(3, req_id);
			check.executeUpdate();
			out.print(
					"<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><div style='text-align: center;margin-top:100px;margin-left:auto;margin-right:auto;width:800px;height:100px;background-color: #f7f7f7;'><h2>密码重置成功，请点击下面链接返回登陆</h2><a href='"
							+ ktCommon.LOGIN_URL + "'>返回登陆</a></div></body></html>");

		} catch (Exception e) {
			out.print(
					"<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><div style='text-align: center;margin-top:100px;margin-left:auto;margin-right:auto;width:800px;height:100px;background-color: #f7f7f7;'><h2>密码重置失败，请稍后重试。</h2></div></body></html>");
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
