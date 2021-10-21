import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Post {
    private int score;
    private String key;
    private String title;
    private Date date;
    private List<String> texts;
    private List<String> images;
    private List<String> tags;
    private double[] coordinates;
}
