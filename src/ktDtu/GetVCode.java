package ktDtu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetVCode
 */
@WebServlet("/GetVCode")
public class GetVCode extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// 验证码长宽
	private static final int vw = 130;
	private static final int vh = 34;
	private static final int vl = 5;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetVCode() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("image/jpeg");

		// 随机数字
		Date d = new Date();
		Random r = new Random(d.getTime());
		int[] n = new int[4];
		n[0] = r.nextInt(10);
		n[1] = r.nextInt(10);
		n[2] = r.nextInt(10);
		n[3] = r.nextInt(10);

		String v_code = "" + n[0] + n[1] + n[2] + n[3];
		request.getSession().setAttribute("v_code", v_code);
        log("v_code="+v_code);
		
		BufferedImage img = new BufferedImage(vw, vh, BufferedImage.TYPE_INT_RGB);
		// 表示一个图像，它具有合成整数像素的 8 位 RGB 颜色分量。
		Graphics g = img.getGraphics();
		// 设置背景色
		g.setColor(Color.white);
		g.fillRect(0, 0, vw, vh);// 画背景
		// 填充指定的矩形。使用图形上下文的当前颜色填充该矩形

		// 设置字体
		g.setFont(new Font("宋体", Font.BOLD, 18));

		for (int i = 0; i < 4; i++) {
			int y = 15 + r.nextInt(20); // 10~30范围内的一个整数，作为y坐标
			Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
			g.setColor(c);
			g.drawString("" + n[i], 5 + 30 * i, y);
		}

		// 干扰线
		for (int i = 0; i < vl; i++) {
			Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
			g.setColor(c);
			g.drawLine(r.nextInt(vw), r.nextInt(vh), r.nextInt(vw), r.nextInt(vh));
		}

		g.dispose();
		ImageIO.write(img, "JPG", response.getOutputStream());

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// doGet(request, response);
	}

}
