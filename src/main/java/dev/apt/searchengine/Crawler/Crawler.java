package dev.apt.searchengine.Crawler;

import java.io.IOException;
// data structures
import java.util.*;
import java.util.stream.Collectors;

// jsoup related dependencies
import dev.apt.searchengine.Ranker.Ranker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
// helping classes
import java.net.URI;
import java.net.URISyntaxException;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;


// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;

public class Crawler implements Runnable {
    private static int maxWPNum;
    private static int maxChildren; // is the maximum number of children per URL
    private CrawlerData crawlerData;
    private CrawlerDB database;
    // Categories
    private Domains domains;

    public Crawler(CrawlerDB db, CrawlerData data) {
        this.database = db;
        this.crawlerData = data;
        domains = new Domains();
    }

    public static void setMaxWPNum(int maxi) {
        maxWPNum = maxi;
    }

    public static void setMaxChildren(int maxi) {
        maxChildren = maxi;
    }

    @Override
    public void run() {
        // check if it reached the limit
        while (crawlerData.getCrawledPagesNum() < maxWPNum) {
            // get a seed from the queue
            String seed = crawlerData.getSeed();

            database.updateCompactString(seed, createCompactString(seed));

            System.out.println("\n" + Thread.currentThread().getName());
            System.out.println("Seed: " + seed);

            // crawl over that seed
            List<String> newSeeds = crawl(seed);
            System.out.println("tamam");
            if (newSeeds == null)
                return;

            // System.out.println("New Seeds: " + newSeeds.size());

            // remove duplicates and create a list of new webpages to upload
            List<WebPage> newWP = detectDuplicate(newSeeds);

            // update the database
            database.updateUrlsDB(newWP);
            database.updateIsCrawled(seed, true);

            database.updateUrlsGraphDB(seed, newSeeds);

        }

        System.out.println("Crawling Sprint Finished");

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
            return null;
        }

        // allow only a certain amount of URLs out of each crawled URL
        List<String> sortedURLs = grippedURLs.stream()
                .distinct()
                .sorted(Comparator.comparingInt(String::length))
                .limit(Math.min(maxWPNum - crawlerData.getCrawledPagesNum(), maxChildren))
                .collect(Collectors.toList());

        crawlerData.increaseCrawledPagesNum(sortedURLs.size());

        return sortedURLs;
    }

    // private void _deleteWP(String url) {

    // }

    // Check if the given URL is allowed in terms of robots.txt
    private boolean _isAllowedPath(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            //System.err.println("Invalid URL: " + url);
            return false;
        }
        // check authorized connection
        try {
            HttpURLConnection connection = (HttpURLConnection) ((new URI(url)).toURL().openConnection());
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode(); //? set timeout here
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
        } catch (IOException e) {
            // System.err.println("Error 1 checking URL: " + url);
            // e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            // System.err.println("Error 2 checking URL: " + url);
            // e.printStackTrace();
            return false;
        }
        // get the base url
        String baseUrl = "";
        try {
            URI urlHandler = new URI("");
            urlHandler = new URI(url);
            baseUrl = urlHandler.getScheme() + "://" + urlHandler.getHost();
        } catch (URISyntaxException e) {
            // System.err.println("error at uriHandler");
            // e.printStackTrace();
            return false;
        }
        // fetch the robots.txt
        Document doc = new Document("");
        try {
            String robotsPath = baseUrl + "/robots.txt";

            // check that it exist first
            HttpURLConnection connection = (HttpURLConnection) ((new URI(robotsPath)).toURL().openConnection());
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                //System.err.println("Received response code " + responseCode + " for URL: " + url);
                return true;
            }

            doc = Jsoup.connect(robotsPath).get();
        } catch (IOException e) {
            // System.err.println("failed to get the base URL");
            // e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            // System.err.println("error at uriHandler");
            // e.printStackTrace();
            return false;
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
                WebPage page = new WebPage(url, compactString, categorizePage(url), false, false);
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
        if (okPages.size() == 0) {
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

    public String createCompactString(String URL) {
        org.jsoup.nodes.Document doc;
        try {
            doc = Jsoup.connect(URL).get();
            String compactString;
            compactString = generateHash(doc.body().text());
            return compactString;
        } catch (IOException e) {
            System.out.println("Error: m4 lagyeen el url deh yabo 3ammo");
            return null;
        }
    }

    private String generateHash(String input) { // ? this is my hand-made hash function aka the inefficient one :D
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
    /*
     * the old hash function
     * private String generateHash(String input, String algorithm) throws
     * NoSuchAlgorithmException {
     * MessageDigest digest = MessageDigest.getInstance(algorithm);
     * byte[] hash = digest.digest(input.getBytes());
     * StringBuffer hexString = new StringBuffer();
     * for (int i = 0; i < hash.length; i++) {
     * String hex = Integer.toHexString(0xff & hash[i]);
     * if (hex.length() == 1)
     * hexString.append('0');
     * hexString.append(hex);
     * }
     * return hexString.toString();
     * }
     */

    public void initializeSeed(String json_name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(json_name)));
            JSONArray jsonArray = new JSONArray(content);
            List<WebPage> webPages = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                System.out.println(i);
                WebPage webPage = new WebPage(jsonObject.getString("URL"), jsonObject.getString("CompactString"),
                        jsonObject.getString("Category"), jsonObject.getBoolean("IsCrawled"), jsonObject.getBoolean("IsIndexed"));
                webPages.add(webPage);
            }

            this.database.updateUrlsDB(webPages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        CrawlerDB db = new CrawlerDB();
        CrawlerData data = new CrawlerData(db);
        // Ask for the number of threads
        System.out.print("Enter number of threads: ");
        Scanner scanner = new Scanner(System.in);
        int threadsNum = scanner.nextInt();
        // Ask for the max number of crawled pages
        System.out.print("Enter max number of crawled WebPages: ");
        int maxi = scanner.nextInt();
        // Ask for the depth of a single url
        System.out.print("What is the max number of URLs out of a single URL? ");
        int maxChildren = scanner.nextInt();
        scanner.close();

        Crawler.setMaxWPNum(maxi);
        Crawler.setMaxChildren(maxChildren);

        // Crawler cr = new Crawler(db, data);
        // //
        // System.out.println(cr._isAllowedPath("https://www.amazon.com/gp/help/customer/display.html/ref=footer_cou?ie=UTF8&nodeId=508088"));
        // cr.initializeSeed();

        // Create an array to store references to the threads
        Thread[] threads = new Thread[threadsNum];

        // Start the threads
        for (int i = 0; i < threadsNum; i++) {
            Thread thread = new Thread(new Crawler(db, data));
            thread.setName("Thread " + (i + 1));
            thread.start();
            threads[i] = thread; // Store the reference to each thread in the array
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join(); // Wait for each thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace(); // Handle interrupted exception if necessary
            }
        }

        // we get the hashmap from the urls graph db at the end of crawling
        // then we pass it to calculate popularity and it returns popularity hash map
        // we recieve it and pass it to upload on the database so that the searching can access it

        db.uploadPopularity(Ranker.calculatePopularity(db.fetchUrlsGraphFromDB()));
    }
}

// javac -cp .:../lib/jsoup.jar Crawler.java
// java -cp .:../lib/jsoup.jar Crawler