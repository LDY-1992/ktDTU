package startListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ktDtu.TcpClass;
import ktDtu.UdpClass;

/**
 * Application Lifecycle Listener implementation class startSocket
 *
 */
@WebListener
public class startSocket implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public startSocket() {
        // TODO Auto-generated constructor stub
    	
    	TcpClass tcp_server =new TcpClass();
        tcp_server.start();
        System.out.println("开启TCP服务端");
        
        UdpClass udp_server=new UdpClass();
        udp_server.start();
        System.out.println("开启UDP服务端");
  
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }
	
}
