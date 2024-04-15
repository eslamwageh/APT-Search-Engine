package dev.apt.searchengine.QueryProcessor;

import dev.apt.searchengine.Indexer.WordsProcessor;
import dev.apt.searchengine.Ranker.RankedDoc;
import dev.apt.searchengine.Ranker.Ranker;

import java.util.ArrayList;
import java.util.Arrays;

public class QueryProcessor {
    public static void main(String[] args) {
        String query = "Iphone 15";
        query = WordsProcessor.withoutStopWords(query);
        ArrayList<String> queryWords = new ArrayList<>(Arrays.asList(query.split(" ")));
        ArrayList<String> stemmedQueryWords = new ArrayList<>();
        for (String word : queryWords) {
            String w = WordsProcessor.wordStemmer(word);
            if (!w.isEmpty()) stemmedQueryWords.add(w);
        }
        ArrayList<RankedDoc> rankedDocs = Ranker.mainRanker(stemmedQueryWords);
        for (RankedDoc rd : rankedDocs) {
            System.out.println(rd.getUrl() + " " + rd.getScore());
        }
    }
}
