package dev.apt.searchengine.QueryProcessor;

import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/query")
public class QueryProcessor {
    String query;
    @PostMapping
    public String processQuery(@RequestBody String query) {
        this.query = query;
        System.out.println(query);
        return "Query processed";
    }
}
