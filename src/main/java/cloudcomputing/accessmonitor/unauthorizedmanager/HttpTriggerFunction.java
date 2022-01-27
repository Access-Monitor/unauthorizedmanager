package cloudcomputing.accessmonitor.unauthorizedmanager;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.FROM_MAIL_ADDRESS;
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
import com.sendgrid.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;
import javax.mail.MessagingException;

public class HttpTriggerFunction {

  private final UnauthorizedAccessPersistenceService unauthorizedAccessPersistenceService =
    new UnauthorizedAccessPersistenceServiceImpl();
  private final AdministratorPersistenceService administratorPersistenceService = new AdministratorPersistenceServiceImpl();
  private final MailService mailService = new MailServiceImpl();

  @FunctionName("unauthorized")
  public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
                                   HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
    Logger logger = context.getLogger();
    logger.info("Java HTTP trigger processed a request.");

    if (request.getBody().isPresent()) {
      UnauthorizedDetection unauthorizedDetection = new Gson().fromJson(request.getBody().get(), UnauthorizedDetection.class);
      unauthorizedDetection.setDetectionTime(LocalDateTime.now());
      logger.info(String.format("Unauthorized detection with faceId: %s, filename: %s", unauthorizedDetection.getFaceId(),
        unauthorizedDetection.getId()));

      unauthorizedAccessPersistenceService.createDetection(unauthorizedDetection);
      logger.info("Registered unauthorized detection");

      administratorPersistenceService.readAll()
        .stream()
        .map(admin -> notifyAdministrator(unauthorizedDetection, admin.getEmailAddress()))
        .findFirst()
        .ifPresentOrElse(
          response -> logger.info("MAIL RESPONSE: status code: " + response.getStatusCode() + " body: " + response.getBody()),
          () -> logger.info("ERROR, no response received from mail sender"));

      return request.createResponseBuilder(HttpStatus.OK).build();
    }
    logger.info("Request body is not present");
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is mandatory").build();
  }

  private Response notifyAdministrator(UnauthorizedDetection unauthorizedDetection, String destinationAddress) {
    try {
      return mailService.withSourceAddress(FROM_MAIL_ADDRESS)
        .withDestinationAddress(destinationAddress)
        .withSubject(MAIL_SUBJECT)
        .withBodyText(unauthorizedDetection.getDetectionTime() + " - Rilevato accesso non autorizzato")
        .withAttachment(unauthorizedDetection.getBlobContent())
        .send();
    } catch (MessagingException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
