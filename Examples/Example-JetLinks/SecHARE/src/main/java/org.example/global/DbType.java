package org.example.global;

public enum DbType {
    /**
     * SQL(mysql, postgreSQL...)
     */
    SQL(0),
    MONGODB(1),
    CASSANDRA(2);

    private final int code;

    DbType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DbType parse(String value) {
        DbType dbType = null;
        if (value != null && value.length() != 0) {
            for (DbType current : DbType.values()) {
                if (current.name().equalsIgnoreCase(value)) {
                    dbType = current;
                    break;
                }
            }
        }
        return dbType;
    }

}
