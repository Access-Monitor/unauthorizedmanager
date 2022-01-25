package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;

public interface UnauthorizedAccessPersistenceService {

  void createDetection(UnauthorizedDetection unauthorizedDetection);
}
