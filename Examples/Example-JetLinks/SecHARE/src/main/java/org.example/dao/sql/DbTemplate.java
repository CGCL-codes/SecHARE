package org.example.dao.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DbTemplate {

    boolean isTableExisted(String tableName);

    int createTable(String tableName) throws SQLException;

    int insert(String tableName, Map<String, Object> valueMap) throws SQLException;

    int delete(String tableName, Map<String, Object> whereMap) throws SQLException;

    int update(String tableName, Map<String, Object> setValueMap, Map<String, Object> whereMap) throws SQLException;

    List<Map<String, Object>> select(String tableName, Map<String, Object> whereMap) throws SQLException;

    List<Map<String, Object>> select(String tableName, String whereClause, String[] whereArgs) throws SQLException;

    List<Map<String, Object>> select(String tableName,
                                     boolean distinct,
                                     String[] columns,
                                     String selection,
                                     Object[] selectionArgs,
                                     String groupBy,
                                     String having,
                                     String orderBy,
                                     String limit) throws SQLException;
}
