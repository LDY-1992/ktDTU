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
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 *                 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)

public class WebSocket {
	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;
	private Session session;

	/**
	 * 连接建立成功调用的方法
	 * 
	 * @param session
	 *            可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
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
		addOnlineCount(); // 在线数加1
		System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		// webSocketMap.remove(this.key); // 从set中删除
		subOnlineCount(); // 在线数减1
		System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * @param message
	 *            客户端发送过来的消息
	 * @param session
	 *            可选的参数
	 */
	@OnMessage
	public void onMessage(String message, Session session, EndpointConfig config) {
		System.out.println("来自客户端的消息:" + message);
		// 处理信息
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
	 * 发生错误时调用
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("发生错误");
		error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
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
