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
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
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
        for (String key1 : invertedFile.keySet()) {
            System.out.println("word: " + key1);
        }

        crawlerDB.updateWordsDB(invertedFile, DFsPerDocs);
    }

    private static void startIndexing() {
        FindIterable<Document> urls = urlsCollection.find().projection(Projections.include("URL"));
        Long l = urlsCollection.countDocuments();
        allDocsCount = l.doubleValue();
        MongoCursor<Document> urlsIterator = urls.iterator();
        try {
            int counter = 0;
            while (urlsIterator.hasNext() && counter++ < 500) {
                Document dbDocument = urlsIterator.next();
                String url = dbDocument.getString("URL");
                org.jsoup.nodes.Document jsoupDoc = getDocFromUrl(url);
                // Get all html elements
                if (jsoupDoc == null) continue;

                String title = jsoupDoc.title();
                String docText = jsoupDoc.text();

                processElements(jsoupDoc.getAllElements(), title, url, docText);
//                Elements allElements = jsoupDoc.getAllElements();

                // Loop over all elements

            }
        } finally {
            urlsIterator.close();
        }

    }


    private static void processElements(Elements allElements, String title, String url, String docText){
        int totWordsInDoc = 0;
        for (Element e : allElements) {
            String tag = e.tagName();
            String text = WordsProcessor.withoutStopWords(e.ownText());

            if (tag.equals("title") || tag.equals("p") || tag.equals("h1") || tag.equals("h2") || tag.equals("h3")
                    || tag.equals("h4") || tag.equals("h5") || tag.equals("h6") || tag.equals("td") || tag.equals("li")
                    || tag.equals("span")) {
                for (String word : text.split(" ")) {
                    int priority = 1; //least priority means more important

                    if (tag == "title") {
                        priority = 5;
                    } else if (tag == "h1" || tag == "h2" || tag == "h3") {
                        priority = 4;
                    } else if (tag == "h4" || tag == "h5" || tag == "h6") {
                        priority = 3;
                    } else if (tag == "p") {
                        priority = 2;
                    }

                    int index = text.indexOf(word);
                    int count = 0;
                    ArrayList<Integer> occurrences = new ArrayList<>();
                    while (index >= 0) {
                        count++;
                        occurrences.add(index);
                        index = text.indexOf(word, index + 1);
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
                            info.setOccurrences(occurrences);
                            info.setPriority(Math.max(info.getPriority(), priority));
                            invertedFile.get(word).put(url, info);
                        } else {
                            DFsPerDocs.put(word, DFsPerDocs.get(word) + 1 / allDocsCount);
                            DocData info = new DocData();
                            info.setTermFrequency(1);
                            info.setOccurrences(occurrences);
                            info.setTitle(title);
                            info.setPriority(priority);
                            invertedFile.get(word).put(url, info);
                        }
                    } else {
                        DFsPerDocs.put(word, 1 / allDocsCount);
                        HashMap<String, DocData> docHash = new HashMap<>();
                        DocData info = new DocData();
                        info.setTitle(title);
                        info.setOccurrences(occurrences);
                        info.setPriority(priority);
                        info.setTermFrequency(1);

                        docHash.put(url, info);
                        invertedFile.put(word, docHash);
                    }
                    System.out.println(word + ": " + invertedFile.get(word).get(url).getTermFrequency());
                }
                processElements(e.children(), title, url, docText);
            }

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
