package cloudcomputing.accessmonitor.unauthorizedmanager.service.impl;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.SEND_GRID_API_KEY;

import cloudcomputing.accessmonitor.unauthorizedmanager.service.MailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;

public class MailServiceImpl implements MailService {

  private final SendGrid sg;
  private String subject;
  private String bodyText;
  private String destinationAddress;
  private String sourceAddress;
  private String attachment;

  public MailServiceImpl() {
    sg = new SendGrid(SEND_GRID_API_KEY);
  }

  @Override
  public Response send() {

    Mail mail = new Mail(new Email(sourceAddress), subject, new Email(destinationAddress), new Content("text/plain", bodyText));
    mail.addAttachments(createAttachment());

    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      return sg.api(request);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public MailService withSourceAddress(String sourceAddress) {
    this.sourceAddress = sourceAddress;
    return this;
  }

  @Override
  public MailService withDestinationAddress(String destinationAddress) {
    this.destinationAddress = destinationAddress;
    return this;
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
  public MailService withAttachment(String attachment) {
    this.attachment = attachment;
    return this;
  }

  private Attachments createAttachment() {
    Attachments attachments = new Attachments();
    attachments.setContent(attachment);
    attachments.setType("image/jpeg");
    attachments.setFilename("detection.jpeg");
    attachments.setDisposition("attachment");
    return attachments;
  }

}
