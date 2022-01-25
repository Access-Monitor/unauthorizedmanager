package cloudcomputing.accessmonitor.unauthorizedmanager.service.impl;

import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.ADMINISTRATORS_CONTAINER_NAME;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.COSMOSDB_ENDPOINT;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.COSMOSDB_SUBSCRIPTION_KEY;
import static cloudcomputing.accessmonitor.unauthorizedmanager.constants.DatabaseConstants.DATABASE_NAME;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.Administrator;
import cloudcomputing.accessmonitor.unauthorizedmanager.service.AdministratorPersistenceService;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

public class AdministratorPersistenceServiceImpl implements AdministratorPersistenceService {

  private final CosmosContainer container;

  public AdministratorPersistenceServiceImpl() {
    CosmosClient client = new CosmosClientBuilder().endpoint(COSMOSDB_ENDPOINT)
      .key(COSMOSDB_SUBSCRIPTION_KEY)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .buildClient();
    CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(DATABASE_NAME);
    CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());
    CosmosContainerResponse containerResponse =
      database.createContainerIfNotExists(new CosmosContainerProperties(ADMINISTRATORS_CONTAINER_NAME, "/emailAddress"));
    container = database.getContainer(containerResponse.getProperties().getId());
  }

  @Override
  public CosmosPagedIterable<Administrator> readAll() {
    return container.queryItems("SELECT * FROM c", new CosmosQueryRequestOptions(), Administrator.class);
  }

}
