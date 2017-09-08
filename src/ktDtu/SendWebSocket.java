package ktDtu;

import java.text.SimpleDateFormat;
import java.util.Date;


import javax.websocket.Session;

import net.sf.json.JSONObject;

public class SendWebSocket extends Thread {

	private String content;
	private String id;

	public SendWebSocket() {
		super();
	}

	public SendWebSocket(String i, String c) {
		this.id = i;
		this.content = c;
	}

	public void run() {
		try {

			ktDeviceClass d = ktMapClass.map.get(id);
			if (d.isWebSocket) {
				Session session = d.session;

				Date now = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String riqi = dateFormat.format(now);

				JSONObject out_json_data = new JSONObject();
				out_json_data.put("action", "send");
				out_json_data.put("id", id);
				out_json_data.put("date", riqi);
				out_json_data.put("content", content);

				session.getBasicRemote().sendText(out_json_data.toString());
			}

		} catch (Exception e) {
			e.getMessage();
		}
	}
}
