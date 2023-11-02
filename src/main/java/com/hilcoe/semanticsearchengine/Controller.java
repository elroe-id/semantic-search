package com.hilcoe.semanticsearchengine;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {
    private final Indexer indexer;
    private final SearchService searchService;

    public Controller(Indexer indexer, SearchService searchService) {
        this.indexer = indexer;
        this.searchService = searchService;
    }

    @GetMapping("/indexData")
    public String indexData(){
        try{
            indexer.createIndex();
            return "Indexing completed successfully!";
        } catch (IOException e) {
            return "Indexing failed: "+e.getMessage();
        }
    }

    @GetMapping("/search")
    public void search(@RequestParam String queryString){
        try{
            String fieldName = "description";
            searchService.queryParserSearch(fieldName,queryString);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
