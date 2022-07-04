package org.example.aspect.entity;

import java.util.Arrays;
import java.util.Objects;

public class PrivacySensitiveInfoEntity {

    private PrivacyElemEntity sensitiveInfo;

    private PrivacyElemEntity[] updateSenInfo;

    private PrivacyElemEntity[] getProtectedInfo;

    private PrivacyElemEntity[] verifySenInfo;

    public PrivacyElemEntity getSensitiveInfo() {
        return sensitiveInfo;
    }

    public void setSensitiveInfo(PrivacyElemEntity sensitiveInfo) {
        this.sensitiveInfo = sensitiveInfo;
    }

    public PrivacyElemEntity[] getUpdateSenInfo() {
        return updateSenInfo;
    }

    public void setUpdateSenInfo(PrivacyElemEntity[] updateSenInfo) {
        this.updateSenInfo = updateSenInfo;
    }

    public PrivacyElemEntity[] getGetProtectedInfo() {
        return getProtectedInfo;
    }

    public void setGetProtectedInfo(PrivacyElemEntity[] getProtectedInfo) {
        this.getProtectedInfo = getProtectedInfo;
    }

    public PrivacyElemEntity[] getVerifySenInfo() {
        return verifySenInfo;
    }

    public void setVerifySenInfo(PrivacyElemEntity[] verifySenInfo) {
        this.verifySenInfo = verifySenInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivacySensitiveInfoEntity that = (PrivacySensitiveInfoEntity) o;
        return sensitiveInfo.equals(that.sensitiveInfo) && Arrays.equals(updateSenInfo, that.updateSenInfo) && Arrays.equals(getProtectedInfo, that.getProtectedInfo) && Arrays.equals(verifySenInfo, that.verifySenInfo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sensitiveInfo);
        result = 31 * result + Arrays.hashCode(updateSenInfo);
        result = 31 * result + Arrays.hashCode(getProtectedInfo);
        result = 31 * result + Arrays.hashCode(verifySenInfo);
        return result;
    }
}