package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;

public interface UnauthorizedAccessPersistenceService {

  CosmosItemResponse<UnauthorizedDetection> createDetection(UnauthorizedDetection unauthorizedDetection);

  CosmosPagedIterable<UnauthorizedDetection> lastNotifiedDetections();
}
