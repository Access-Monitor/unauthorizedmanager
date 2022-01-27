package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import java.io.IOException;
import javax.mail.MessagingException;

public interface MailService {

  MailService withSubject(String subject);

  MailService withBodyText(String bodyText);

  MailService withDestinationAddress(String destinationAddress);

  MailService withSourceAddress(String sourceAddress);

  MailService withAttachment(byte[] attachment);

  void send() throws MessagingException, IOException;
}
