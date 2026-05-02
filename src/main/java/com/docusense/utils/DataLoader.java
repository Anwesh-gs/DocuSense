package com.docusense.utils; // This file belongs to the utils package

import org.springframework.beans.factory.annotation.Autowired; // Import our Category model
import org.springframework.boot.CommandLineRunner; // Import our CategoryRepository
import org.springframework.stereotype.Component; // Import EmbeddingService to generate embeddings

import com.docusense.model.Category;
import com.docusense.model.CategoryRepository; // Runs code automatically when app starts
import com.docusense.service.EmbeddingService; // Marks this as a Spring managed component

@Component // Tells Spring to manage this class automatically
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository; // Spring injects repository automatically

    @Autowired
    private EmbeddingService embeddingService; // Spring injects embedding service automatically

    @Override
    public void run(String... args) throws Exception {

        // Only load categories if the table is empty
        if (categoryRepository.count() == 0) {

            // Create OS category
            Category os = new Category();
            os.setName("OS");
            os.setKeywords("process,thread,memory,kernel,scheduling,deadlock,semaphore,cpu,virtual memory,paging,segmentation,file system,interrupt,context switch");

            // Generate AI embedding for OS keywords
            // This converts the keywords into 384 numbers representing their meaning
            os.setEmbedding(embeddingService.getEmbedding(os.getKeywords()));
            categoryRepository.save(os); // Save to database

            // Create DBMS category
            Category dbms = new Category();
            dbms.setName("DBMS");
            dbms.setKeywords("database,sql,table,query,normalization,index,transaction,join,primary key,foreign key,schema,relational,trigger,stored procedure");
            dbms.setEmbedding(embeddingService.getEmbedding(dbms.getKeywords()));
            categoryRepository.save(dbms);

            // Create Networks category
            Category networks = new Category();
            networks.setName("Networks");
            networks.setKeywords("tcp,ip,router,protocol,bandwidth,network,packet,dns,http,firewall,subnet,osi,ethernet,socket,latency");
            networks.setEmbedding(embeddingService.getEmbedding(networks.getKeywords()));
            categoryRepository.save(networks);

            // Create AI category
            Category ai = new Category();
            ai.setName("AI");
            ai.setKeywords("machine learning,neural network,deep learning,model,training,classification,regression,clustering,artificial intelligence,gradient,backpropagation,dataset,feature,prediction");
            ai.setEmbedding(embeddingService.getEmbedding(ai.getKeywords()));
            categoryRepository.save(ai);

            System.out.println("✅ Categories with embeddings loaded successfully!");

        } else {
            System.out.println("✅ Categories already exist, skipping load.");
        }
    }
}