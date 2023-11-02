package com.hilcoe.semanticsearchengine;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class SearchService {
    private final String indexDir = "./indexDir";

    public void queryParserSearch(String fieldName, String queryString) throws IOException {
        try(IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir).toPath()))){
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser(fieldName,new StandardAnalyzer());
            Query query = queryParser.parse(queryString);
            TopDocs hits = indexSearcher.search(query,10);

            System.out.println("Searching for ["+queryString+"]:");
            System.out.println("The total number of hits = "+hits.totalHits);
            showQueryResult(indexSearcher,hits);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void showQueryResult(IndexSearcher indexSearcher, TopDocs hits) throws IOException {
        for (int i=0; i<hits.scoreDocs.length; i++){
            int docId = hits.scoreDocs[i].doc;
            Document doc = indexSearcher.doc(docId);

            System.out.println("Document ID: "+docId);
            System.out.println("Brand: "+doc.get("brand"));
            System.out.println("Producer: "+doc.get("producer"));
            System.out.println("Flavor: "+doc.get("flavor"));
        }
    }

}
