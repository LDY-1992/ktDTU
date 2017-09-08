package customServlet;

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
import javax.servlet.http.HttpSession;

import ktDtu.EmailManager;
import ktUtil.ktCommon;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetEmailCode
 */
@WebServlet("/GetEmailCode")
public class GetEmailCode extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetEmailCode() {
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
		String result=ktCommon.RETURN_HANDLE_DATA_FAIL;

		try {
			con.setAutoCommit(false);
			sql = "select * from custom where account=?";
			check = con.prepareStatement(sql);
			check.setString(1, session_account);
			rs = check.executeQuery();
			if(rs.next()){
				String email_a=rs.getString("cor_email");
				String get_mes=rs.getString("mes");
				// 随机生成密码字母a开头
				StringBuilder str = new StringBuilder();// 定义变长字符串
				Random random = new Random();
				// 随机生成数字，并添加到字符串
				for (int i = 0; i < 4; i++) {
					str.append(random.nextInt(10));
				}
				String code=str.toString();
				
				JSONObject json_data=JSONObject.fromObject(get_mes);
				json_data.put("email_code", code);
				
				sql="update custom set mes=? where account=?";
				check = con.prepareStatement(sql);
				check.setString(1, json_data.toString());
				check.setString(2, session_account);
				check.executeUpdate();
				
				// 发送邮件
				String[] to_emails = { email_a };
				String email_content = "<!DOCTYPE html><html><head><meta charset='utf-8'><title>金讯科技透传云</title></head><body><br><br><h2>金讯科技透传云邮箱验证码</h2><p>邮箱验证码为："+code;
				EmailManager email = new EmailManager(ktCommon.EMAIL_HOST, ktCommon.EMAIL_A, ktCommon.EMAIL_P);
				boolean b = email.sendMail(ktCommon.EMAIL_A, to_emails, null, "金训科技透传云邮箱验证码", email_content, "");

				if (b) {
					result = ktCommon.RETURN_SEND_EMAIL_OK;
				} else {
					result = ktCommon.RETURN_EMAIL_FAIL;
					con.rollback();
				}
			}
			con.commit();
			out_json_data.put("status", result);
			out_json_data.put("data", "");
			out_json_data.put("count", "");
			out.print(out_json_data);
			
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

}
