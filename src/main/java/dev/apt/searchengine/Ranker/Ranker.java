package dev.apt.searchengine.Ranker;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import dev.apt.searchengine.Crawler.CrawlerDB;
import dev.apt.searchengine.Indexer.DocData;
import org.bson.Document;

import java.lang.reflect.Array;
import java.util.*;

public class Ranker {
    public static CrawlerDB db;
    public static MongoCollection<Document> words;
    public static ArrayList<String> queryWords;
    public static ArrayList<RankedDoc> rankedDocs;
    public static HashMap<String, RankedDoc> docHashMap;

    private static HashMap<String, ArrayList<String>> urlsGraph = new HashMap<>();

    public static HashMap<String, Double> pageRanks = new HashMap<>();


    public static ArrayList<RankedDoc> mainRanker(ArrayList<String> qw, boolean isPhrase) {
        db = new CrawlerDB();
        words = db.getWordsCollection();
        queryWords = qw;
        docHashMap = new HashMap<>();
        if(isPhrase)
            phraseRank();
        else
            rank();
        return rankedDocs;
    }
    
    public static void updatePopularity(String parent, List<String> children) {
       synchronized (urlsGraph)
       {
           for(String url: children)
           {
                if(urlsGraph.containsKey(url))
                {
                    urlsGraph.get(url).add(parent);
                }
                else
                {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(parent);
                    urlsGraph.put(url, temp);
                }
           }
       }
    }

    public static void calculatePopularity()
    {
        for (String node : urlsGraph.keySet()) {
            pageRanks.put(node, 1/(double)urlsGraph.size()); // Initialize all PageRank values to 1.0
        }

        // Perform PageRank iterations
        int iterations = 10;
        double dampingFactor = 0.85; // Typical damping factor used in PageRank
        for (int i = 0; i < iterations; i++) {
            HashMap<String, Double> newPageRanks = new HashMap<>();
            double sumOfRanks = 0.0;

            // Calculate new PageRank values for each node
            for (String node : urlsGraph.keySet()) {
                double newPageRank = (1 - dampingFactor); // Initial value (damping factor)

                // Contribution from incoming edges
                for (String incomingNode : urlsGraph.keySet()) {
                    if (urlsGraph.get(incomingNode) != null && urlsGraph.get(incomingNode).size() > 0) {
                        for (String outgoingNode : urlsGraph.get(incomingNode)) {
                            if (outgoingNode.equals(node)) {
                                newPageRank += dampingFactor * (pageRanks.get(incomingNode) / urlsGraph.get(incomingNode).size());
                            }
                        }
                    }
                }

                newPageRanks.put(node, newPageRank);
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

                    String url = doc.getString("URL");
                    String title = embeddedDoc.getString("Title");
                    Double score = calculateScore(embeddedDoc, IDF, url);
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

    public static void phraseRank() {
        rankedDocs = new ArrayList<>();
        ArrayList<Document> wordsIntersection = new ArrayList<>();
        ArrayList<ArrayList<ArrayList<Integer>>> occurrences = new ArrayList<>(); //for each document, for each word, for each occurrence
        // initialize the intersection with the first word's documents
        Document initDocsQuery = new Document("Word", queryWords.get(0));
        FindIterable<Document> initDocuments = words.find(initDocsQuery);
        Document initDoc = initDocuments.first();
        if (initDoc != null) {
            wordsIntersection = (ArrayList<Document>) initDoc.get("Documents");
        }
        for (String word : queryWords) {
            Document findQuery = new Document("Word", word);
            FindIterable<Document> documents = words.find(findQuery);
            Document doc = documents.first();

            if (doc != null) {
                // get the documents of the current word
                ArrayList<Document> currentWordDocs = (ArrayList<Document>) doc.get("Documents");
                // get the intersection between the current word's documents and the wordsIntersection
                ArrayList<Document> newIntersection = new ArrayList<>();
                for (Document currentWordDoc : currentWordDocs) {
                    for (Document intersectionDoc : wordsIntersection) {
                        if (currentWordDoc.getString("URL").equals(intersectionDoc.getString("URL"))) {
                            newIntersection.add(currentWordDoc);
                            break;
                        }
                    }
                }
                wordsIntersection = newIntersection;
            }
        }
        // wordsIntersection now contains the documents that contain all the words in the query

        // fill the occurrences array
        for (String word : queryWords) {
            Document findQuery = new Document("Word", word);
            FindIterable<Document> documents = words.find(findQuery);
            Document doc = documents.first();

            if (doc != null) {
                ArrayList<Document> currentWordDocs = (ArrayList<Document>) doc.get("Documents");
                for (Document currentWordDoc : currentWordDocs) {
                    for (Document intersectionDoc : wordsIntersection) {
                        if (currentWordDoc.getString("URL").equals(intersectionDoc.getString("URL"))) {
                            if (occurrences.size() < wordsIntersection.size()) {
                                occurrences.add(new ArrayList<>());
                            }
                            ArrayList<Integer> currentWordOccurrences = (ArrayList<Integer>) currentWordDoc.get("occurrences");
                            occurrences.get(wordsIntersection.indexOf(intersectionDoc)).add(currentWordOccurrences);
                        }
                    }
                }
            }
        }
        // occurrences now contains the occurrences of each word in the query in the documents in wordsIntersection

        // now we have the documents that contain all the words in the query and the occurrences of each word in each document
        // we can check the order of the words in the occurrences

        for (int i = 0; i < wordsIntersection.size(); i++) {
            Document doc = wordsIntersection.get(i);
            String url = doc.getString("URL");
            String title = doc.getString("Title");
            Double IDF = (Double)doc.get("IDF");
            ArrayList<ArrayList<Integer>> currentDocOccurrences = occurrences.get(i);
            boolean isPhrase = true;
            int nextStartIndex = 0;
            for (int j = 0; j < currentDocOccurrences.size() - 1; j++) {
                ArrayList<Integer> currentWordOccurrences = currentDocOccurrences.get(j);
                ArrayList<Integer> nextWordOccurrences = currentDocOccurrences.get(j + 1);
                boolean found = false;
                for (int k = nextStartIndex; k < currentWordOccurrences.size(); k++) {
                    for (int l = 0; l < nextWordOccurrences.size(); l++) {
                        if (currentWordOccurrences.get(k) < nextWordOccurrences.get(l)) {
                            found = true;
                            nextStartIndex = l;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    isPhrase = false;
                    break;
                }
            }
            if (isPhrase) {
               
                RankedDoc info = new RankedDoc(url, calculateScore(doc, IDF, url), title, "");
                rankedDocs.add(info);
            }
        }

        Collections.sort(rankedDocs, Comparator.comparingDouble(RankedDoc::getScore).reversed());
        
    }

    private static Double calculateScore(Document doc, Double IDF, String url) {
        Double score;
        int termFrequency = doc.getInteger("TermFrequency");
        int priority = doc.getInteger("Priority");

        score = termFrequency * IDF * priority * (pageRanks.get(url) != null ? pageRanks.get(url) : 1 );

        return score;
    }

};
