package org.example.dao.sql.impl;

import org.example.dao.sql.DbTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class DbTemplateImpl implements DbTemplate {

    private final DataSource dataSource;

    private final Pattern sLimitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");

    public DbTemplateImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int createTable(String tableName) throws SQLException {
        String sb = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id varchar(255) NOT NULL," +
                "created_time int NOT NULL," +
                "device_id VARCHAR(255) DEFAULT NULL," +
                "user_id VARCHAR(255) DEFAULT NULL," +
                "real_info text DEFAULT NULL," +
                "shadow_info text DEFAULT NULL," +
                "remarks text DEFAULT NULL," +
                "PRIMARY KEY (id)" +
                ");";

        return insDelUpd(sb, null);
    }

    @Override
    public int insert(String tableName, Map<String, Object> valueMap) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> objectList = new ArrayList<>();

        StringBuilder columnSql = new StringBuilder();
        StringBuilder valueQm = new StringBuilder();

        for(String key : valueMap.keySet()) {
            columnSql.append(key).append(", ");
            valueQm.append("?").append(", ");
            objectList.add(valueMap.get(key));
        }
        columnSql.deleteCharAt(columnSql.length()-2);
        valueQm.deleteCharAt(valueQm.length()-2);

        sql.append("INSERT INTO ")
                .append(tableName)
                .append(" (").append(columnSql)
                .append(" )  VALUES (")
                .append(valueQm)
                .append(" )");

        return insDelUpd(sql.toString(), objectList.toArray());
    }

    @Override
    public int delete(String tableName, Map<String, Object> whereMap) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> objectList = new ArrayList<>();

        sql.append("DELETE FROM ").append(tableName);

        if (whereMap != null && whereMap.size() > 0) {
            sql.append(" WHERE ");

            for(String key : whereMap.keySet()) {
                sql.append(key).append(" = ? ").append(" AND ");
                objectList.add(whereMap.get(key));
            }
            sql.delete(sql.length() - 5, sql.length());

            return insDelUpd(sql.toString(), objectList.toArray());
        }
        return -1;
    }

    @Override
    public int update(String tableName,
                      Map<String, Object> setValueMap,
                      Map<String, Object> whereMap) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> objectList = new ArrayList<>();

        sql.append("UPDATE ").append(tableName).append(" SET ");
        for(String key : setValueMap.keySet()) {
            sql.append(key).append(" = ? ").append(", ");
            objectList.add(setValueMap.get(key));
        }
        sql.delete(sql.length() - 2, sql.length());

        if(whereMap != null && whereMap.size() > 0) {
            sql.append(" WHERE ");
            for(String key : whereMap.keySet()) {
                sql.append(key).append(" = ? ").append(" AND ");
                objectList.add(whereMap.get(key));
            }
            sql.delete(sql.length() - 5, sql.length());
        }

        return insDelUpd(sql.toString(), objectList.toArray());
    }

    @Override
    public List<Map<String, Object>> select(String tableName,
                                            Map<String, Object> whereMap) throws SQLException {
        StringBuilder whereClause = new StringBuilder();
        List<Object> whereArgs = new ArrayList<>();
        if (whereMap != null && whereMap.size() > 0) {
            for(String key : whereMap.keySet()) {
                whereClause.append(key).append(" = ? ").append("AND ");
                whereArgs.add(whereMap.get(key));
            }
            whereClause.delete(whereClause.length() - 4, whereClause.length());
        }
        return select(tableName, false, null, whereClause.toString(), whereArgs.toArray(), null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> select(String tableName,
                                            String whereClause,
                                            String[] whereArgs) throws SQLException {
        return select(tableName, false, null, whereClause, whereArgs, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> select(String tableName,
                                            boolean distinct,
                                            String[] columns,
                                            String selection,
                                            Object[] selectionArgs,
                                            String groupBy,
                                            String having,
                                            String orderBy,
                                            String limit) throws SQLException {
        String sql = generateSql(distinct, tableName, columns, selection, groupBy, having, orderBy, limit);
        return execSel(sql, selectionArgs);
    }

    @Override
    public boolean isTableExisted(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            DatabaseMetaData dbMetaData = conn.getMetaData();
            String[] types  = { "TABLE" };
            rs = dbMetaData.getTables(null, null, tableName, types);
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            log.error("PrivacyTool sql judge table existed: {}", e.getMessage(), e);
        }finally{
            close(conn, null, rs);
        }
        return false;
    }

    private int insDelUpd(String sql, Object[] bindArgs) throws SQLException{
        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            conn.setAutoCommit(false);
            if (bindArgs != null) {
                for (int i = 0; i < bindArgs.length; i++) {
                    pst.setObject(i + 1, bindArgs[i]);
                }
            }
            int eftCount = pst.executeUpdate();
            conn.commit();
            return eftCount;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            close(conn, pst);
        }
    }

    private List<Map<String, Object>> execSel(String sql, Object[] bindArgs) throws SQLException {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(sql);
            if (bindArgs != null) {
                for (int i = 0; i < bindArgs.length; i++) {
                    pst.setObject(i + 1, bindArgs[i]);
                }
            }
            rs = pst.executeQuery();
            return buildResult(rs);
        } finally {
            close(conn, pst, rs);
        }
    }

    private String generateSql(boolean distinct,
                               String tables,
                               String[] columns,
                               String where,
                               String groupBy,
                               String having,
                               String orderBy,
                               String limit) {
        if (isEmpty(groupBy) && !isEmpty(having)) {
            throw new IllegalArgumentException("HAVING clauses are only permitted when using a groupBy clause");
        }
        if (!isEmpty(limit) && !sLimitPattern.matcher(limit).matches()) {
            throw new IllegalArgumentException("invalid LIMIT clauses: " + limit);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        if (distinct) {
            sql.append("DISTINCT ");
        }
        if (columns != null && columns.length != 0) {
            for(String column : columns) {
                if(!column.isEmpty()) {
                    sql.append(column).append(", ");
                }
            }
            sql.deleteCharAt(sql.length()-1);
        } else {
            sql.append(" * ");
        }
        sql.append("FROM ").append(tables);
        addClause(sql, " WHERE ", where);
        addClause(sql, " GROUP BY ", groupBy);
        addClause(sql, " HAVING ", having);
        addClause(sql, " ORDER BY ", orderBy);
        addClause(sql, " LIMIT ", limit);
        return sql.toString();
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private void addClause(StringBuilder s, String name, String clause) {
        if (!isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    private List<Map<String, Object>> buildResult(ResultSet resultSet) throws SQLException{
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        while (resultSet.next()) {
            Map<String, Object> rsMap = new HashMap<>(rsmd.getColumnCount());
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                rsMap.put(rsmd.getColumnName(i), resultSet.getObject(i));
            }
            resultList.add(rsMap);
        }
        return resultList;
    }

    private void close(Connection conn, Statement pst) {
        close(conn, pst, null);
    }

    private void close(Connection conn, Statement pst, ResultSet rs){
        if(pst!=null){
            try {
                pst.close();
            } catch (SQLException e) {
                log.error("Close statement error: {}", e.getMessage(), e);
            }
        }
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Close resultSet error: {}", e.getMessage(), e);
            }
        }
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Close connection error: {}", e.getMessage(), e);
            }
        }
    }

}
