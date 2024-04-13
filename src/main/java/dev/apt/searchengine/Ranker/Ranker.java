package dev.apt.searchengine.Ranker;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import dev.apt.searchengine.Crawler.CrawlerDB;
import dev.apt.searchengine.Indexer.DocData;
import org.bson.Document;

import java.util.*;

public class Ranker {
    public static CrawlerDB db;
    public static MongoCollection<Document> words;
    public static ArrayList<String> queryWords;
    public static ArrayList<RankedDoc> rankedDocs;
    public static HashMap<String, RankedDoc> docHashMap;

    public static void main(String[] args) {
        db = new CrawlerDB();
        words = db.getWordsCollection();
        //queryWords = qw;
        queryWords = new ArrayList<>(Arrays.asList("compar", "web", "tool"));
        docHashMap = new HashMap<>();
        rank();
    }

    public static void rank() {
        rankedDocs = new ArrayList<>();

        for (String word : queryWords) {
            Document findQuery = new Document("Word", word);
            FindIterable<Document> documents = words.find(findQuery);
            Document doc = documents.first();

            if (doc != null) {
                ArrayList<Document> documentList = (ArrayList<Document>) doc.get("Documents");
                Double IDF = (Double) doc.get("IDF");

                /* Iterate over the embedded documents in the "Documents" array */
                for (Document embeddedDoc : documentList) {
                    Double score;
                    // Access fields of each embedded document
                    String url = embeddedDoc.getString("URL");
                    int termFrequency = embeddedDoc.getInteger("TermFrequency");
                    String title = embeddedDoc.getString("Title");
                    int priority = embeddedDoc.getInteger("Priority");

                    score = termFrequency * IDF * priority;
                    if (docHashMap.containsKey(url)) {
                        docHashMap.get(url).setScore(docHashMap.get(url).getScore() + score);
                    } else {
                        RankedDoc info = new RankedDoc(url, score, title, "");
                        rankedDocs.add(info);
                        docHashMap.put(url, info);
                    }
                }

            }
        }

        Collections.sort(rankedDocs, Comparator.comparingDouble(RankedDoc::getScore).reversed());

        for (RankedDoc rd : rankedDocs) {
            System.out.println(rd.getUrl() + " " + rd.getTitle());
        }
    }
};
