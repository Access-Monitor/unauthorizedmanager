package cloudcomputing.accessmonitor.unauthorizedmanager;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.FROM_MAIL_ADDRESS;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.MAIL_SUBJECT;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.StorageConstants.ACCESSMONITORBLOB_CONTAINER;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.AdministratorPersistenceService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.BlobStorageService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.MailService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.AdministratorPersistenceServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.BlobStorageServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.MailServiceImpl;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

public class HttpTriggerFunction {

  private final AdministratorPersistenceService administratorPersistenceService = new AdministratorPersistenceServiceImpl();
  private final MailService mailService = new MailServiceImpl();
  private final BlobStorageService blobStorageService = new BlobStorageServiceImpl();

  @FunctionName("unauthorized")
  public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
                                   HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
    Logger logger = context.getLogger();
    logger.info("Java HTTP trigger processed a request.");

    if (request.getBody().isPresent()) {
      UnauthorizedDetection unauthorizedDetection = new Gson().fromJson(request.getBody().get(), UnauthorizedDetection.class);
      logger.info(String.format("Unauthorized detection with faceId: %s, filename: %s", unauthorizedDetection.getFaceId(),
        unauthorizedDetection.getId()));

      notifyUnauthorizedDetection(unauthorizedDetection, logger);

      return request.createResponseBuilder(HttpStatus.OK).build();
    }

    logger.log(Level.SEVERE, "Request body is not present");
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is mandatory").build();

  }

  private void notifyUnauthorizedDetection(UnauthorizedDetection unauthorizedDetection, Logger logger) {
    logger.info(String.format("Notifying unauthorized detection with faceId: %s, filename: %s", unauthorizedDetection.getFaceId(),
      unauthorizedDetection.getId()));
    administratorPersistenceService.readAll()
      .forEach(admin -> buildAndSendMail(unauthorizedDetection, admin.getEmailAddress(), logger));
  }

  private void buildAndSendMail(UnauthorizedDetection unauthorizedDetection, String destinationAddress, Logger logger) {
    try {
      logger.info(String.format("Attempting to send unauthorized mail notification to admin %s", destinationAddress));

      String attachmentBase64 = blobStorageService.readBlob(unauthorizedDetection.getFilename(), ACCESSMONITORBLOB_CONTAINER);
      Response sendResponse = mailService.withSourceAddress(FROM_MAIL_ADDRESS)
        .withDestinationAddress(destinationAddress)
        .withSubject(MAIL_SUBJECT)
        .withBodyText(unauthorizedDetection.getDetectionTime() + " - Rilevato accesso non autorizzato")
        .withAttachment(attachmentBase64)
        .send();

      logger.info(
        String.format("MAIL RESPONSE: status code: %s and body: %s", sendResponse.getStatusCode(), sendResponse.getBody()));
    } catch (MessagingException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
