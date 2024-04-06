package dev.apt.searchengine.Indexer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.apt.searchengine.Crawler.CrawlerDB;

public class Indexer {
    public static void main(String[] args) {
        CrawlerDB crawlerDB = new CrawlerDB();
        MongoDatabase db = crawlerDB.getDatabase();
        MongoCollection collection = crawlerDB.getCollection();

    }


}
