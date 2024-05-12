package dev.apt.searchengine.Indexer;

import org.tartarus.snowball.ext.englishStemmer;

import java.util.Arrays;
import java.util.List;

public class WordsProcessor {
    private static final List<String> stopWords = Arrays.asList( "a", "about", "above", "across", "after", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "among", "an", "and", "another", "any", "anybody", "anyone", "anything", "anywhere", "are", "area", "areas", "around", "as", "ask", "asked", "asking", "asks", "at", "away",
            "b", "back", "backed", "backing", "backs", "be", "became", "because", "become", "becomes", "been", "before", "began", "behind", "being", "beings", "best", "better", "between", "big", "both", "but", "by",
            "came", "can", "cannot", "case", "cases", "certain", "certainly", "clear", "clearly", "come", "could",
            "d", "did", "differ", "different", "differently", "do", "does", "done", "down", "downed", "downing", "downs", "during",
            "e", "each", "early", "either", "end", "ended", "ending", "ends", "enough", "even", "evenly", "ever", "every", "everybody", "everyone", "everything", "everywhere",
            "f", "face", "faces", "fact", "facts", "far", "felt", "few", "find", "finds", "first", "for", "four", "from", "full", "fully", "further", "furthered", "furthering", "furthers",
            "g", "gave", "general", "generally", "get", "gets", "give", "given", "gives", "go", "going", "good", "goods", "got", "great", "greater", "greatest", "group", "grouped", "grouping", "groups",
            "h", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "high", "higher", "highest", "him", "himself", "his", "how", "however",
            "i", "if", "important", "in", "interest", "interested", "interesting", "interests", "into", "is", "it", "its", "itself",
            "j", "just",
            "k", "keep", "keeps", "kind", "knew", "know", "known", "knows",
            "l", "large", "largely", "last", "later", "latest", "least", "less", "let", "lets", "like", "likely", "long", "longer", "longest",
            "m", "made", "make", "making", "man", "many", "may", "me", "member", "members", "men", "might", "more", "most", "mostly", "mr", "mrs", "much", "must", "my", "myself",
            "n", "necessary", "need", "needed", "needing", "needs", "never", "new", "newer", "newest", "next", "no", "nobody", "non", "noone", "not", "nothing", "now", "nowhere", "number", "numbers",
            "o", "of", "off", "often", "old", "older", "oldest", "on", "once", "one", "only", "open", "opened", "opening", "opens", "or", "order", "ordered", "ordering", "orders", "other", "others", "our", "out", "over",
            "p", "part", "parted", "parting", "parts", "per", "perhaps", "place", "places", "point", "pointed", "pointing", "points", "possible", "present", "presented", "presenting", "presents", "problem", "problems", "put", "puts",
            "q", "quite",
            "r", "rather", "really", "right", "room", "rooms",
            "s", "said", "same", "saw", "say", "says", "second", "seconds", "see", "seem", "seemed", "seeming", "seems", "sees", "several", "shall", "she", "should", "show", "showed", "showing", "shows", "side", "sides", "since", "small", "smaller", "smallest", "so", "some", "somebody", "someone", "something", "somewhere", "state", "states", "still", "such", "sure",
            "t", "take", "taken", "than", "that", "the", "their", "them", "then", "there", "therefore", "these", "they", "thing", "things", "think", "thinks", "this", "those", "though", "thought", "thoughts", "three", "through", "thus", "to", "today", "together", "too", "took", "toward", "turn", "turned", "turning", "turns", "two",
            "u", "under", "until", "up", "upon", "us", "use", "used", "uses",
            "v", "very",
            "w", "want", "wanted", "wanting", "wants", "was", "way", "ways", "we", "well", "wells", "went", "were", "what", "when", "where", "whether", "which", "while", "who", "whole", "whose", "why", "will", "with", "within", "without", "work", "worked", "working", "works", "would",
            "x",
            "y", "year", "years", "yet", "you", "young", "younger", "youngest", "your", "yours",
            "z",
            "×",
            "»",
            "?");

    public static List<String> withoutStopWords(String s) {
        List<String> words = Arrays.asList(s.split(" "));
        //words.removeIf(stopWords::contains);
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i).replaceAll("[^a-zA-Z\\u0600-\\u06FF ]", "");
            // Check if the stopWords list contains the current word
            if (stopWords.contains(word.toLowerCase())) {
                // Remove the word if it's a stop word
                words.set(i, "");
                // Decrement the index to handle the shifted elements
            }
            else words.set(i, word);
        }
        System.out.println(words.toString());
        return words;
    }

    public static String wordStemmer(String w) {
        String cleanedText = w.toLowerCase().replaceAll("[^a-zA-Z\\u0600-\\u06FF ]", "");
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(cleanedText);
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
