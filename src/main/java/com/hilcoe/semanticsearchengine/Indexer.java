package com.hilcoe.semanticsearchengine;


import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.jena.enhanced.BuiltinPersonalities.model;
import static org.apache.jena.reasoner.rulesys.impl.WrappedReasonerFactory.schemaURL;

@Service
public class Indexer {
    private final String dataDir="./dataDir";
    private final String indexDir = "./indexDir";
//    private String indexPath, dataSource;
//
//    public Indexer(String indexPath, String dataSource) {
//        this.indexPath = indexPath;
//        this.dataSource = dataSource;
//    }

//    public String getIndexPath() {
//        return indexPath;
//    }
//
//    public void setIndexPath(String indexPath) {
//        this.indexPath = indexPath;
//    }
//
//    public String getDataSource() {
//        return dataSource;
//    }
//
//    public void setDataSource(String dataSource) {
//        this.dataSource = dataSource;
//    }

    public void createIndex() throws IOException {
        List<File> results = new ArrayList<File>();
        findFiles(results,new File(dataDir));
        System.out.println(results.size()+" articles to index");

        Directory directory= FSDirectory.open(new File(indexDir).toPath());
        IndexWriterConfig iwconf = new IndexWriterConfig(new StandardAnalyzer());
        iwconf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(directory,iwconf);

        for(File file : results){
            Document doc = getDocument(file);
            indexWriter.addDocument(doc);
        }

        indexWriter.close();
        directory.close();
    }

    private void findFiles(List<File> results, File dataDir){
        if(dataDir != null && dataDir.isDirectory()){
            File[] files = dataDir.listFiles();
            if(files != null){
                for(File file : files){
                    if(file.isFile()){
                        results.add(file);
                    }
                }
            }
        }
    }

    private Document getDocument(File file) throws IOException {
        Document doc = new Document();
        Properties props = new Properties();
        props.load(new FileInputStream(file));

        String brand = props.getProperty("brand");
        String producer = props.getProperty("producer");
        String flavor = props.getProperty("flavor");
        String price = props.getProperty("price");
        String description = props.getProperty("description");
        String url = props.getProperty("url");


        FieldType fieldType = new FieldType();
        fieldType.setTokenized(false);
        fieldType.setStored(true);

        doc.add(new TextField("brand", brand, Field.Store.YES));
        doc.add(new TextField("description", description, Field.Store.YES));
        doc.add(new TextField("everything", brand+description, Field.Store.NO));
        doc.add(new Field("producer", producer, fieldType));
        doc.add(new Field("flavor", flavor, fieldType));
        doc.add(new Field("price", price, fieldType));
        doc.add(new Field("url", url, fieldType));

//        String mfile = file.getAbsolutePath().replaceAll("txt","ttl");
//        doc.add(new Field("rs",getRichSnippets(mfile),fieldType));
        return doc;
    }

//    private String getRichSnippets(String fpath){
//        String richSnippets ="";
//        updateModel(fpath);
//
//        RDFNode rootSubject = null;
//        Property pAbout = ResourceFactory.createProperty(schemaAbout);
//        StmtIterator st = model.listStatements(null,pAbout,(RDFNode)null);
//
//        while(st.hasNext()){
//            Statement statement=st.nextStatement();
//            rootSubject = statement.getSubject();
//            break;
//        }
//        if(rootSubject==null)
//            return richSnippets;
//
//        Resource rootType =null;
//        st = model.listStatements((Resource)rootSubject, RDF.type,(RDFNode)null);
//        while(st.hasNext()){
//            Statement statement=st.nextStatement();
//            rootType = (Resource) statement.getObject();
//            break;
//        }
//        richSnippets+=rootType.getLocalName()+": ";
//
//        Property pDescription = ResourceFactory.createProperty(schemaDescription);
//        st = model.listStatements((Resource)rootSubject, pDescription,(RDFNode)null);
//        while(st.hasNext()){
//            Statement statement=st.nextStatement();
//            if(statement.getObject().isLiteral()){
//                richSnippets+=statement.getObject().asLiteral().getString()+"\n";
//            }
//            break;
//        }
//
//        String description = "";
//        NodeIterator nodes = model.listObjectsOfProperty(pAbout);
//        while(nodes.hasNext()){
//            description+="About: "+getDescription(nodes.next(),true)+"\n";
//        }
//
//        richSnippets+=description;
//        return richSnippets;
//    }
//
//    private String getDescription(RDFNode node, boolean showType){
//        String description = "";
//        StmtIterator st = model.listStatements((Resource)node,RDF.type,(RDFNode)null);
//        while(st.hasNext()){
//            Statement statement=st.nextStatement();
//            if(showType){
//                description +="["+((Resource)(statement.getObject())).getLocalName()+"] ";
//            }
//            break;
//        }
//
//        st=model.listStatements((Resource)node,null,(RDFNode)null);
//
//        while(st.hasNext()){
//            Statement statement = st.nextStatement();
//            if(statement.getPredicate().getURI().equalsIgnoreCase((RDF.type.getURI()))){
//                continue;
//            }
//            if(statement.getPredicate().getURI().equalsIgnoreCase(schemaURL)){
//                continue;
//            }
//            RDFNode objectNode = statement.getObject();
//            if(objectNode.isLiteral()){
//                description+=objectNode.asLiteral().getString()+";";
//            }
//            else{
//                description+=getDescription(objectNode,false);
//            }
//        }
//        return description.substring(0,description.length()-2);
//    }

}
