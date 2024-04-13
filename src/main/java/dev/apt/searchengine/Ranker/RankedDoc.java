package dev.apt.searchengine.Ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankedDoc {
    private String url;
    private Double score;
    private String title;
    private String snippet;
};
