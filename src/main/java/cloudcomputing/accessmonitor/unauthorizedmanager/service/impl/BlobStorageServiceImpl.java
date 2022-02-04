package cloudcomputing.accessmonitor.unauthorizedmanager.service.impl;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.StorageConstants.BLOB_STORAGE_CONNECTION_STRING;

import cloudcomputing.accessmonitor.unauthorizedmanager.service.BlobStorageService;
import com.azure.cosmos.implementation.Strings;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BlobStorageServiceImpl implements BlobStorageService {

  @Override
  public String readBlob(String blobName, String containerName) {
    try {
      BlobContainerClient accessMonitorBlobClient = new BlobServiceClientBuilder().connectionString(BLOB_STORAGE_CONNECTION_STRING)
        .buildClient()
        .getBlobContainerClient(containerName);
      BlobClient blobClient = accessMonitorBlobClient.getBlobClient(blobName);
      byte[] bytesBlob = blobClient.downloadContent().toBytes();
      return new String(Base64.getEncoder().encode(bytesBlob), StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
      return Strings.Emtpy;
    }
  }

}
