package cloudcomputing.accessmonitor.unauthorizedmanager.model.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;

public class UnauthorizedDetection {

  private String id;
  private String faceId;
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime detectionTime;
  private byte[] blobContent;

  public UnauthorizedDetection(String id, String faceId, LocalDateTime detectionTime, byte[] blobContent) {
    this.id = id;
    this.faceId = faceId;
    this.detectionTime = detectionTime;
    this.blobContent = blobContent;
  }

  public UnauthorizedDetection() {
  }

  public byte[] getBlobContent() {
    return blobContent;
  }

  public void setBlobContent(byte[] blobContent) {
    this.blobContent = blobContent;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFaceId() {
    return faceId;
  }

  public void setFaceId(String faceId) {
    this.faceId = faceId;
  }

  public LocalDateTime getDetectionTime() {
    return detectionTime;
  }

  public void setDetectionTime(LocalDateTime detectionTime) {
    this.detectionTime = detectionTime;
  }
}
