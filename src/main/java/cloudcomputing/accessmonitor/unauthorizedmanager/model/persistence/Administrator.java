package cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence;

public class Administrator {

  private String id;
  private String emailAddress;

  public Administrator(String id, String emailAddress) {
    this.id = id;
    this.emailAddress = emailAddress;
  }

  public Administrator() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  @Override
  public String toString() {
    return "Administrator{" + "id='" + id + '\'' + ", emailAddress='" + emailAddress + '\'' + '}';
  }
}
