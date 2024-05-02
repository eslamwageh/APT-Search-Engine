package dev.apt.searchengine.Crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;


public class GenerateSeed {
    public static void main(String[] args) throws InterruptedException {
 
        System.out.print("0: generate seeds\n1: upload seeds\n2: delete data base\nChoice: ");
        Scanner scanner = new Scanner(System.in);


        String choice = scanner.nextLine();
        if (choice.equals("0")) {
            System.out.print("file name: ");
            String file_name = scanner.nextLine();
            System.out.print("json name: ");
            String json_name = scanner.nextLine();

            Crawler crawler = new Crawler(null, null);
            List<Seed> seeds = readUrlsFromFile(file_name, crawler);
            writeToJsonFile(seeds, json_name);
        } else if (choice.equals("1")){
            System.out.print("json name: ");
            String json_name = scanner.nextLine();
            CrawlerDB db = new CrawlerDB();
            CrawlerData data = new CrawlerData(db);
            Crawler crawler = new Crawler(db, data);
            crawler.initializeSeed(json_name);
        } else if (choice.equals("2")) {
            CrawlerDB db = new CrawlerDB();
            db.clearDB();
        }
        scanner.close();
    }

    private static List<Seed> readUrlsFromFile(String filePath, Crawler crawler) {
        List<Seed> seeds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String com;
            int count = 1;
            while ((line = reader.readLine()) != null) {
                Seed seed = new Seed();
                seed.setURL(line);
                if ((com = crawler.createCompactString(line)) != null)
                    seed.setCompactString(com);
                else seed.setCompactString("noCompactString");
                seed.setCategory("Programming");
                seed.setIsCrawled(false);
                seed.setIsIndexed(false);
                seeds.add(seed);
                System.out.println("URL num: " + count);
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seeds;
    }

    private static void writeToJsonFile(List<Seed> seeds, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new Gson();
            gson.toJson(seeds, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static class Seed {
        private String URL;
        private String CompactString;
        private String Category;
        private boolean IsCrawled;
        private boolean IsIndexed;

        // public String getURL() {
        //     return URL;
        // }

        public void setURL(String URL) {
            this.URL = URL;
        }

        // public String getCompactString() {
        //     return CompactString;
        // }

        public void setCompactString(String compactString) {
            CompactString = compactString;
        }

        // public String getCategory() {
        //     return Category;
        // }

        public void setCategory(String category) {
            Category = category;
        }

        // public boolean isCrawled() {
        //     return IsCrawled;
        // }

        public void setIsCrawled(boolean isCrawled) {
            IsCrawled = isCrawled;
        }

        // public boolean isIndexed() {
        //     return IsIndexed;
        // }

        public void setIsIndexed(boolean isIndexed) {
            IsIndexed = isIndexed;
        }
    }
}