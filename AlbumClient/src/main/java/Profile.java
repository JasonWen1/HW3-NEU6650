import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Profile {
  private String artist;
  private String title;
  private String year;
}
