package org.example.aspect.entity;

import java.util.Objects;

public class PrivacyElemEntity {

    private String methodOrClassPath;

    private String fieldInfo;

    public PrivacyElemEntity() {
    }

    public PrivacyElemEntity(String methodOrClassPath, String fieldInfo) {
        this.methodOrClassPath = methodOrClassPath;
        this.fieldInfo = fieldInfo;
    }

    public String getMethodOrClassPath() {
        return methodOrClassPath;
    }

    public void setMethodOrClassPath(String methodOrClassPath) {
        this.methodOrClassPath = methodOrClassPath;
    }

    public String getFieldInfo() {
        if(fieldInfo.contains(".")) {
            fieldInfo = fieldInfo.replace(".", "_");
        }
        return fieldInfo;
    }

    public void setFieldInfo(String fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    @Override
    public String toString() {
        return "PrivacyElemEntity [methodOrClassPath=" + methodOrClassPath +
                ", fieldInfo=" + fieldInfo +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivacyElemEntity that = (PrivacyElemEntity) o;
        return methodOrClassPath.equals(that.methodOrClassPath) && fieldInfo.equals(that.fieldInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodOrClassPath, fieldInfo);
    }
}
