package dev.apt.searchengine.Indexer;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import dev.apt.searchengine.Crawler.CrawlerDB;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Indexer {
    private static MongoCollection<Document> urlsCollection;
    private static HashMap<String, HashMap<String, DocData>> invertedFile;



    public static void main(String[] args) {
        CrawlerDB crawlerDB = new CrawlerDB();
        MongoDatabase db = crawlerDB.getDatabase();
        urlsCollection = crawlerDB.getUrlsCollection();
        //MongoCollection<Document> wordsCollection = crawlerDB.getWordsCollection();
        startIndexing();
    }

    private static void startIndexing () {
        FindIterable<Document> urls = urlsCollection.find().projection(Projections.include("URL"));

        for (Document dbDocument : urls) {
            int totWordsInDoc = 0;
            String url = dbDocument.getString("URL");
            org.jsoup.nodes.Document jsoupDoc = getDocFromUrl(url);
            // Get all html elements
            assert jsoupDoc != null;
            Elements allElements = jsoupDoc.getAllElements();

            // Loop over all elements
            for (Element e : allElements) {
                String tag = e.tagName();
                String text = WordsProcessor.withoutStopWords(e.text());
                if (tag.equals("title") || tag.equals("p") || tag.equals("h1") || tag.equals("h2") || tag.equals("h3")
                        || tag.equals("h4") || tag.equals("h5") || tag.equals("h6") ||  tag.equals("td") || tag.equals("li")
                        || tag.equals("span")) {
                    for (String word : text.split(" ")) {
                        word = WordsProcessor.wordStemmer(word);
                        totWordsInDoc++;
                        if (invertedFile.containsKey(word)){

                        }
                    }
                }
            }
            System.out.println("Url: " + url);
        }
    }

    private static org.jsoup.nodes.Document getDocFromUrl(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("Error in reading file:  \nFound in db but not in file system");
            return null;
        }
    }
}
