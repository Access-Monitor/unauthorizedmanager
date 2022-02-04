package cloudcomputing.accessmonitor.unauthorizedmanager.constants;

public class StorageConstants {

  public static final String COSMOSDB_SUBSCRIPTION_KEY = System.getenv("CosmosDBKey");
  public static final String COSMOSDB_ENDPOINT = System.getenv("CosmosDBEndpoint");
  public static final String DATABASE_NAME = "AccessMonitorDb";
  public static final String ADMINISTRATORS_CONTAINER_NAME = "Administrators";

  public static final String BLOB_STORAGE_CONNECTION_STRING = System.getenv("BlobStorageConnectionString");
  public static final String ACCESSMONITORBLOB_CONTAINER = "accessmonitorblob";

}
