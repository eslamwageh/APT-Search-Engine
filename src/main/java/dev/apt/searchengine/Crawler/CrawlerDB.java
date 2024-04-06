package dev.apt.searchengine.Crawler;

import java.io.FileInputStream;
import java.io.IOException;
//	Data structures
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

//	MongoDB dependencies
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Data;
import lombok.Getter;

@Data // This line is to make setters and getters of each data member
public class CrawlerDB {
	private Properties env;

	// MongoDB data members
	private MongoClient mongoClient;

	private MongoDatabase database;
	private MongoCollection<org.bson.Document> urlsCollection;
	private MongoCollection<org.bson.Document> wordsCollection;

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
		//! collection.deleteMany(new org.bson.Document());
	}

	public void connectMongoDB() {
		String password = env.getProperty("MONGOPASS");
		String username = env.getProperty("MONGOUSER");
		System.out.println(password);
		System.out.println(username);

		String uri = String.format(
				"mongodb+srv://%s:%s@searchengine.c7upty2.mongodb.net/?retryWrites=true&w=majority&appName=SearchEngine",
				username, password);
		mongoClient = MongoClients.create(uri);
		database = mongoClient.getDatabase("SearchEngine");
		urlsCollection = database.getCollection("WebPage");
		wordsCollection = database.getCollection("Search Index");
	}

	// changed the name to urls as there will be another db update
	public void updateUrlsDB(List<WebPage> newWPs) {
		if (newWPs != null) {
			List<org.bson.Document> documents = new ArrayList<>();
			for (WebPage wp : newWPs) {
				org.bson.Document document = new org.bson.Document()
						.append("URL", wp.URL)
						.append("CompactString", wp.compactString)
						.append("Category", wp.category)
						.append("Refreshed", wp.isRefreshed);
				documents.add(document);
			}
			urlsCollection.insertMany(documents);
		}
	}

	// this is the best version
	public LinkedList<String> _fetchSeed() {
		connectMongoDB();
		LinkedList<String> s = new LinkedList<>();
		for (org.bson.Document doc : urlsCollection.find()) {
			s.add(doc.getString("URL"));
		}
		return s;
	}

	public boolean detectDuplicatePages(String compactString) {
		org.bson.Document query = new org.bson.Document("CompactString", compactString);
		return urlsCollection.countDocuments(query) > 0;
	}
}
