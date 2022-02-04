package cloudcomputing.accessmonitor.unauthorizedmanager.service;

public interface BlobStorageService {

  String readBlob(String blobName, String containerName);
}
