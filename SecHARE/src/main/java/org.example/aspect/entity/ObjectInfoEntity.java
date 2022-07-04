package org.example.aspect.entity;

public class ObjectInfoEntity {

    private String info;

    private String actualFieldName;

    public ObjectInfoEntity() {}

    public ObjectInfoEntity(String info, String actualFieldName) {
        this.info = info;
        this.actualFieldName = actualFieldName;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getActualFieldName() {
        return actualFieldName;
    }

    public void setActualFieldName(String actualFieldName) {
        this.actualFieldName = actualFieldName;
    }
}
