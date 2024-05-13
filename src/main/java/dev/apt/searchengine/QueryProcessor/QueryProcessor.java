package dev.apt.searchengine.QueryProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.mongodb.client.MongoCollection;
import dev.apt.searchengine.Crawler.CrawlerDB;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import dev.apt.searchengine.Indexer.DocData;
import dev.apt.searchengine.Indexer.WordsProcessor;
import dev.apt.searchengine.Ranker.RankedDoc;
import dev.apt.searchengine.Ranker.Ranker;

@RestController
@RequestMapping("/api/v1/query")
@CrossOrigin(origins = "*")
public class QueryProcessor {
    boolean isPhrase = false;
    CrawlerDB database = new CrawlerDB();
    MongoCollection<Document> wordsCol = database.getWordsCollection();
    HashMap<String, String> urlhtml = database.getUrlsAndHtmlContentMap();
    @PostMapping
    public ArrayList<RankedDoc> processQuery(@RequestBody String query) {
        if (query.startsWith("\"") && query.endsWith("\"")) isPhrase = true;
        else isPhrase = false;
        query = WordsProcessor.withoutStopWords(query).toString();
        String[] words = query.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^a-zA-Z\\u0600-\\u06FF ]", "");
        }
        ArrayList<String> stemmedQueryWords = new ArrayList<>();
        for(String word : words) {
            String w = WordsProcessor.wordStemmer(word);
            if (!w.isEmpty()) stemmedQueryWords.add(w);
        }
        LinkedList<String> urls = new LinkedList<>();
        System.out.println("before ranks");
        ArrayList<RankedDoc> rankedDocs = Ranker.mainRanker(stemmedQueryWords, words, database.fetchPopularity(), isPhrase, database, wordsCol, urlhtml);
        System.out.println("after ranks");
        for (RankedDoc rd : rankedDocs) {
            urls.add(rd.getUrl());
            System.out.println(rd.getSnippet());
            System.out.println(rd.getUrl());
        }
        return rankedDocs;
    }
}
