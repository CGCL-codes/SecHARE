package org.example.dao.nosql.mongo.impl;

import org.example.dao.nosql.mongo.MongoDbTemplate;
import org.example.dao.nosql.mongo.entity.MappingTable;
import org.example.protect.entity.PrivacyMappingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Slf4j
public class MongoDbTemplateImpl implements MongoDbTemplate {

    private final MongoTemplate mongoTemplate;

    public MongoDbTemplateImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void createCollection(String collectionName) {
        try {
            if (!mongoTemplate.collectionExists(collectionName)) {
                mongoTemplate.createCollection(collectionName);
            }
        } catch (UncategorizedMongoDbException ex) {
            log.warn("Failed to create collection {} due to", collectionName, ex);
        }
    }

    @Override
    public MappingTable save(PrivacyMappingEntity privacyMappingEntity, String collectionName) {
        MappingTable mappingTable = new MappingTable(privacyMappingEntity);
        log.debug("Saving mappingTable info");

        mongoTemplate.insert(mappingTable, collectionName);
//        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//        String json = JSON.toJSONString(mappingTable);
//        Document document = Document.parse(json);
//        collection.insertOne(document);

        return mappingTable;
    }

    @Override
    public void updateFirst(Query query, Update update, String collectionName) {
        log.debug("updateFirst document of collection [{}] ", collectionName);
        mongoTemplate.updateFirst(query, update, MappingTable.class, collectionName);
    }

    @Override
    public void updateMulti(Query query, Update update, String collectionName) {
        log.debug("updateMulti document of collection [{}] ", collectionName);
        mongoTemplate.updateMulti(query, update, MappingTable.class, collectionName);
    }

    @Override
    public MappingTable findById(String key, String collectionName) {
        log.debug("Find document of collection [{}] by id [{}]", MappingTable.class, key);
        return mongoTemplate.findById(key, MappingTable.class, collectionName);
    }

    @Override
    public MappingTable findOne(Query query, String collectionName) {
        log.debug("Find one document of collection [{}] ", collectionName);
        return mongoTemplate.findOne(query, MappingTable.class, collectionName);
    }

    @Override
    public List<MappingTable> find(Query query, String collectionName) {
        log.debug("Find document of collection [{}] ", collectionName);
        return mongoTemplate.find(query, MappingTable.class, collectionName);
    }

    @Override
    public void removeById(String key, String collectionName) {
        log.debug("Remove document of collection [{}] by id [{}]", collectionName, key);
        MappingTable object = findById(key, collectionName);
        if (object != null) {
            mongoTemplate.remove(object, collectionName);
        }
    }

    @Override
    public void remove(Query query, String collectionName) {
        log.debug("Remove document of collection [{}] ", collectionName);
        mongoTemplate.remove(query, MappingTable.class, collectionName);
    }

    @Override
    public void removeAll(String collectionName) {
        log.debug("Remove all documents from [{}] collection.", collectionName);
        mongoTemplate.dropCollection(collectionName);
    }

}
