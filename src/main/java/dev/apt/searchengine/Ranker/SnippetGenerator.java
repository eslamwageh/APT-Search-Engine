package dev.apt.searchengine.Ranker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnippetGenerator {
    private static final int SNIPPET_LENGTH = 1000; // Maximum length of snippet
    private static final String HIGHLIGHT_START = "<b>"; // HTML tag to highlight query terms
    private static final String HIGHLIGHT_END = "</b>"; // Closing tag for highlighting

    // Method to generate snippet for a document
    public String generateSnippet(String document, List<String> queryTerms) {
        // Initialize variables to store the best snippet and its relevance score
        String bestSnippet = "";
        double bestScore = Double.NEGATIVE_INFINITY;

        if (document == null || document.isEmpty()) {
            System.out.println("doc was null");
            return bestSnippet;
        }
        // Parse HTML content using Jsoup
        Document doc = Jsoup.parse(document);

        // Select all paragraph elements
        Elements paragraphElements = doc.select("p");

        // Iterate over each paragraph element
        for (Element paragraph : paragraphElements) {
            // Extract text from the paragraph element
            String paragraphText = paragraph.text();

            // Calculate relevance score for the paragraph based on query terms frequency
            double score = calculateRelevanceScore(paragraphText, queryTerms);

            // Update best snippet if the current paragraph has a higher score
            if (score > bestScore && paragraphText.length() <= SNIPPET_LENGTH) {
                System.out.println(score);
                bestSnippet = paragraphText;
                bestScore = score;
            }
        }

        // Highlight query terms in the best snippet
        for (String term : queryTerms) {
            bestSnippet = highlightQueryTerm(bestSnippet, term);
        }
        return bestSnippet;
    }

    // Method to calculate relevance score for a text based on query terms frequency
    private double calculateRelevanceScore(String text, List<String> queryTerms) {
        double score = 0.0;
        for (String term : queryTerms) {
            // Calculate frequency of query term in the text
            int frequency = countFrequency(text.toLowerCase(), term);
            // Increment score based on query term frequency
            score += frequency;
        }
        return score;
    }

    // Method to count frequency of a term in a string
    private int countFrequency(String text, String term) {
        int frequency = 0;
        int index = text.indexOf(term);
        while (index != -1) {
            frequency++;
            index = text.indexOf(term, index + 1);
        }
        return frequency;
    }

    // Method to highlight query terms in the snippet
    private String highlightQueryTerm(String snippet, String term) {
        int index = snippet.toLowerCase().indexOf(term.toLowerCase());
        while (index != -1) {
            snippet = snippet.substring(0, index) + HIGHLIGHT_START + snippet.substring(index, index + term.length()) + HIGHLIGHT_END + snippet.substring(index + term.length());
            index = snippet.toLowerCase().indexOf(term.toLowerCase(), index + HIGHLIGHT_START.length() + HIGHLIGHT_END.length());
        }
        return snippet;
    }
}

