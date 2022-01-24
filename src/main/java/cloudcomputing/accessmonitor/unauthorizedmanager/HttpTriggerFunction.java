package cloudcomputing.accessmonitor.unauthorizedmanager;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.HttpConstants.Headers.FACE_ID_HEADER;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.HttpConstants.Headers.FILENAME_HEADER;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.MailService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.PersistenceService;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.MailServiceImpl;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.impl.PersistenceServiceCosmosDBImpl;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.mail.MessagingException;

public class HttpTriggerFunction {

  private final PersistenceService persistenceService = new PersistenceServiceCosmosDBImpl();
  private final MailService mailService = new MailServiceImpl();

  @FunctionName("unauthorized")
  public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION)
                                   HttpRequestMessage<Optional<String>> request, final ExecutionContext context)
    throws MessagingException, IOException {
    context.getLogger().info("Java HTTP trigger processed a request.");

    final String faceId = request.getQueryParameters().get(FACE_ID_HEADER);
    final String filename = request.getQueryParameters().get(FILENAME_HEADER);
    UnauthorizedDetection unauthorizedDetection = new UnauthorizedDetection(filename, faceId, LocalDateTime.now());
    persistenceService.createDetection(unauthorizedDetection);
    mailService.withDestinationAddress("leocapuano0@gmail.com")
      .withSubject("Oggetto Mail")
      .withBodyText("Rilevato " + unauthorizedDetection.getFaceId() + " alle ore " + unauthorizedDetection.getDetectionTime())
      .withAttachment(new File("D:\\A-Immagini\\3501.jpg"))
      .send();

    return request.createResponseBuilder(HttpStatus.OK).body("Hello").build();
  }
}
