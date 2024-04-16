package dev.apt.searchengine.QueryProcessor;

import java.util.ArrayList;
import java.util.LinkedList;
import dev.apt.searchengine.Crawler.CrawlerDB;

import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.apt.searchengine.Indexer.DocData;
import dev.apt.searchengine.Indexer.WordsProcessor;
import dev.apt.searchengine.Ranker.RankedDoc;
import dev.apt.searchengine.Ranker.Ranker;

@RestController
@RequestMapping("/api/v1/query")
public class QueryProcessor {
    @PostMapping
    public LinkedList<String> processQuery(@RequestBody String query) {
        query = WordsProcessor.withoutStopWords(query);
        String[] words = query.split(" ");
        ArrayList<String> stemmedQueryWords = new ArrayList<>();
        for(String word : words) {
            String w = WordsProcessor.wordStemmer(word);
            if (!w.isEmpty()) stemmedQueryWords.add(w);
        }
        LinkedList<String> urls = new LinkedList<>();
        ArrayList<RankedDoc> rankedDocs = Ranker.mainRanker(stemmedQueryWords);
        for (RankedDoc rd : rankedDocs) {
            urls.add(rd.getUrl());
        }
        return urls;
    }
}
