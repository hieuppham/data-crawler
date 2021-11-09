
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;


public class DataCrawler {
    private final int MAX_DEPTH = 7;
    private HashSet<String> list;
    private Jedis jedis;
    final private String rootUrl = "https://reviewdoan.com/";
    private int score;
    private List<String[]> keysAndCoors;

    public DataCrawler() {
        list = new HashSet<>();
        jedis = new Jedis("redis://default:1259pjQF8MZ1i8cvcahZXegQifZIN2Hy@redis-19316.c292.ap-southeast-1-1.ec2.cloud.redislabs.com:19316"); 
        /* jedis = new Jedis("redis://127.0.0.1:6379");*/
        score = 0;
        keysAndCoors = this.readFile();
    }

    // key, lat, lng
    public List<String[]> readFile() {
        String fileName = "C:\\Users\\phamt\\IdeaProjects\\untitled\\location.csv";
        List<String[]> r = null;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            r = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
        return r;
    }

    public void getData(String url, int depth) throws IOException {
        if (!list.contains(url) && depth < MAX_DEPTH && url.indexOf(rootUrl) == 0) {
            list.add(url);
            depth++;
            System.out.println(depth);
            Document doc = Jsoup.connect(url).get();

            Elements posts = doc.select("li.post-item, li.widget-single-post-item");
            posts.forEach(post -> {
                String link = post.selectFirst("a[href]").attr("abs:href");
                String date = post.selectFirst("span.date").text();
                key_date.put(link, date);
            });

            if (url.endsWith(".html")) {
                Post post = new Post();
                post.setScore(score);

                String key = url.substring(url.lastIndexOf("/") + 1, url.indexOf(".html"));
                post.setKey(key);

                Element header = doc.select("header.entry-header-outer").first();
                Elements tags = header.select("a.post-cat");
                ArrayList<String> tagList = new ArrayList<>();
                tags.forEach(element -> tagList.add(element.text()));
                post.setTags(tagList);

                Element title = header.select("h1.post-title").first();
                post.setTitle(title.text());

                Element main = doc.select("div.entry-content").first();

                ArrayList<String> imgList = new ArrayList<>();
                Elements imgs = main.select("img[src]");
                imgs.forEach(element -> {
                    String src = element.attr("src");
                    if (src.indexOf("https://static.xx.fbcdn.net/") == -1) {
                        imgList.add(src);
                    }
                });

                ArrayList<String> textList = new ArrayList<>();

                if (texts.size() != 0)
                    texts.forEach(element -> {
                        String text = element.text();
                        if (text.length() != 0) textList.add(text);
                    });

                keysAndCoors.forEach(keyAndCoor -> {
                    if (keyAndCoor[0].equals(key)) {
                        double lng = Double.parseDouble(keyAndCoor[2].trim());
                        double lat = Double.parseDouble(keyAndCoor[1].trim());
                        double[] coordinates = new double[]{lng, lat};
                        post.setCoordinates(coordinates);
                    }
                });

                if (!textList.isEmpty() && !imgList.isEmpty()) {
                    post.setTexts(textList);
                    post.setImages(imgList);
                    String json = new Gson().toJson(post, Post.class);
                    jedis.zadd("food-review", score, json);
                    jedis.close();
                    score++;
                }
            }
            Elements links = doc.select("a[href]");

            int finalDepth = depth;
            links.forEach(link -> {
                String subUrl = link.attr("abs:href");
                try {
                    getData(subUrl, finalDepth);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {
        DataCrawler DataCrawler = new DataCrawler();
        DataCrawler.getData(DataCrawler.rootUrl, 0);
    }

}