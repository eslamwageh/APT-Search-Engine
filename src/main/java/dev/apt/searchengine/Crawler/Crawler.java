package dev.apt.searchengine.Crawler;

import java.io.IOException;
// data structures
import java.util.List;
import java.util.ArrayList;
// jsoup related dependencies
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
// helping classes
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;

public class Crawler implements Runnable {
	private CrawlerData crawlerData;
	private CrawlerDB database;
	// Categories
	private Domains domains;

	public Crawler(CrawlerDB db, CrawlerData data) {
		this.database = db;
		this.crawlerData = data;
		domains = new Domains();
	}

	@Override
	public void run() {
		// check if it reached the limit
		while (crawlerData.getCrawledPagesNum() < 6000){
		// get a seed from the queue
		String seed = crawlerData.getSeed();
		System.out.println("Seed: " + seed);
		// crawl over that seed
		List<String> newSeeds = crawl(seed);
		System.out.println("New Seeds: " + newSeeds.size());
		// remove duplicates and create a list of new webpages to upload
		List<WebPage> newWP = detectDuplicate(newSeeds);
		// update the database
		database.updateDB(newWP);
	}
	System.out.println("Crawling is done");

	}

	private List<String> crawl(String url) {
		List<String> grippedURLs = new ArrayList<>();
		try {
			// connect to the webpage
			Document doc = Jsoup.connect(url).get();
			// get all the link elements in document
			Elements links = doc.select("a[href]");
			// extract the urls into the grippedURLs array
			for (Element link : links) {
				String href = link.attr("abs:href");
				if (_isAllowedPath(href))
					grippedURLs.add(href);
			}
		} catch (IOException e) {
			System.err.println("error in connecting to the url");
			e.printStackTrace();
		}
		return grippedURLs;
	}

	// private void _deleteWP(String url) {

	// }

	// Check if the given URL is allowed in terms of robots.txt
	private boolean _isAllowedPath(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			System.err.println("Invalid URL: " + url);
			return false;
		}
		// get the base url
		String baseUrl = "";
		try {
			URI urlHandler = new URI("");
			urlHandler = new URI(url);
			baseUrl = urlHandler.getScheme() + "://" + urlHandler.getHost();
		} catch (URISyntaxException e) {
			System.err.println("error at uriHandler");
			e.printStackTrace();
		}
		// fetch the robots.txt
		Document doc = new Document("");
		try {
			doc = Jsoup.connect(baseUrl + "/robots.txt").get();
		} catch (IOException e) {
			System.err.println("failed to get the base URL");
			e.printStackTrace();
		}
		String robotsTxt = doc.wholeText();
		String[] lines = robotsTxt.split("\n");
		// get the disallowed paths
		boolean isGeneralAgent = false;
		List<String> disallowedPaths = new ArrayList<>();
		for (String line : lines) {
			// check that the rules are for general agents and not a specific one
			if (line.startsWith("User-agent:")) {
				isGeneralAgent = line.substring("User-agent:".length()).trim().equals("*");
			}
			// check and collect the disallowed paths
			if (line.startsWith("Disallow:") && isGeneralAgent) {
				String path = line.substring("Disallow:".length()).trim();
				path = path.replace("*", ".*");
				disallowedPaths.add(baseUrl + path);
			}
		}
		// now, check the url against every path
		for (String disallowed : disallowedPaths)
			if (url.matches(disallowed))
				return false;
		// the url is ok
		return true;
	}

	private List<WebPage> detectDuplicate(List<String> urls) {
		List<WebPage> okPages = new ArrayList<WebPage>();
		for (String url : urls) {
			System.out.println("URL: " + url);
			if (!crawlerData.isUniqueURL(url)) {
				continue;
			}
			String compactString = createCompactString(url);
			if (!database.detectDuplicatePages(compactString)) {
				WebPage page = new WebPage(url, compactString, categorizePage(url), true);
				okPages.add(page);
				crawlerData.addUniqueURL(url);
				crawlerData.addSeed(url);
				// ? update(page);
			}
			// System.out.println("URL: " + URL + " with compactString: " + compactString);
			// //! testing purposes
			// System.out.println("URL: " + URL + " with category: " + categorizePage(URL));
			// //! testing purposes
		}
		if(okPages.size() == 0){
			System.out.println("No new pages to add from this seed");
			return null;
		}
		return okPages;

	}

	private String categorizePage(String url) {
		try {
			String domain = getDomain(new URI(url));

			if (domains.newsDomains.contains(domain)) {
				return "News";
			}
			if (domains.socialMediaDomains.contains(domain)) {
				return "Social Media";
			}
			if (domains.shoppingDomains.contains(domain)) {
				return "Shopping";
			}
			if (domains.sportsDomains.contains(domain)) {
				return "Sports";
			}
			if (domains.educationDomains.contains(domain)) {
				return "Education";
			}
			if (domains.scienceDomains.contains(domain)) {
				return "Science";
			}
			if (domains.healthDomains.contains(domain)) {
				return "Health";
			}
			if (domains.programmingToolsDomains.contains(domain)) {
				return "Development";
			}
			if (domains.streamingDomains.contains(domain)) {
				return "Entertainment";
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "Miscellaneous";
	}

	private String getDomain(URI url) {
		String host = url.getHost();
		String[] hostParts = host.split("\\.");

		if (hostParts.length >= 2) {
			return hostParts[hostParts.length - 2];
		} else {
			return host;
		}
	}

	private String createCompactString(String URL) {
		org.jsoup.nodes.Document doc;
		try {
			doc = Jsoup.connect(URL).get();
			String compactString;
			compactString = generateHash(doc.body().text());
			return compactString;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String generateHash(String input) { //? this is my hand-made hash function aka the inefficient one :D
		long sum = 0;
		int groupSize = 100;
		int groupIndex = 0;
		for (int i = 0; i < input.length(); i += groupSize) {
			long groupSum = 0;
			for (int j = i; j < i + groupSize && j < input.length(); j++) {
				groupSum += (long) input.charAt(j);
			}
			sum += groupSum * groupIndex;
			groupIndex++;
		}
		String hexString = Long.toHexString(sum);
		return hexString;
	}
	/* the old hash function
	private String generateHash(String input, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		byte[] hash = digest.digest(input.getBytes());
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
	 */

	private void initializeSeed(){
		try {
			String content = new String(Files.readAllBytes(Paths.get("./seed.json")));
			JSONArray jsonArray = new JSONArray(content);
			List<WebPage> webPages = new ArrayList<>();
	
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				WebPage webPage = new WebPage(jsonObject.getString("URL"), jsonObject.getString("CompactString"),
						jsonObject.getString("Category"), jsonObject.getBoolean("Refreshed"));
				webPages.add(webPage);
			}
	
			this.database.updateDB(webPages);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		int threadsNum  = Integer.parseInt(args[0]);
		// for now, make it only one
		threadsNum = 1;
		CrawlerDB db = new CrawlerDB();
		CrawlerData data = new CrawlerData(db);
		for (int i = 0; i < threadsNum; i++) {
			(new Thread( new Crawler(db, data))).start();
		}
	}
}

// javac -cp .:../lib/jsoup.jar Crawler.java
// java -cp .:../lib/jsoup.jar Crawler