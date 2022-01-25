package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.Administrator;
import com.azure.cosmos.util.CosmosPagedIterable;

public interface AdministratorPersistenceService {

  CosmosPagedIterable<Administrator> readAll();
}
