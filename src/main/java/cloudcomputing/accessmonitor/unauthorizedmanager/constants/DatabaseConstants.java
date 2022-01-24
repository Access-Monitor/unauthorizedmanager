package cloudcomputing.accessmonitor.unauthorizedmanager.constants;

public class DatabaseConstants {

  public static final String COSMOSDB_SUBSCRIPTION_KEY = System.getenv("CosmosDBKey");
  public static final String COSMOSDB_ENDPOINT = System.getenv("CosmosDBEndpoint");
  public static final String DATABASE_NAME = "AccessMonitorDb";
  public static final String UNAUTHORIZED_CONTAINER_NAME = "UnauthorizedMembers";
}
