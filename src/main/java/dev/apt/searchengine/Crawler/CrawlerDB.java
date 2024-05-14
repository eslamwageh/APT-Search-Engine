package dev.apt.searchengine.Crawler;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//	Data structures
import java.util.*;

//	MongoDB dependencies
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import dev.apt.searchengine.Indexer.DocData;
import dev.apt.searchengine.Indexer.Indexer;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

@Data // This line is to make setters and getters of each data member
public class CrawlerDB {
    private Properties env;

    // MongoDB data members
    private MongoClient mongoClient;

    private MongoDatabase database;
    private MongoCollection<org.bson.Document> urlsCollection;
    private MongoCollection<org.bson.Document> wordsCollection;
    private MongoCollection<org.bson.Document> urlsGraphCollection;
    private MongoCollection<org.bson.Document> popularityCollection;
    private MongoCollection<org.bson.Document> urlHtmlCollection;

    public CrawlerDB() {
        env = new Properties();
        try {
            FileInputStream fis = new FileInputStream(".env");
            env.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectMongoDB();
        //delete all the documents in the collection
        //urlsCollection.deleteMany(new org.bson.Document());
    }

    public void clearDB() {
        System.out.print("Are you sure you want to delete the data base? (yes, no): ");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        if (response.equals("yes")) urlsCollection.deleteMany(new org.bson.Document());
        scanner.close();
    }

    public void connectMongoDB() {
        String password = env.getProperty("MONGOPASS");
        String username = env.getProperty("MONGOUSER");
        System.out.println(password);
        System.out.println(username);

        String uri = "mongodb://localhost:27017";
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("SearchEngine");
        urlsCollection = database.getCollection("WebPage");
        wordsCollection = database.getCollection("Search Index");
        urlsGraphCollection = database.getCollection("URLs Graph");
        popularityCollection = database.getCollection("Popularity");
        urlHtmlCollection = database.getCollection(("urlHtml"));
    }

    // changed the name to urls as there will be another db update
    public synchronized void updateUrlsDB(List<WebPage> newWPs) {
        if (newWPs != null) {

            List<org.bson.Document> documents = new ArrayList<>();
            List<org.bson.Document> urlHtmldocuments = new ArrayList<>();
            for (WebPage wp : newWPs) {

                org.bson.Document document = new org.bson.Document()
                        .append("URL", wp.URL)
                        .append("CompactString", wp.compactString)
                        .append("Category", wp.category)
                        .append("IsCrawled", wp.isCrawled)
                        .append("IsIndexed", wp.isIndexed);

                org.bson.Document urlHtmldocument = new org.bson.Document()
                        .append("url", wp.URL)
                        .append("html", wp.HtmlContent);

                System.out.println("to upload: " + wp.URL);
                documents.add(document);
                urlHtmldocuments.add(urlHtmldocument);
            }
            urlsCollection.insertMany(documents);
            urlHtmlCollection.insertMany(urlHtmldocuments);
        }
    }

    public void updateCompactString(String URL, String compactString) {
        org.bson.Document query = new org.bson.Document("URL", URL);
        org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("CompactString", compactString));
        org.bson.Document result = urlsCollection.findOneAndUpdate(query, update);
        if (result != null) {
            String oldValue = result.getString("CompactString");
            if (compactString == null)
                return;
            if (!compactString.equals(oldValue)) {
                updateIsIndexed(URL, false);
            }
        }

    }

    public void updateIsIndexed(String URL, boolean isIndexed) {
        org.bson.Document query = new org.bson.Document("URL", URL);
        org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("IsIndexed", isIndexed));
        urlsCollection.updateOne(query, update);
    }

    public void updateIsCrawled(String URL, boolean isCrawled) {
        org.bson.Document query = new org.bson.Document("URL", URL);
        org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("IsCrawled", isCrawled));
        urlsCollection.updateOne(query, update);
    }

    public void updateIsCrawled() {
        // update all the documents to IsCrawled = false
        org.bson.Document query = new org.bson.Document();
        org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("IsCrawled", false));
        urlsCollection.updateMany(query, update);
    }

    // to update the second collection
    public void updateWordsDB(HashMap<String, HashMap<String, DocData>> invertedFile, HashMap<String, Double> DFsPerDocs) {
        if (invertedFile != null && !invertedFile.isEmpty() && DFsPerDocs != null && !DFsPerDocs.isEmpty()) {
            List<Document> documents = new ArrayList<>();

            for (Map.Entry<String, HashMap<String, DocData>> entry : invertedFile.entrySet()) {
                String word = entry.getKey();
                HashMap<String, DocData> docHash = entry.getValue();

                List<Document> wordDocuments = new ArrayList<>();

                for (Map.Entry<String, DocData> docHashEntry : docHash.entrySet()) {
                    String docUrl = docHashEntry.getKey();
                    DocData docData = docHashEntry.getValue();

                    Document wordDocument = new Document()
                            .append("URL", docUrl) // Assuming docId is the HTML document URL
                            .append("TermFrequency", docData.getTermFrequency())
                            .append("Title", docData.getTitle())
                            .append("Priority", docData.getPriority())
                            .append("Occurrences", docData.getOccurrences());

                    wordDocuments.add(wordDocument);
                }

                Document wordEntry = new Document("Word", word)
                        .append("Documents", wordDocuments).append("IDF", DFsPerDocs.get(word) != null? Math.log(1 / DFsPerDocs.get(word)): 5.0);

                documents.add(wordEntry);
            }
            wordsCollection.deleteMany(new Document());
            wordsCollection.insertMany(documents);
        }
    }

    public HashMap<String, Double> retrieveDFsPerDocs() {
        HashMap<String, Double> DFsPerDocs = new HashMap<>();

        // Retrieve all documents from the collection
        FindIterable<Document> iterable = wordsCollection.find();

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                Document wordEntry = cursor.next();

                // Extract word and documents information
                String word = wordEntry.getString("Word");
                Double idf = wordEntry.getDouble("IDF");

                // Put DocData object into docHash
                DFsPerDocs.put(word, Math.pow(10, -idf));
            }
        }
        return DFsPerDocs;
    }


    public HashMap<String, HashMap<String, DocData>> retrieveWordsDB() {
        HashMap<String, HashMap<String, DocData>> invertedFile = new HashMap<>();

        // Retrieve all documents from the collection
        FindIterable<Document> iterable = wordsCollection.find();

        try (MongoCursor<Document> cursor = iterable.iterator()) {
            while (cursor.hasNext()) {
                Document wordEntry = cursor.next();

                // Extract word and documents information
                String word = wordEntry.getString("Word");
                HashMap<String, DocData> docHash = new HashMap<>();
                for (Document wordDocument : (Iterable<Document>) wordEntry.get("Documents")) {
                    String docUrl = wordDocument.getString("URL");
                    Integer termFrequency = wordDocument.getInteger("TermFrequency");
                    String title = wordDocument.getString("Title");
                    Integer priority = wordDocument.getInteger("Priority");
                    ArrayList<Integer> occurrences = (ArrayList<Integer>) wordDocument.get("Occurrences");

                    // Create DocData object
                    DocData info = new DocData();
                    info.setTitle(title);
                    info.setOccurrences(occurrences);
                    info.setPriority(priority);
                    info.setTermFrequency(termFrequency);

                    // Put DocData object into docHash
                    docHash.put(docUrl, info);
                }

                // Put docHash into invertedFile
                invertedFile.put(word, docHash);
            }
        }


        return invertedFile;
    }

    // this is the best version
    public LinkedList<String> _fetchSeed() {
        connectMongoDB();
        LinkedList<String> s = new LinkedList<>();
        // download all the documents having IsCrawled = false
        org.bson.Document query = new org.bson.Document("IsCrawled", false);
        for (org.bson.Document doc : urlsCollection.find(query)) {
            s.add(doc.getString("URL"));
        }
        return s;

    }

    public LinkedList<String> _fetchAllURLs() {
        LinkedList<String> s = new LinkedList<>();
        org.bson.Document query = new org.bson.Document();
        for (org.bson.Document doc : urlsCollection.find(query)) {
            s.add(doc.getString("URL"));
        }
        return s;
    }

    public boolean detectDuplicatePages(String compactString) {
        org.bson.Document query = new org.bson.Document("CompactString", compactString);
        return urlsCollection.countDocuments(query) > 0;
    }


    public void updateUrlsGraphDB(String parent, List<String> children) {
        for (String url : children) {
            Document query = new Document("url", url);
            Document update = new Document("$push", new Document("parents", parent));

            UpdateOptions options = new UpdateOptions().upsert(true);
            urlsGraphCollection.updateOne(query, update, options);
        }
    }

    public HashMap<String, ArrayList<String>> fetchUrlsGraphFromDB() {
        HashMap<String, ArrayList<String>> urlsGraph = new HashMap<>();

        // Query all documents from the collection
        FindIterable<Document> documents = urlsGraphCollection.find();

        // Iterate over the documents and populate urlsGraph
        for (Document document : documents) {
            String url = document.getString("url");
            List<String> parents = (List<String>) document.get("parents");

            // Add the URL and its parents to the urlsGraph
            urlsGraph.put(url, new ArrayList<>(parents));
        }
        return urlsGraph;
    }

    public void uploadPopularity(HashMap<String, Double> popularityHashMap) {
        if (popularityHashMap != null && !popularityHashMap.isEmpty()) {
            List<Document> documents = new ArrayList<>();

            for (Map.Entry<String, Double> entry : popularityHashMap.entrySet()) {
                String url = entry.getKey();
                Double pop = entry.getValue();


                Document popDocument = new Document("url", url)
                        .append("popularity", pop);

                documents.add(popDocument);
            }
            popularityCollection.deleteMany(new Document());
            popularityCollection.insertMany(documents);
        }
    }

    public HashMap<String, Double> fetchPopularity() {
        HashMap<String, Double> popularityHashMap = new HashMap<>();

        FindIterable<Document> documents = popularityCollection.find();
        for (Document document : documents) {
            String url = document.getString("url");
            Double popularity = document.getDouble("popularity");
            popularityHashMap.put(url, popularity);
        }
        return popularityHashMap;
    }

    public HashMap<String, String> getUrlsAndHtmlContentMap() {
        HashMap<String, String> urlHtmlMap = new HashMap<>();

        for (Document doc : urlHtmlCollection.find()) {
            // Extract URL and HTML content from the document
            String url = doc.getString("url");
            String htmlContent = doc.getString("html");

            // Put URL and HTML content into the map
            urlHtmlMap.put(url, htmlContent);

            // Print URL (optional)
            System.out.println(url);
        }

//        try (FileReader reader = new FileReader("urlHtml.json")) {
//            // Parse the JSON file into a JSON array
//            JSONArray jsonArray = new JSONArray(new JSONTokener(reader));
//
//            // Iterate through the JSON array
//            for (int i = 0; i < jsonArray.length(); i++) {
//                // Get the JSON object at the current index
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                // Extract URL and HTML content from the JSON object
//                String url = jsonObject.getString("url");
//                String htmlContent = jsonObject.getString("html");
//
//                // Put URL and HTML content into the map
//                urlHtmlMap.put(url, htmlContent);
//
//                // Print URL (optional)
//                System.out.println(url);
//            }
//        } catch (IOException e) {
//            // Handle IO exception
//            System.out.println("error in gettin hash map");
//        }

        return urlHtmlMap;
    }
}
