package org.example.global;

import org.example.aspect.entity.PrivacyElemEntity;
import org.example.aspect.entity.PrivacyLifeCycleEntity;
import org.example.aspect.entity.PrivacySensitiveInfoEntity;
import org.example.dao.nosql.mongo.impl.MongoDbTemplateImpl;
import org.example.dao.sql.impl.DbTemplateImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

@Slf4j
public class PrivacyGlobalValue {

    private static final CurrentGlobalData CURRENT_THREAD_DATA = new CurrentGlobalData();

    static {
        init();
    }

    public static CurrentGlobalData get() {
        return CURRENT_THREAD_DATA;
    }

    private static void init() {
        Properties properties = new Properties();
        PrivacyLifeCycleEntity plcEntity = new PrivacyLifeCycleEntity();

        // try (InputStream in = Object.class.getClassLoader().getResourceAsStream("privacyconfig.properties")) {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("privacyconfig.properties")) {

            properties.load(in);
            // dbType
            CURRENT_THREAD_DATA.setDbType(DbType.parse(properties.getProperty("privacy.dbType")));

            // DbTemplate
            // dataSource config
            DataSourceConfigEntity dataSourceConfigEntity = new DataSourceConfigEntity(
                    properties.getProperty("mongo.address"), properties.getProperty("mongo.username"),
                    properties.getProperty("mongo.password"), properties.getProperty("mongo.database"),
                    properties.getProperty("spring.datasource.driverClassName"), properties.getProperty("spring.datasource.url"),
                    properties.getProperty("spring.datasource.username"), properties.getProperty("spring.datasource.password"));
            CURRENT_THREAD_DATA.setMongoDbTemplate(creatMongoTemplate(CURRENT_THREAD_DATA.getDbType(), dataSourceConfigEntity));
            CURRENT_THREAD_DATA.setSqlDbTemplate(createSqlDataSource(CURRENT_THREAD_DATA.getDbType(), dataSourceConfigEntity));

            String regexComma = ",";
            // addDevice
            PrivacyElemEntity peeDeviceAdd = new PrivacyElemEntity();
            peeDeviceAdd.setMethodOrClassPath(properties.getProperty("privacy.addDevice.methodPath"));
            peeDeviceAdd.setFieldInfo(properties.getProperty("privacy.addDevice.idInfo"));
            plcEntity.setAddDevice(peeDeviceAdd);
            // deleteDevice
            PrivacyElemEntity peeDeviceDel = new PrivacyElemEntity();
            peeDeviceDel.setMethodOrClassPath(properties.getProperty("privacy.deleteDevice.methodPath"));
            peeDeviceDel.setFieldInfo(properties.getProperty("privacy.deleteDevice.idInfo"));
            plcEntity.setDeleteDevice(peeDeviceDel);
            // deleteUser
            PrivacyElemEntity peeUserDel = new PrivacyElemEntity();
            peeUserDel.setMethodOrClassPath(properties.getProperty("privacy.deleteUser.methodPath"));
            peeUserDel.setFieldInfo(properties.getProperty("privacy.deleteUser.idInfo"));
            plcEntity.setDeleteUser(peeUserDel);
            // grantAuth
            plcEntity.setGrantAuth(getPrivacyElemArray(properties.getProperty("privacy.grantAuth.methodPath"),
                    properties.getProperty("privacy.grantAuth.idInfo"), regexComma));
            // RevokeAuth
            plcEntity.setRevokeAuth(getPrivacyElemArray(properties.getProperty("privacy.revokeAuth.methodPath"),
                    properties.getProperty("privacy.revokeAuth.idInfo"), regexComma));
            // Authority Name
            plcEntity.setAuthorityName(properties.getProperty("privacy.authority.name").split(regexComma));
            // Sensitive Info Operation
            plcEntity.setSenInfoOperateEntity(getPrivacySenOperateArray(
                    properties.getProperty("privacy.senInfo.fieldName"),
                    properties.getProperty("privacy.senInfo.classPath"),
                    properties.getProperty("privacy.updateInfo.methodPath"),
                    properties.getProperty("privacy.updateInfo.protectedAndIdInfo"),
                    properties.getProperty("privacy.protectedInfo.methodPath"),
                    properties.getProperty("privacy.protectedInfo.protectedAndIdInfo"),
                    properties.getProperty("privacy.verifyInfo.methodPath"),
                    properties.getProperty("privacy.verifyInfo.protectedInfo")));

        } catch (Exception e) {
            log.error("Privacy Tool reading configuration file Error! {}", e.getMessage(), e);
        }
        CURRENT_THREAD_DATA.setPlcEntity(plcEntity);
        // isNeedAuth
        CURRENT_THREAD_DATA.setNeedAuth(ArrayUtils.isNotEmpty(plcEntity.getGrantAuth()) ||
                ArrayUtils.isNotEmpty(plcEntity.getRevokeAuth()));
    }

    private static DbTemplateImpl createSqlDataSource(DbType dbType, DataSourceConfigEntity entity) {

        if (dbType.equals(DbType.SQL)) {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(entity.getDriverClassName());
            config.setJdbcUrl(entity.getJdbcUrl());
            config.setUsername(entity.getJdbcUsername());
            config.setPassword(entity.getJdbcPassword());

            config.addDataSourceProperty("autoCommit", "false");
            DataSource dataSource = new HikariDataSource(config);
            return new DbTemplateImpl(dataSource);
        }
        return new DbTemplateImpl(null);
    }

    private static MongoDbTemplateImpl creatMongoTemplate(DbType dbType, DataSourceConfigEntity entity) {
        if (dbType.equals(DbType.MONGODB)) {
            String connectionString;
            String nullStr = "NULL";
            if (nullStr.equals(entity.getMongoUserName()) || nullStr.equals(entity.getMongoPassword())) {
                connectionString = "mongodb://" + entity.getMongoServers() + "/" + entity.getMongoDatabase();
            } else {
                String credential = entity.getMongoUserName() + ":" + entity.getMongoPassword() + "@";
                connectionString = "mongodb://" + credential + entity.getMongoServers() + "/" + entity.getMongoDatabase();
            }

            MongoDatabaseFactory mdFactory = new SimpleMongoClientDatabaseFactory(connectionString);
            MongoTemplate mongoTemplate = new MongoTemplate(mdFactory);
//            MongoTemplate mongoTemplate = new MongoTemplate(new Mongo(new MongoURI(connectionString)), entity.getMongoDatabase());
            mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);

            return new MongoDbTemplateImpl(mongoTemplate);
        }
        return new MongoDbTemplateImpl(null);
    }

    private static PrivacyElemEntity[] getPrivacyElemArray(String pmn, String ifn, String splitRegex) {
        String[] pmnArray = new String[0];
        String[] ifnArray = new String[0];
        if(StringUtils.isNotEmpty(pmn) && StringUtils.isNotEmpty(ifn)) {
            pmnArray = pmn.split(splitRegex);
            ifnArray = ifn.split(splitRegex);
        }
        ArrayList<PrivacyElemEntity> peeList = new ArrayList<>();
        for(int i = 0; i < pmnArray.length; i++) {
            if(StringUtils.isNotEmpty(pmnArray[i]) && StringUtils.isNotEmpty(ifnArray[i])) {
                PrivacyElemEntity pee = new PrivacyElemEntity(pmnArray[i].trim(), ifnArray[i].trim());
                peeList.add(pee);
            }
        }
        return peeList.toArray(new PrivacyElemEntity[0]);
    }

    private static PrivacySensitiveInfoEntity[] getPrivacySenOperateArray(String senInfoField, String senInfoClassPath,
                                                                          String updateInfoPackage, String updateInfoId,
                                                                          String protectedInfoPackage, String protectedInfoId,
                                                                          String verifyInfoPackage, String verifyInfoId) {

        ArrayList<PrivacySensitiveInfoEntity> pseList = new ArrayList<>();

        String regexComma = ",";
        String regexHyphen = "/";

        String[] senInfoFieldArr = getSplitStr(senInfoField, regexComma);
        String[] senInfoClassArr = getSplitStr(senInfoClassPath, regexComma);
        String[] updateInfoPackageArr = getSplitStr(updateInfoPackage, regexComma);
        String[] updateInfoIdArr = getSplitStr(updateInfoId, regexComma);
        String[] protectedInfoPackageArr = getSplitStr(protectedInfoPackage, regexComma);
        String[] protectedInfoIdArr = getSplitStr(protectedInfoId, regexComma);
        String[] verifyInfoPackageArr = getSplitStr(verifyInfoPackage, regexComma);
        String[] verifyProtectedInfo = getSplitStr(verifyInfoId, regexComma);

        for (int i = 0; i < senInfoFieldArr.length; i++) {
            PrivacySensitiveInfoEntity pse = new PrivacySensitiveInfoEntity();

            pse.setSensitiveInfo(new PrivacyElemEntity(
                    getIndexOfArray(senInfoClassArr, i), getIndexOfArray(senInfoFieldArr, i)));
            pse.setUpdateSenInfo(getPrivacyElemArray(
                    getIndexOfArray(updateInfoPackageArr, i), getIndexOfArray(updateInfoIdArr, i), regexHyphen));
            pse.setGetProtectedInfo(getPrivacyElemArray(
                    getIndexOfArray(protectedInfoPackageArr, i), getIndexOfArray(protectedInfoIdArr, i), regexHyphen));
            pse.setVerifySenInfo(getPrivacyElemArray(
                    getIndexOfArray(verifyInfoPackageArr, i), getIndexOfArray(verifyProtectedInfo, i), regexHyphen));

            pseList.add(pse);
        }

        return pseList.toArray(new PrivacySensitiveInfoEntity[0]);
    }

    private static String[] getSplitStr(String str, String regexComma) {
        if (StringUtils.isNotEmpty(str)) {
            return str.split(regexComma);
        }
        return new String[0];
    }

    private static String getIndexOfArray(String[] strArray, int i) {
        if (ArrayUtils.isNotEmpty(strArray) && strArray.length > i) {
            return strArray[i];
        }
        return null;
    }

}
