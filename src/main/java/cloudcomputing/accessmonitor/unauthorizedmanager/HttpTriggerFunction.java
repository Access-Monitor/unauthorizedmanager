package cloudcomputing.accessmonitor.unauthorizedmanager;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.MIN_TIME_FOR_NOTIFICATION;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.FROM_MAIL_ADDRESS;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.MailConstants.MAIL_SUBJECT;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.AdministratorPersistenceService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.FaceAPIService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.MailService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.UnauthorizedAccessPersistenceService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.AdministratorPersistenceServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.FaceAPIServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.MailServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.UnauthorizedAccessPersistenceServiceImpl;
import com.azure.cosmos.models.CosmosItemResponse;
import com.google.gson.Gson;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.VerifyResult;
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
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

public class HttpTriggerFunction {

  private final UnauthorizedAccessPersistenceService unauthorizedAccessPersistenceService =
    new UnauthorizedAccessPersistenceServiceImpl();
  private final AdministratorPersistenceService administratorPersistenceService = new AdministratorPersistenceServiceImpl();
  private final MailService mailService = new MailServiceImpl();
  private final FaceAPIService faceAPIService = new FaceAPIServiceImpl();

  @FunctionName("unauthorized")
  public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
                                   HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
    Logger logger = context.getLogger();
    logger.info("Java HTTP trigger processed a request.");

    if (request.getBody().isPresent()) {
      UnauthorizedDetection unauthorizedDetection = new Gson().fromJson(request.getBody().get(), UnauthorizedDetection.class);
      unauthorizedDetection.setDetectionTime(LocalDateTime.now(ZoneOffset.UTC));
      logger.info(String.format("Unauthorized detection with faceId: %s, filename: %s", unauthorizedDetection.getFaceId(),
        unauthorizedDetection.getId()));

      Optional<Boolean> faceAlreadyNotified =
        unauthorizedAccessPersistenceService.lastNotifiedDetections().stream().map(lastDetection -> {
          logger.info(
            String.format("Notified detection in past %s minutes found: faceId %s, filename %s", MIN_TIME_FOR_NOTIFICATION,
              lastDetection.getFaceId(), lastDetection.getId()));

          HttpResponse<String> verifyResponse =
            faceAPIService.faceVerify(lastDetection.getFaceId(), unauthorizedDetection.getFaceId());
          return new Gson().fromJson(verifyResponse.body(), VerifyResult.class).isIdentical();
        }).filter(identical -> identical).findAny();

      faceAlreadyNotified.ifPresentOrElse(
        face -> logger.log(Level.WARNING, String.format("FaceId %s has been already notified", unauthorizedDetection.getFaceId())),
        () -> {
          notifyUnauthorizedDetection(unauthorizedDetection, logger);
          registerDetection(unauthorizedDetection, logger);
        });

      return request.createResponseBuilder(HttpStatus.OK).build();
    }
    logger.log(Level.SEVERE, "Request body is not present");
    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is mandatory").build();
  }

  private void registerDetection(UnauthorizedDetection unauthorizedDetection, Logger logger) {
    CosmosItemResponse<UnauthorizedDetection> createDetectionResponse =
      unauthorizedAccessPersistenceService.createDetection(unauthorizedDetection);
    logger.log(Level.INFO, String.format("Create Detection Response with status: %s", createDetectionResponse.getStatusCode()));
  }

  private void notifyUnauthorizedDetection(UnauthorizedDetection unauthorizedDetection, Logger logger) {
    logger.info(String.format("Notifying unauthorized detection with faceId: %s, filename: %s", unauthorizedDetection.getFaceId(),
      unauthorizedDetection.getId()));
    administratorPersistenceService.readAll()
      .forEach(admin -> buildAndSendMail(unauthorizedDetection, admin.getEmailAddress(), logger));
    unauthorizedDetection.setNotified(true);
  }

  private void buildAndSendMail(UnauthorizedDetection unauthorizedDetection, String destinationAddress, Logger logger) {
    try {
      logger.info(String.format("Attempting to send unauthorized mail notification to admin %s", destinationAddress));
      Response sendResponse = mailService.withSourceAddress(FROM_MAIL_ADDRESS)
        .withDestinationAddress(destinationAddress)
        .withSubject(MAIL_SUBJECT)
        .withBodyText(unauthorizedDetection.getDetectionTime() + " - Rilevato accesso non autorizzato")
        .withAttachment(unauthorizedDetection.getBlobContent())
        .send();
      logger.info(
        String.format("MAIL RESPONSE: status code: %s and body: %s", sendResponse.getStatusCode(), sendResponse.getBody()));
    } catch (MessagingException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
