package dev.apt.searchengine.Indexer;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import dev.apt.searchengine.Crawler.CrawlerDB;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.Objects;

public class Indexer {
    private static MongoCollection<Document> urlsCollection;
    private static HashMap<String, HashMap<String, DocData>> invertedFile = new HashMap<>();
    private static HashMap<String, Double> DFsPerDocs = new HashMap<>();
    private static Double allDocsCount = 0.0;


    public static void main(String[] args) {
        CrawlerDB crawlerDB = new CrawlerDB();
        MongoDatabase db = crawlerDB.getDatabase();
        urlsCollection = crawlerDB.getUrlsCollection();
        //MongoCollection<Document> wordsCollection = crawlerDB.getWordsCollection();
        startIndexing();
        crawlerDB.updateWordsDB(invertedFile, DFsPerDocs);
    }

    private static void startIndexing() {
        FindIterable<Document> urls = urlsCollection.find().projection(Projections.include("URL"));
        Long l = urlsCollection.countDocuments();
        allDocsCount = l.doubleValue();
        MongoCursor<Document> urlsIterator = urls.iterator();
        try {
            while (urlsIterator.hasNext()) {
                Document dbDocument = urlsIterator.next();
                int totWordsInDoc = 0;
                String url = dbDocument.getString("URL");
                org.jsoup.nodes.Document jsoupDoc = getDocFromUrl(url);
                // Get all html elements
                if (jsoupDoc == null) continue;
                Elements allElements = jsoupDoc.getAllElements();
                String title = jsoupDoc.title();

                // Loop over all elements
                for (Element e : allElements) {
                    String tag = e.tagName();
                    String text = WordsProcessor.withoutStopWords(e.text());

                    if (tag.equals("title") || tag.equals("p") || tag.equals("h1") || tag.equals("h2") || tag.equals("h3")
                            || tag.equals("h4") || tag.equals("h5") || tag.equals("h6") || tag.equals("td") || tag.equals("li")
                            || tag.equals("span")) {
                        for (String word : text.split(" ")) {
                            int priority = 0; //least priority means more important

                            if (tag == "title") {
                                priority = 4;
                            } else if (tag == "h1" || tag == "h2" || tag == "h3") {
                                priority = 3;
                            } else if (tag == "h4" || tag == "h5" || tag == "h6") {
                                priority = 2;
                            } else if (tag == "p") {
                                priority = 1;
                            }

                            word = WordsProcessor.wordStemmer(word);
                            totWordsInDoc++;
                            if (word.isEmpty()) continue;
                            // if word found before in all documents
                            if (invertedFile.containsKey(word)) {

                                // if word found before in the same document
                                if (invertedFile.get(word).containsKey(url)) {
                                    DocData info = invertedFile.get(word).get(url);
                                    info.setTermFrequency(info.getTermFrequency() + 1);
                                    info.setPriority(Math.max(info.getPriority(), priority));
                                    invertedFile.get(word).put(url, info);
                                } else {
                                    DFsPerDocs.put(word, DFsPerDocs.get(word) + 1 / allDocsCount);
                                    DocData info = new DocData();
                                    info.setTermFrequency(1);
                                    info.setTitle(title);
                                    info.setPriority(priority);
                                    invertedFile.get(word).put(url, info);
                                }
                            } else {
                                DFsPerDocs.put(word, 1 / allDocsCount);
                                HashMap<String, DocData> docHash = new HashMap<>();
                                DocData info = new DocData();
                                info.setTitle(title);
                                info.setPriority(priority);
                                info.setTermFrequency(1);

                                docHash.put(url, info);
                                invertedFile.put(word, docHash);
                            }
                            System.out.println(word + ": " + invertedFile.get(word).get(url).getTermFrequency());
                        }
                    }
                }
            }
        } finally {
            urlsIterator.close();
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
