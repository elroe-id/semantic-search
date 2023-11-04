package com.hilcoe.semanticsearchengine;


import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;
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



import org.apache.jena.vocabulary.*;
//import org.apache.jena.vocabulary.SchemaOrg;




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
        doc.add(new TextField("everything", brand+description+producer+flavor, Field.Store.NO));
        doc.add(new TextField("producer", producer, Field.Store.YES));
        doc.add(new TextField("flavor", flavor, Field.Store.YES));
        doc.add(new Field("price", price, fieldType));
        doc.add(new Field("url", url, fieldType));

        String rdfFolderPath = "./rdf/";
        String fileName = file.getName();
        String mfile  = rdfFolderPath + fileName.replace(".txt", ".ttl");

        doc.add(new Field("rs",getRichSnippets(mfile),fieldType));
        return doc;
    }


    private String getRichSnippets(String fpath) {
        String snippets = "";

        // Load model from file
        Model model = ModelFactory.createDefaultModel();

        try {
            model.read(fpath);
        } catch (Exception e) {
            // Log exception
            System.out.println("Failed to read RDF file: ");
            return "";
        }

        // Get the main subject
        Resource mainSubject = null;
        Property schemaAbout = ResourceFactory.createProperty("http://schema.org/about");
        StmtIterator stmts = model.listStatements(null, schemaAbout, (RDFNode) null);

        if (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            mainSubject = stmt.getSubject();
        }

        if (mainSubject == null) {
            System.out.println("Main subject not found.");
            return snippets;
        }

        // Get description
        Property schemaDescription = ResourceFactory.createProperty("http://schema.org/description");
        StmtIterator descriptionStmts = model.listStatements(mainSubject, schemaDescription, (RDFNode) null);
        if (descriptionStmts.hasNext()) {
            Statement stmt = descriptionStmts.nextStatement();
            Literal description = stmt.getLiteral();
            snippets += "Description: " + description.getString() + "\n";
        }

        // Get additional info
        snippets += getAdditionalInfo(mainSubject, model);

        return snippets;
    }



    private String getAdditionalInfo(Resource resource, Model model) {
        String info = "";

        StmtIterator stmts = model.listStatements(resource, null, (RDFNode) null);

        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            Property property = stmt.getPredicate();
            RDFNode obj = stmt.getObject();

            // Check if the object is a literal or a resource
            if (obj.isLiteral()) {
                Literal literal = obj.asLiteral();
                info += property.getLocalName() + ": " + literal.getString() + "\n";
            } else if (obj.isResource()) {
                Resource nestedResource = obj.asResource();
                info += property.getLocalName() + ": " + getDescription(nestedResource, model) + "\n";
            }
        }

        return info;
    }

    private String getDescription(Resource resource, Model model) {
        String desc = "";

        Property schemaName = ResourceFactory.createProperty("http://schema.org/name");
        NodeIterator nodes = model.listObjectsOfProperty(resource, schemaName);

        while (nodes.hasNext()) {
            RDFNode node = nodes.nextNode();
            if (node.isLiteral()) {
                Literal name = node.asLiteral();
                desc += name.getString() + "; ";
            }
        }

        return desc;
    }



//        return descriptionBuilder.toString();
    //   }
}

