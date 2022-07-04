package org.example.global;

import org.example.aspect.entity.PrivacyLifeCycleEntity;
import org.example.dao.nosql.mongo.MongoDbTemplate;
import org.example.dao.sql.DbTemplate;

public class CurrentGlobalData {
    private PrivacyLifeCycleEntity plcEntity;

    private DbType dbType;

    private MongoDbTemplate mongoDbTemplate;

    private DbTemplate sqlDbTemplate;

    private boolean isNeedAuth;

    public CurrentGlobalData() {}

    public void setPlcEntity(PrivacyLifeCycleEntity plcEntity) {
        this.plcEntity = plcEntity;
    }

    public PrivacyLifeCycleEntity getPlcEntity() {
        return plcEntity;
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public MongoDbTemplate getMongoDbTemplate() {
        return mongoDbTemplate;
    }

    public void setMongoDbTemplate(MongoDbTemplate mongoDbTemplate) {
        this.mongoDbTemplate = mongoDbTemplate;
    }

    public DbTemplate getSqlDbTemplate() {
        return sqlDbTemplate;
    }

    public void setSqlDbTemplate(DbTemplate sqlDbTemplate) {
        this.sqlDbTemplate = sqlDbTemplate;
    }

    public boolean isNeedAuth() {
        return isNeedAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        isNeedAuth = needAuth;
    }
}
