import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.util.Set;

public class Write {
    Jedis jedis;
    public Write(){
        jedis = new Jedis("redis://127.0.0.1:6379");
    }

    private List<String[]>  getKeyAndScore(){
        Set<String> set = jedis.zrange("food-review", 0L, 400L);
        List<String[]> list= new ArrayList<>();
        for(String json : set){
            Post post = new Gson().fromJson(json, Post.class);
            String[] strings = new String[] {String.valueOf(post.getScore()), ",", "", post.getKey()};
            list.add(strings);
        }
        return list;
    }

    public void csvWriterOneByOne(List<String[]> stringArray) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\phamt\\IdeaProjects\\untitled\\location2.csv"));
        for (String[] array : stringArray) {
            writer.writeNext(array);
        }
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        Write write = new Write();
        List<String[]> list = write.getKeyAndScore();
        write.csvWriterOneByOne(list);
        System.out.println("done");
    }
}
