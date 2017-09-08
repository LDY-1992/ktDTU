package ktDtu;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ktUtil.ktCommon;

/**
 * Servlet implementation class customForget
 */
@WebServlet("/customForget")
public class customForget extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public customForget() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		String e_a = request.getParameter("email");

		Connection con = ktCommon.Start_database();
		if (con == null) {
			out.print(ktCommon.RETURN_OPEN_DATABASE_FAIL);
			return;
		}

		String sql = null;
		PreparedStatement check = null;
		ResultSet rs = null;
		String result = "";

		try {
			sql = "select * from custom where cor_email=? limit 1";
			check = con.prepareStatement(sql);
			check.setString(1, e_a);
			rs = check.executeQuery();
			if (rs.next()) {

				sql = "select * from custom where cor_email=? limit 1";
				check = con.prepareStatement(sql);
				check.setString(1, e_a);
				rs = check.executeQuery();
				String id = "";
				if (rs.next()) {
					id = rs.getString("id");
				}

				// 随机生成密码字母a开头
				StringBuilder str = new StringBuilder();// 定义变长字符串
				Random random = new Random();
				// 随机生成数字，并添加到字符串
				for (int i = 0; i < 8; i++) {
					str.append(random.nextInt(10));
				}
				String key = "a" + str.toString();

				// 发送邮件
				String[] to_emails = { e_a };
				String email_content = "<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><h2>金讯科技透传云找回密码</h2><p>点击下面链接密码会重置为："+key+"<p>如要更改，请登陆后修改。<a href='"
						+ ktCommon.EMAIL_F_URL + id + "&k=" + key + "'>点击此链接完成密码重置</a></body></html>";
				EmailManager email = new EmailManager(ktCommon.EMAIL_HOST, ktCommon.EMAIL_A, ktCommon.EMAIL_P);
				boolean b = email.sendMail(ktCommon.EMAIL_A, to_emails, null, "金训科技透传云密码找回", email_content, "");

				if (b) {
					result = ktCommon.RETURN_SEND_EMAIL_OK;
				} else {
					result = ktCommon.RETURN_EMAIL_FAIL;
				}
			} else {
				result = ktCommon.RETURN_EMAIL_NE;
			}
			out.print(result);

		} catch (Exception e) {
			out.print(ktCommon.RETURN_HANDLE_DATA_FAIL);
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
