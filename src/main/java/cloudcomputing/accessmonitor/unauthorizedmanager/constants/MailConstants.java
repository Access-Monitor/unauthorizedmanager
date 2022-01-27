package cloudcomputing.accessmonitor.unauthorizedmanager.constants;

public class MailConstants {

  public static final String FROM_MAIL_ADDRESS = System.getenv("FromMailAddress");
  public static final String FROM_MAIL_PWD = System.getenv("FromMailPwd");
  public static final String MAIL_SUBJECT = "Rilevato accesso non autorizzato";
  public static final String SEND_GRID_API_KEY = System.getenv("SENDGRID_API_KEY");

}
