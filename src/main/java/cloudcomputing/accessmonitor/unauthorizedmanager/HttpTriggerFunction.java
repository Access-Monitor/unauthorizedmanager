package cloudcomputing.accessmonitor.unauthorizedmanager;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.MAIL_SUBJECT;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.AdministratorPersistenceService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.MailService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.UnauthorizedAccessPersistenceService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.AdministratorPersistenceServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.MailServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.UnauthorizedAccessPersistenceServiceImpl;
import com.google.gson.Gson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.mail.MessagingException;

public class HttpTriggerFunction {

  private final UnauthorizedAccessPersistenceService unauthorizedAccessPersistenceService =
    new UnauthorizedAccessPersistenceServiceImpl();
  private final AdministratorPersistenceService administratorPersistenceService = new AdministratorPersistenceServiceImpl();
  private final MailService mailService = new MailServiceImpl();

  @FunctionName("unauthorized")
  public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
                                   HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
    context.getLogger().info("Java HTTP trigger processed a request.");

    if (request.getBody().isPresent()) {
      UnauthorizedDetection unauthorizedDetection = new Gson().fromJson(request.getBody().get(), UnauthorizedDetection.class);
      unauthorizedDetection.setDetectionTime(LocalDateTime.now());
      unauthorizedAccessPersistenceService.createDetection(unauthorizedDetection);
      administratorPersistenceService.readAll()
        .stream()
        .forEach(admin -> notifyAdministrator(unauthorizedDetection, admin.getEmailAddress()));
      return request.createResponseBuilder(HttpStatus.OK).build();
    }
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is mandatory").build();
  }

  private void notifyAdministrator(UnauthorizedDetection unauthorizedDetection, String destinationAddress) {
    try {
      mailService.withDestinationAddress(destinationAddress)
        .withSubject(MAIL_SUBJECT)
        .withBodyText(unauthorizedDetection.getDetectionTime() + " - Rilevato accesso non autorizzato")
        .withAttachment(createAttachment(unauthorizedDetection))
        .send();
    } catch (MessagingException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private File createAttachment(UnauthorizedDetection unauthorizedDetection) throws IOException {
    File attachment = File.createTempFile(unauthorizedDetection.getId(), ".jpg");
    try (FileOutputStream fileOutputStream = new FileOutputStream(attachment)) {
      fileOutputStream.write(unauthorizedDetection.getBlobContent());
    }
    return attachment;
  }
}
