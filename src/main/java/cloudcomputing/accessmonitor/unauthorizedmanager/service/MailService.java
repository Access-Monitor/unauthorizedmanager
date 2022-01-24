package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public interface MailService {

  MailService withSubject(String subject);

  MailService withBodyText(String bodyText);

  MailService withDestinationAddress(String destinationAddress);

  MailService withAttachment(File attachment);

  void send() throws MessagingException, IOException;
}
