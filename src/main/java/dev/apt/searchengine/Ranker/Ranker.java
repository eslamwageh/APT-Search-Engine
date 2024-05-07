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
    public static SnippetGenerator snippeter;
    public static MongoCollection<Document> words;
    public static HashMap<String, String> urlHtmlHashMap;
    public static ArrayList<String> queryWords;
    public static String[] originalQueryWords;
    public static ArrayList<RankedDoc> rankedDocs;
    public static HashMap<String, RankedDoc> docHashMap;
    private static HashMap<String, HashMap<String, ArrayList<Integer>>> docWordOccurrences= new HashMap<>();



    public static ArrayList<RankedDoc> mainRanker(ArrayList<String> qw, String[] oqw,  HashMap<String, Double> popularityHashMap, boolean isPhrase) {
        db = new CrawlerDB();
        snippeter = new SnippetGenerator();
        words = db.getWordsCollection();
        urlHtmlHashMap = db.getUrlsAndHtmlContentMap();
        queryWords = qw;
        originalQueryWords = oqw;
        docHashMap = new HashMap<>();
        if(isPhrase)
            phraseRank(popularityHashMap);
        else
            rank(popularityHashMap);
        return rankedDocs;
    }


    private static double diff(HashMap<String, Double> nextIter, Map<String, Double> prevIter) {
        double sum = 0;
        for(String s : nextIter.keySet()) {
            sum += Math.abs(nextIter.get(s) - prevIter.get(s));
        }
        return(Math.sqrt(sum));
    }

    public static HashMap<String, Double> calculatePopularity(HashMap<String, ArrayList<String>> urlsGraph) {
        double prevDiff = 1000;
        double convergenceThreshold = 0.001;
        HashMap<String, Double> pageRanks = new HashMap<>();

        for (String node : urlsGraph.keySet()) {
            pageRanks.put(node, 1 / (double) urlsGraph.size()); // Initialize all PageRank values to 1.0
        }

        //that is a function that calculates the converge


        // Perform PageRank iterations
        int iterations = 100;
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

            double currentDiff = diff(newPageRanks, pageRanks);

            // Check for convergence
            if (Math.abs(currentDiff - prevDiff) < convergenceThreshold) {
                // Convergence achieved, exit loop
                System.out.println("Convergence achieved at iteration: " + (i + 1));
                break;
            }

            pageRanks = newPageRanks;
            prevDiff = currentDiff;
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
                        String snippet = snippeter.generateSnippet(urlHtmlHashMap.get(url), Arrays.asList(originalQueryWords));
                        RankedDoc info = new RankedDoc(url, score, title, snippet);
                        rankedDocs.add(info);
                        docHashMap.put(url, info);
                    }
                }

            }
        }

        Collections.sort(rankedDocs, Comparator.comparingDouble(RankedDoc::getScore).reversed());


    }

    public static void phraseRank(HashMap<String, Double> popularityHashMap) {
        rankedDocs = new ArrayList<>();
        ArrayList<Document> wordsIntersection = new ArrayList<>();
        //ArrayList<ArrayList<ArrayList<Integer>>> occurrences = new ArrayList<>(); //for each document, for each word, for each occurrence
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
                    ArrayList<Integer> occ = (ArrayList<Integer>) currentWordDoc.get("Occurrences");
                    String url = currentWordDoc.getString("URL");
                    if (!docWordOccurrences.containsKey(url))
                        docWordOccurrences.put(url, new HashMap<>());
                    docWordOccurrences.get(url).put(word, occ);

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


        for (int i = 0; i < wordsIntersection.size(); i++) {
            Document doc = wordsIntersection.get(i);
            String url = doc.getString("URL");
            String title = doc.getString("Title");
            Double IDF = (Double)doc.get("IDF");
            Double score;
            // Access fields of each embedded document
            int termFrequency = doc.getInteger("TermFrequency");
            int priority = doc.getInteger("Priority");

            score = termFrequency * IDF * priority * (popularityHashMap.get(url) != null ? popularityHashMap.get(url) : 1);

            int lastOcc = -1;
            boolean isPhrase = true;
            for(String word: queryWords)
            {
                boolean goodDoc = false;
                for(Integer j :docWordOccurrences.get(url).get(word))
                {
                    if(j>lastOcc) {
                        lastOcc = j;
                        goodDoc = true;
                        break;
                    }
                }
                if(!goodDoc) {
                    isPhrase = false;
                    break;
                }
            }
            if (isPhrase)
            {
                RankedDoc info = new RankedDoc(url, score, title, "");
            }

        }


        Collections.sort(rankedDocs, Comparator.comparingDouble(RankedDoc::getScore).reversed());

    }

};
