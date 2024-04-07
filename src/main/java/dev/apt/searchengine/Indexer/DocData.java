package dev.apt.searchengine.Indexer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocData {
    private Integer termFrequency;
    private Integer priority;
    private String title;
    private String snippet;
};
