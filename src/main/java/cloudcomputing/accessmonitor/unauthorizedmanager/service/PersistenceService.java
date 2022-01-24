package cloudcomputing.accessmonitor.unauthorizedmanager.service;

import cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence.UnauthorizedDetection;

public interface PersistenceService {

  void createDetection(UnauthorizedDetection unauthorizedDetection);
}
