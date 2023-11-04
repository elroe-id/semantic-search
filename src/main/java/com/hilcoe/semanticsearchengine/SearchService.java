package com.hilcoe.semanticsearchengine;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Service
public class SearchService {
    private final String indexDir = "./indexDir";
    List<String> results;

    public List<String> queryParserSearch(String fieldName, String queryString) throws IOException {
        results = new ArrayList<>();
        try(IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir).toPath()))){
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser(fieldName,new StandardAnalyzer());
            Query query = queryParser.parse(queryString);
            TopDocs hits = indexSearcher.search(query,10);

            System.out.println("Searching for ["+queryString+"]:");
            System.out.println("The total number of hits = "+hits.totalHits);
//            result = showQueryResult(indexSearcher,hits);
            showQueryResultWithHighlight(indexSearcher,hits,query,fieldName);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (InvalidTokenOffsetsException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private String getHighlightText(Query query,Analyzer analyzer, String fieldName, String text) throws InvalidTokenOffsetsException, IOException {
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">","</span>");
        TokenStream tokenStream = analyzer.tokenStream(fieldName,new StringReader(text));
        QueryScorer queryScorer = new QueryScorer(query, fieldName);
        Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
        Highlighter highlighter = new Highlighter(formatter,queryScorer);
        highlighter.setTextFragmenter(fragmenter);
        String tmp = highlighter.getBestFragment(tokenStream,text);
        String result = "<style>\n .highlight {\n background:yellow; \n}\n</style>"+tmp;
        return result;
    }

    private String getRichSnippets(Document doc, String fieldname){
        String richSnippets = doc.get(fieldname);
        if(richSnippets==null || richSnippets.length()==0)
            return "";
        String style = "<style>\n.richSnippets{\ncolor:gray;\n}\n</style>";
        if(richSnippets.contains("\n")==false){
            return style+"<br><span class=\"richSnippets\">"+richSnippets+"</span></br>";
        }
        String res = "";
        StringTokenizer rs = new StringTokenizer(richSnippets,"\n");
        while (rs.hasMoreElements()){
            res+=(String)(rs.nextElement())+"</br>";
        }
        return style+"<br><span class=\"richSnippets\">"+res+"</span></br>";
    }

    private void showQueryResultWithHighlight(IndexSearcher indexSearcher, TopDocs hits, Query query, String fieldName) throws IOException, InvalidTokenOffsetsException {
        String res="";
        Analyzer analyzer = new StandardAnalyzer();
        for (int i=0; i<hits.scoreDocs.length; i++) {
            int docId = hits.scoreDocs[i].doc;
            Document doc = indexSearcher.doc(docId);
            String url = "<br><a href=\""+doc.get("url")+"\">"+doc.get("url")+"</a></br>";
            String tmpRes = url + getHighlightText(query,analyzer,fieldName,doc.get(fieldName));
            results.add(tmpRes+getRichSnippets(doc,fieldName));
        }
    }

    private String showQueryResult(IndexSearcher indexSearcher, TopDocs hits) throws IOException {
        String result = "";
        for (int i=0; i<hits.scoreDocs.length; i++){
            int docId = hits.scoreDocs[i].doc;
            Document doc = indexSearcher.doc(docId);
            result += "\n\nDocument ID: "+docId+"\nBrand: "+doc.get("brand")+"\nProducer: "+doc.get("producer")+"\nFlavor: "+doc.get("flavor")+"\n\nRich Snippets: \n"+doc.get("rs");
//            System.out.println("Document ID: "+docId);
//            System.out.println("Brand: "+doc.get("brand"));
//            System.out.println("Producer: "+doc.get("producer"));
//            System.out.println("Flavor: "+doc.get("flavor"));
//            System.out.println("Rich Snippets: "+doc.get("rs"));
        }
        return result;
    }

}
