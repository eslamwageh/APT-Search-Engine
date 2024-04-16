package dev.apt.searchengine.QueryProcessor;

import java.util.LinkedList;
import dev.apt.searchengine.Crawler.CrawlerDB;

import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.apt.searchengine.Indexer.DocData;
import dev.apt.searchengine.Indexer.WordsProcessor;

@RestController
@RequestMapping("/api/v1/query")
public class QueryProcessor {
    @PostMapping
    public LinkedList<String> processQuery(@RequestBody String query) {
        query = WordsProcessor.withoutStopWords(query);
        String[] words = query.split(" ");
        for(String word : words) {
            word = WordsProcessor.wordStemmer(word);
        }
        LinkedList<String> urls = new LinkedList<>();
        //ranker.rank(words, urls);
        return urls;
    }
}
