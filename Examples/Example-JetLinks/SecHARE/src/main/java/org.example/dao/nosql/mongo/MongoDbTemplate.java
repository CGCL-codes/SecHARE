package org.example.dao.nosql.mongo;

import org.example.dao.nosql.mongo.entity.MappingTable;
import org.example.protect.entity.PrivacyMappingEntity;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public interface MongoDbTemplate {

    void createCollection(String collectionName);

    MappingTable save(PrivacyMappingEntity privacyMappingEntity, String collectionName);

    void updateFirst(Query query, Update update, String collectionName);

    void updateMulti(Query query, Update update, String collectionName);

    MappingTable findById(String key, String collectionName);

    MappingTable findOne(Query query, String collectionName);

    List<MappingTable> find(Query query, String collectionName);

    void removeById(String key, String collectionName);

    void remove(Query query, String collectionName);

    void removeAll(String collectionName);
}
