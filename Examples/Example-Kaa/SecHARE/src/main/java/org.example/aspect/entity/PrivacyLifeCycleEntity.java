package org.example.aspect.entity;

public class PrivacyLifeCycleEntity {

    private PrivacyElemEntity addDevice;

    private PrivacyElemEntity deleteDevice;

    private PrivacyElemEntity deleteUser;

    private PrivacyElemEntity[] grantAuth;

    private PrivacyElemEntity[] revokeAuth;

    private String[] authorityName;

    private  PrivacySensitiveInfoEntity[] senInfoOperateEntity;

    public PrivacyElemEntity getAddDevice() {
        return addDevice;
    }

    public void setAddDevice(PrivacyElemEntity addDevice) {
        this.addDevice = addDevice;
    }

    public PrivacyElemEntity getDeleteDevice() {
        return deleteDevice;
    }

    public void setDeleteDevice(PrivacyElemEntity deleteDevice) {
        this.deleteDevice = deleteDevice;
    }

    public PrivacyElemEntity getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(PrivacyElemEntity deleteUser) {
        this.deleteUser = deleteUser;
    }

    public PrivacyElemEntity[] getGrantAuth() {
        return grantAuth;
    }

    public void setGrantAuth(PrivacyElemEntity[] grantAuth) {
        this.grantAuth = grantAuth;
    }

    public PrivacyElemEntity[] getRevokeAuth() {
        return revokeAuth;
    }

    public void setRevokeAuth(PrivacyElemEntity[] revokeAuth) {
        this.revokeAuth = revokeAuth;
    }

    public String[] getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String[] authorityName) {
        this.authorityName = authorityName;
    }

    public PrivacySensitiveInfoEntity[] getSenInfoOperateEntity() {
        return senInfoOperateEntity;
    }

    public void setSenInfoOperateEntity(PrivacySensitiveInfoEntity[] senInfoOperateEntity) {
        this.senInfoOperateEntity = senInfoOperateEntity;
    }
}
