package ktDtu;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

//�����ʼ���
	public class EmailManager {

		private Properties props; // ϵͳ����
		private Session session; // �ʼ��Ự����
		private MimeMessage mimeMsg; // MIME�ʼ�����
		private Multipart mp; // Multipart����,�ʼ�����,����,���������ݾ���ӵ����к�������MimeMessage����

		/**
		 * Constructor
		 * 
		 * @param smtp
		 *            �ʼ����ͷ�����
		 */
		public EmailManager() {
			props = System.getProperties();
			props.put("mail.smtp.auth", "false");
			session = Session.getDefaultInstance(props, null);
			session.setDebug(false);
			mimeMsg = new MimeMessage(session);
			mp = new MimeMultipart();
		}

		/**
		 * Constructor
		 * 
		 * @param smtp
		 *            �ʼ����ͷ�����
		 */
		public EmailManager(String smtp, String username, String password) {
			props = System.getProperties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.host", smtp);
			props.put("username", username);
			props.put("password", password);
			session = Session.getDefaultInstance(props, null);
			session.setDebug(false);
			mimeMsg = new MimeMessage(session);
			mp = new MimeMultipart();
		}

		/**
		 * �����ʼ�
		 */
		public boolean sendMail(String from, String[] to, String[] copyto, String subject, String content,
				String filename) {
			try {
				// ���÷�����
				mimeMsg.setFrom(new InternetAddress(from));
				// ���ý�����
				// log("to.length=" + to.length);
				for (int i = 0; i < to.length; i++) {
					// mimeMsg.setRecipients(Message.RecipientType.TO,
					// InternetAddress.parse(to[i]));
					mimeMsg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to[i]));
				}
				// ���ó�����
				if (copyto != null) {
					for (int i = 0; i < copyto.length; i++) {
						mimeMsg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(copyto[i]));
					}
				}

				// ��������
				mimeMsg.setSubject(subject);
				// ��������
				BodyPart bp = new MimeBodyPart();
				bp.setContent(content, "text/html;charset=utf-8");
				mp.addBodyPart(bp);
				// ���ø���
				if (filename.length() != 0) {
					bp = new MimeBodyPart();
					FileDataSource fileds = new FileDataSource(filename);
					bp.setDataHandler(new DataHandler(fileds));
					bp.setFileName(MimeUtility.encodeText(fileds.getName(), "UTF-8", "B"));
					mp.addBodyPart(bp);
				}

				mimeMsg.setContent(mp);
				mimeMsg.saveChanges();
				// �����ʼ�
				if (props.get("mail.smtp.auth").equals("true")) {
					Transport transport = session.getTransport("smtp");
					transport.connect((String) props.get("mail.smtp.host"), (String) props.get("username"),
							(String) props.get("password"));
					transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));
					if (copyto != null) {
						transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.CC));
					}

					transport.close();
				} else {
					Transport.send(mimeMsg);
				}
				// System.out.println("�ʼ����ͳɹ�");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}

	}
