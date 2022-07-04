package org.example.global;

import org.apache.commons.lang3.StringUtils;

public class DataSourceConfigEntity {

    private String mongoServers = "localhost:27017";
    private String mongoUserName = "NULL";
    private String mongoPassword = "NULL";
    private String mongoDatabase = "ptool";

    private String driverClassName = "org.postgresql.Driver";
    private String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    private String jdbcUsername = "postgres";
    private String jdbcPassword = "postgres";

    public DataSourceConfigEntity(String mongoServers, String mongoUserName, String mongoPassword, String mongoDatabase,
                                  String driverClassName, String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        setMongoServers(mongoServers);
        setMongoUserName(mongoUserName);
        setMongoPassword(mongoPassword);
        setMongoDatabase(mongoDatabase);
        setDriverClassName(driverClassName);
        setJdbcUrl(jdbcUrl);
        setJdbcUsername(jdbcUsername);
        setJdbcPassword(jdbcPassword);
    }

    public String getMongoServers() {
        return mongoServers;
    }

    public void setMongoServers(String mongoServers) {
        if (StringUtils.isNotEmpty(mongoServers)) {
            this.mongoServers = mongoServers;
        }
    }

    public String getMongoUserName() {
        return mongoUserName;
    }

    public void setMongoUserName(String mongoUserName) {
        if (StringUtils.isNotEmpty(mongoUserName)) {
            this.mongoUserName = mongoUserName;
        }
    }

    public String getMongoPassword() {
        return mongoPassword;
    }

    public void setMongoPassword(String mongoPassword) {
        if (StringUtils.isNotEmpty(mongoPassword)) {
            this.mongoPassword = mongoPassword;
        }
    }

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(String mongoDatabase) {
        if (StringUtils.isNotEmpty(mongoDatabase)) {
            this.mongoDatabase = mongoDatabase;
        }
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        if (StringUtils.isNotEmpty(driverClassName)) {
            this.driverClassName = driverClassName;
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        if (StringUtils.isNotEmpty(jdbcUrl)) {
            this.jdbcUrl = jdbcUrl;
        }
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public void setJdbcUsername(String jdbcUsername) {
        if (StringUtils.isNotEmpty(jdbcUsername)) {
            this.jdbcUsername = jdbcUsername;
        }
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        if (StringUtils.isNotEmpty(jdbcPassword)) {
            this.jdbcPassword = jdbcPassword;
        }
    }

}
