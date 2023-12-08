import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AlbumResponse {
  private String albumId;
  private String imageSize;

  // Constructor
  public AlbumResponse() {
    super();
  }

  public AlbumResponse(String albumID, String imageSize) {
    this.albumId = albumID;
    this.imageSize = imageSize;
  }

}
