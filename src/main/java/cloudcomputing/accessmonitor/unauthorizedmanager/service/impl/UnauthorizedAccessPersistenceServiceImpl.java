package cloudcomputing.accessmonitor.unauthorizedmanager.service.impl;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.COSMOSDB_ENDPOINT;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.COSMOSDB_SUBSCRIPTION_KEY;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.DATABASE_NAME;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.UNAUTHORIZED_CONTAINER_NAME;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.UnauthorizedAccessPersistenceService;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;

public class UnauthorizedAccessPersistenceServiceImpl implements UnauthorizedAccessPersistenceService {

  private final CosmosContainer container;

  public UnauthorizedAccessPersistenceServiceImpl() {
    CosmosClient client = new CosmosClientBuilder().endpoint(COSMOSDB_ENDPOINT)
      .key(COSMOSDB_SUBSCRIPTION_KEY)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .buildClient();
    CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(DATABASE_NAME);
    CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());
    CosmosContainerResponse containerResponse =
      database.createContainerIfNotExists(new CosmosContainerProperties(UNAUTHORIZED_CONTAINER_NAME, "/faceId"));
    container = database.getContainer(containerResponse.getProperties().getId());
  }

  @Override
  public void createDetection(UnauthorizedDetection unauthorizedDetection) {
    container.createItem(unauthorizedDetection, new PartitionKey(unauthorizedDetection.getFaceId()),
      new CosmosItemRequestOptions());
  }
}
