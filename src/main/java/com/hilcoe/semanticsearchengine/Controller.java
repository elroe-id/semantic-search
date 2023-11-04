package com.hilcoe.semanticsearchengine;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@org.springframework.stereotype.Controller
public class Controller {
    private final Indexer indexer;
    private final SearchService searchService;

    private boolean indexed=false;

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
    public String search(@RequestParam(required = false) String queryString, Model model) {
        if(queryString!=null) {
            try {
                if(!indexed) {
                    indexer.createIndex();
                    indexed=true;
                }
                String fieldName = "description";
                List<String> searchResult = searchService.queryParserSearch(fieldName, queryString);
                model.addAttribute("searchResult", searchResult);
                model.addAttribute("queryString",queryString);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return "search";
    }
}
