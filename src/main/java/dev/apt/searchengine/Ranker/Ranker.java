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



    public static ArrayList<RankedDoc> mainRanker(ArrayList<String> qw, HashMap<String, Double> popularityHashMap) {
        db = new CrawlerDB();
        words = db.getWordsCollection();
        queryWords = qw;
        docHashMap = new HashMap<>();
        rank(popularityHashMap);
        return rankedDocs;
    }



    public static HashMap<String, Double> calculatePopularity(HashMap<String, ArrayList<String>> urlsGraph) {
        HashMap<String, Double> pageRanks = new HashMap<>();

        for (String node : urlsGraph.keySet()) {
            pageRanks.put(node, 1 / (double) urlsGraph.size()); // Initialize all PageRank values to 1.0
        }

        //that is a function that calculates the converge
        /*private double diff(Map<String, Double> nextIter, Map<String, Double> prevIter) {
        double sum = 0;
        for(String s : nodeCounter) {
            sum += Math.abs(nextIter.get(s) - prevIter.get(s));
        }
        return(Math.sqrt(sum));
        }*/

        // Perform PageRank iterations
        int iterations = 15;
        double dampingFactor = 0.85; // Typical damping factor used in PageRank
        for (int i = 0; i < iterations; i++) {
            HashMap<String, Double> newPageRanks = new HashMap<>();
            double sumOfRanks = 0.0;


            // Contribution from incoming edges
            for (String incomingNode : urlsGraph.keySet()) {
                double newPageRank = (1 - dampingFactor); // Initial value (damping factor)
                if (urlsGraph.get(incomingNode) != null && !urlsGraph.get(incomingNode).isEmpty()) {
                    for (String outgoingNode : urlsGraph.get(incomingNode)) {
                        if (urlsGraph.containsKey((outgoingNode)))
                            newPageRank += dampingFactor * (pageRanks.get(outgoingNode) / urlsGraph.get(outgoingNode).size());
                    }
                }
                newPageRanks.put(incomingNode, newPageRank);
                sumOfRanks += newPageRank;
            }


            // Normalize PageRank values
            for (String node : newPageRanks.keySet()) {
                newPageRanks.put(node, newPageRanks.get(node) / sumOfRanks);
            }

            // Update PageRank values for the next iteration

            pageRanks = newPageRanks;
        }

        // Print the final PageRank values
        System.out.println("Final PageRank values:");
        for (String node : pageRanks.keySet()) {
            System.out.println(node + ": " + pageRanks.get(node));
        }
        return pageRanks;
    }

    public static void rank(HashMap<String, Double> popularityHashMap) {
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

                    score = termFrequency * IDF * priority * (popularityHashMap.get(url) != null ? popularityHashMap.get(url) : 1);

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


    }

};
