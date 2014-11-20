package hci.biominer.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailUtil {


	public static String sendMail(String [] recipient, String body, String subject) {
		String status = "";
		String from = "DoNotReply@hci.utah.edu";

		Properties properties = System.getProperties();

		properties.put("mail.smtp.host", "hci-mail.hci.utah.edu");
		Session session = Session.getDefaultInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			for (int ii = 0; ii < recipient.length; ii++) {
				message.addRecipient(Message.RecipientType.TO,new InternetAddress(recipient[ii]));
			}
			message.setSubject(subject);
			message.setContent(body,"text/html");
			Transport.send(message);
			status = null;
		} catch (MessagingException mex) {
			status =  mex.toString();
		}
		
		return status;

	}


}
