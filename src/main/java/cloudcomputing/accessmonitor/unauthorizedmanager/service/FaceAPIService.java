package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import java.net.http.HttpResponse;

public interface FaceAPIService {

  HttpResponse<String> faceVerify(String faceId1, String faceId2);
}
