package ktDtu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import ktUtil.ktCommon;
import net.sf.json.JSONObject;
import ktDtu.GetHttpSessionConfigurator;

/**
 * @ServerEndpoint ע����һ�����ε�ע�⣬���Ĺ�����Ҫ�ǽ�Ŀǰ���ඨ���һ��websocket��������,
 *                 ע���ֵ�������ڼ����û����ӵ��ն˷���URL��ַ,�ͻ��˿���ͨ�����URL�����ӵ�WebSocket��������
 */
@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)

public class WebSocket {
	// ��̬������������¼��ǰ������������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
	private static int onlineCount = 0;
	private Session session;

	/**
	 * ���ӽ����ɹ����õķ���
	 * 
	 * @param session
	 *            ��ѡ�Ĳ�����sessionΪ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {

		HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		String session_account = (String) httpSession.getAttribute("account");
		ktCommon.log("WebSocket[session]=" + session_account);
		if (session_account == null) {
			try {
				session.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return;
		}
		this.session = session;
		addOnlineCount(); // ��������1
		System.out.println("�������Ӽ��룡��ǰ��������Ϊ" + getOnlineCount());
	}

	/**
	 * ���ӹرյ��õķ���
	 */
	@OnClose
	public void onClose() {
		// webSocketMap.remove(this.key); // ��set��ɾ��
		subOnlineCount(); // ��������1
		System.out.println("��һ���ӹرգ���ǰ��������Ϊ" + getOnlineCount());
	}

	/**
	 * �յ��ͻ�����Ϣ����õķ���
	 * 
	 * @param message
	 *            �ͻ��˷��͹�������Ϣ
	 * @param session
	 *            ��ѡ�Ĳ���
	 */
	@OnMessage
	public void onMessage(String message, Session session, EndpointConfig config) {
		System.out.println("���Կͻ��˵���Ϣ:" + message);
		// ������Ϣ
		try {
			JSONObject jo = JSONObject.fromObject(message);
			String action = jo.getString("action");
			if (action.equals("register")) {
				String id = jo.getString("id");
				ktDeviceClass d = ktMapClass.map.get(id);
				if (d != null) {
					d.SetWebSocket(session, id, true);
					if (!d.isOnline()) {
						JSONObject out_json_data = new JSONObject();
						out_json_data.put("action", "register");
						out_json_data.put("id", id);
						out_json_data.put("content", "offline");
						out_json_data.put("status", "offline");
						this.sendMessage(out_json_data.toString());
					}else{
						JSONObject out_json_data = new JSONObject();
						out_json_data.put("action", "register");
						out_json_data.put("id", id);
						out_json_data.put("content", "online");
						out_json_data.put("status", "online");
						this.sendMessage(out_json_data.toString());
					}
				} else {
					JSONObject out_json_data = new JSONObject();
					out_json_data.put("action", "register");
					out_json_data.put("id", id);
					out_json_data.put("content", "offline");
					out_json_data.put("status", "offline");
					this.sendMessage(out_json_data.toString());
				}
			}else if(action.equals("closeWebSocket")){
				String id = jo.getString("id");
				ktDeviceClass d = ktMapClass.map.get(id);
				d.isWebSocket=false;
				d.session=null;
			}else if(action.equals("send")){
				String id = jo.getString("id");
				String content = jo.getString("content");
				
				ktDeviceClass.SendSelfData(id, content);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * ��������ʱ����
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("��������");
		error.printStackTrace();
	}

	/**
	 * ������������漸��������һ����û����ע�⣬�Ǹ����Լ���Ҫ��ӵķ�����
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
		// this.session.getAsyncRemote().sendText(message);
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		WebSocket.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		WebSocket.onlineCount--;
	}

}
