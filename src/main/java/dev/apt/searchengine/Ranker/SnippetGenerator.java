package dev.apt.searchengine.Ranker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnippetGenerator {
    private static final int SNIPPET_LENGTH = 1000; // Maximum length of snippet
    private static final String HIGHLIGHT_START = "<b>"; // HTML tag to highlight query terms
    private static final String HIGHLIGHT_END = "</b>"; // Closing tag for highlighting

    private int numberOfTerms;

    // Method to generate snippet for a document
    public String generateSnippet(String document, List<String> queryTerms) {
        // Initialize variables to store the best snippet and its relevance score
        String bestSnippet = "";
        double bestScore = -1.0;
        double bestNumberOfTerms = -1.0;

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
            int score = calculateRelevanceScore(paragraphText, queryTerms);

            // Update best snippet if the current paragraph has a higher score
            if (paragraphText.length() <= SNIPPET_LENGTH && (numberOfTerms > bestNumberOfTerms || numberOfTerms == bestNumberOfTerms && score > bestScore)) {
                System.out.println(score);
                bestSnippet = paragraphText;
                bestScore = score;
                bestNumberOfTerms = numberOfTerms;
                System.out.println(bestSnippet);
            }
        }

        // Highlight query terms in the best snippet
//        for (String term : queryTerms) {
//            bestSnippet = highlightQueryTerm(bestSnippet, term);
//        }
        return bestSnippet;
    }

    // Method to calculate relevance score for a text based on query terms frequency
    private int calculateRelevanceScore(String text, List<String> queryTerms) {
        int score = 0;
        numberOfTerms = 0;
        for (String term : queryTerms) {
            // Calculate frequency of query term in the text
            if (term == null || term.isEmpty()) continue;
            int frequency = countFrequency(text.toLowerCase(), term.toLowerCase());
            // Increment score based on query term frequency
            score += frequency;
            if (frequency != 0) numberOfTerms++;
        }

        return score;
    }

    // Method to count frequency of a term in a string
    private int countFrequency(String text, String term) {
        // Escape special characters in the term to prevent them from being interpreted as regex metacharacters
        String escapedTerm = Pattern.quote(term);

        // Create a regex pattern to match the term
        Pattern pattern = Pattern.compile(escapedTerm, Pattern.CASE_INSENSITIVE);

        // Use Matcher to find all occurrences of the term
        Matcher matcher = pattern.matcher(text);

        int frequency = 0;
        while (matcher.find()) {
            frequency++;
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

