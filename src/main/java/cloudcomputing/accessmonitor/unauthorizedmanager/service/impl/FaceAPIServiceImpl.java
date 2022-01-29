package cloudcomputing.accessmonitor.unauthorizedmanager.service.impl;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.FaceAPIConstants.FACE_API_BASE_ENDPOINT;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.FaceAPIConstants.FACE_API_SUBSCRIPTION_KEY;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.FaceAPIConstants.OCP_APIM_SUBSCRIPTION_KEY_HEADER;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.api.FaceVerifyRequestBody;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.FaceAPIService;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class FaceAPIServiceImpl implements FaceAPIService {

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Override
  public HttpResponse<String> faceVerify(String faceId1, String faceId2) {
    try {
      FaceVerifyRequestBody faceVerifyRequestBody = new FaceVerifyRequestBody(faceId1, faceId2);
      HttpRequest httpRequest = buildBaseFaceAPIHttpRequest(FACE_API_BASE_ENDPOINT + "/face/v1.0/verify").POST(
        BodyPublishers.ofString(new Gson().toJson(faceVerifyRequestBody, FaceVerifyRequestBody.class))).build();
      return httpClient.send(httpRequest, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private Builder buildBaseFaceAPIHttpRequest(String endpoint) {
    return HttpRequest.newBuilder(URI.create(endpoint)).header(OCP_APIM_SUBSCRIPTION_KEY_HEADER, FACE_API_SUBSCRIPTION_KEY);
  }

}
