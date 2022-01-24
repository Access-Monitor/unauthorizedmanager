package cloudcomputing.accessmonitor.unauthorizedmanager.service.impl;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.FROM_MAIL_ADDRESS;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.FROM_MAIL_PWD;

import cloudcomputing.accessmonitor.unauthorizedmanager.service.MailService;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailServiceImpl implements MailService {

  private final Message message;
  private String subject;
  private String bodyText;
  private File attachment;
  private String destinationAddress;

  public MailServiceImpl() {
    Properties prop = initClientProperties();
    Session session = clientSignIn(prop);
    message = new MimeMessage(session);
  }

  @Override
  public void send() throws MessagingException, IOException {
    message.setFrom(new InternetAddress(FROM_MAIL_ADDRESS));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinationAddress));
    message.setSubject(subject);

    MimeBodyPart bodyTextPart = new MimeBodyPart();
    bodyTextPart.setContent(bodyText, "text/html; charset=utf-8");
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile(attachment);

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(bodyTextPart);
    multipart.addBodyPart(attachmentPart);

    message.setContent(multipart);
    Transport.send(message);
  }

  @Override
  public MailService withSubject(String subject) {
    this.subject = subject;
    return this;
  }

  @Override
  public MailService withBodyText(String bodyText) {
    this.bodyText = bodyText;
    return this;
  }

  @Override
  public MailService withDestinationAddress(String destinationAddress) {
    this.destinationAddress = destinationAddress;
    return this;
  }

  @Override
  public MailService withAttachment(File attachment) {
    this.attachment = attachment;
    return this;
  }

  private Session clientSignIn(Properties prop) {
    return Session.getInstance(prop, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(FROM_MAIL_ADDRESS, FROM_MAIL_PWD);
      }
    });
  }

  private Properties initClientProperties() {
    Properties prop = new Properties();
    prop.put("mail.smtp.host", "smtp.gmail.com");
    prop.put("mail.smtp.port", "587");
    prop.put("mail.smtp.auth", true);
    prop.put("mail.smtp.starttls.enable", "true");
    return prop;
  }
}
