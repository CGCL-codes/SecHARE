package org.example.protect;

import org.example.protect.entity.PrivacyMappingEntity;

import java.util.List;

public interface PrivacyHandle {

    // -------------------------select-----------------------
    /**
     * Get ShadowInfo By DeviceId And UserId
     *
     * @param fieldName     privacy info field name
     * @param deviceId      char(36) id
     * @param userId        char(36) id
     * @return              sham info
     */
    String getShadowInfoByDeviceIdAndUserId(String fieldName, String deviceId, String userId);

    /**
     * Get ShadowInfo By RealInfo And UserId
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @param userId        char(36) id
     * @return              sham info
     */
    String getShadowInfoByRealInfoAndUserId(String fieldName, String realInfo, String userId);

    /**
     * Get ShadowInfo By RealInfo (No Auth verify)
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @return              sham info
     */
    String getShadowInfoByRealInfoWithNoAuth(String fieldName, String realInfo);

    /**
     * Get ShadowInfo By RealInfo (have Auth verify)
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @return              sham info
     */
    String getShadowInfoByRealInfoWithAuth(String fieldName, String realInfo);

    /**
     * Get RealInfo By ShadowInfo (No Auth verify)
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     * @return              privacy info
     */
    String getRealInfoByShadowInfoWithNoAuth(String fieldName, String shadowInfo);

    /**
     * Get RealInfo By ShadowInfo (have Auth verify)
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     * @return              privacy info
     */
    String getRealInfoByShadowInfoWithAuth(String fieldName, String shadowInfo);

    /**
     * Get All By DeviceId
     *
     * @param fieldName     privacy info field name
     * @param deviceId      deviceId
     * @return              privacy entity
     */
    List<PrivacyMappingEntity> getAllByDeviceId(String fieldName, String deviceId);

    /**
     * Get All By DeviceId
     *
     * @param fieldName     privacy info field name
     * @param userId        userId
     * @return              privacy entity
     */
    List<PrivacyMappingEntity> getAllByUserId(String fieldName, String userId);

    /**
     * Get All By DeviceId And UserId
     *
     * @param fieldName     privacy info field name
     * @param deviceId      deviceId
     * @param userId        userId
     * @return              privacy entity
     */
    PrivacyMappingEntity getAllByDeviceIdAndUserId(String fieldName, String deviceId, String userId);

    /**
     * Get All By RealInfo
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @return              privacy entity
     */
    List<PrivacyMappingEntity> getAllByRealInfo(String fieldName, String realInfo);

    /**
     * Get All By RealInfo And UserId
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @param userId        userId
     * @return              privacy entity
     */
    PrivacyMappingEntity getAllByRealInfoAndUserId(String fieldName, String realInfo, String userId);

    /**
     * Get All By ShadowInfo (No Auth verify)
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     * @return              privacy entity
     */
    PrivacyMappingEntity getAllByShadowInfoWithNoAuth(String fieldName, String shadowInfo);

    /**
     * Get All By ShadowInfo (have Auth verify)
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     * @return              privacy entity
     */
    PrivacyMappingEntity getAllByShadowInfoWithAuth(String fieldName, String shadowInfo);

    // -------------------------update-----------------------
    /**
     * Update RealInfo By DeviceId
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @param deviceId      char(36) id
     */
    void updateRealInfoByDeviceId(String fieldName, String realInfo, String deviceId);

    /**
     * Update RealInfo By DeviceId
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     * @param deviceId      char(36) id
     * @param userId        char(36) id
     */
    void updateRealInfoAndDeviceIdByUserId(String fieldName, String realInfo, String deviceId, String userId);

    /**
     * Update UserId By DeviceId and UserId(null-- Unauthorized)
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     * @param deviceId      char(36) id
     */
    void updateUserIdByDeviceIdAndNullUserId(String fieldName, String userId, String deviceId);

    /**
     * Update UserId and ShadowInfo By DeviceId and UserId(null-- Unauthorized)
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     * @param shadowInfo    sham info
     * @param deviceId      char(36) id
     */
    void updateUserIdAndShadowInfoByDeviceIdAndNullUserId(String fieldName, String userId, String shadowInfo, String deviceId);

    /**
     * Update UserId(Set null) and ShadowInfo By DeviceId and UserId
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     * @param deviceId      char(36) id
     * @param userId        char(36) id
     */
    void updateNullUserIdAndShadowInfoByDeviceIdAndUserId(String fieldName, String shadowInfo, String deviceId, String userId);

    /**
     * Update UserId and ShadowInfo By DeviceId
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     * @param shadowInfo    sham info
     * @param deviceId      char(36) id
     */
    void updateUserIdAndShadowInfoByDeviceId(String fieldName, String userId, String shadowInfo, String deviceId);

    /**
     * Update userId By sensitive info and userId(null-- Unauthorized)
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     * @param realInfo      privacy info
     */
    void updateUserIdByRealInfoAndNullUserId(String fieldName, String userId, String realInfo);

    /**
     * Update UserId and ShadowInfo By realInfo and UserId(null-- Unauthorized)
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     * @param shadowInfo    sham info
     * @param realInfo      privacy info
     */
    void updateUserIdAndShadowInfoByRealInfoAndNullUserId(String fieldName, String userId, String shadowInfo, String realInfo);

    /**
     * Update UserId(Set null) and ShadowInfo By RealInfo and UserId
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     * @param realInfo      privacy info
     * @param userId        char(36) id
     */
    void updateNullUserIdAndShadowInfoByRealInfoAndUserId(String fieldName, String shadowInfo, String realInfo, String userId);

    /**
     * Update UserId and ShadowInfo By realInfo and UserId(null-- Unauthorized)
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     * @param shadowInfo    sham info
     * @param realInfo      privacy info
     */
    void updateUserIdAndShadowInfoByRealInfo(String fieldName, String userId, String shadowInfo, String realInfo);

    /**
     * Update Remarks By DeviceId and UserId
     *
     * @param fieldName     privacy info field name
     * @param remarks       additional info, str or json type
     * @param deviceId      char(36) id
     * @param userId        char(36) id
     */
    void updateRemarksByDeviceIdAndUserId(String fieldName, String remarks, String deviceId, String userId);

    /**
     * Update Remarks By real info and userId
     *
     * @param fieldName     privacy info field name
     * @param remarks       additional info, str or json type
     * @param realInfo      privacy info
     * @param userId        char(36) id
     */
    void updateRemarksByRealInfoAndUserId(String fieldName, String remarks, String realInfo, String userId);

    /**
     * Update ShadowInfo By DeviceId And UserId
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info, can be null
     * @param deviceId      char(36) id
     * @param userId        char(36) id
     */
    void updateShadowInfoByDeviceIdAndUserId(String fieldName, String shadowInfo, String deviceId, String userId);

    /**
     * Update ShadowInfo By privacy info and userId
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info, can be null
     * @param realInfo      privacy info
     * @param userId        char(32) id
     */
    void updateShadowInfoByRealInfoAndUserId(String fieldName, String shadowInfo, String realInfo, String userId);

    // -------------------------delete-----------------------
    /**
     * Delete By DeviceId
     *
     * @param fieldName     privacy info field name
     * @param deviceId      char(36) id
     */
    void deleteByDeviceId(String fieldName, String deviceId);

    /**
     * Delete By UserId
     *
     * @param fieldName     privacy info field name
     * @param userId        char(36) id
     */
    void deleteByUserId(String fieldName, String userId);

    /**
     * Delete By sensitive info
     *
     * @param fieldName     privacy info field name
     * @param realInfo      privacy info
     */
    void deleteByRealInfo(String fieldName, String realInfo);

    /**
     * Delete By ShadowInfo
     *
     * @param fieldName     privacy info field name
     * @param shadowInfo    sham info
     */
    void deleteByShadowInfo(String fieldName, String shadowInfo);

    // -------------------------save-----------------------
    /**
     * Generate ShadowInfo
     *
     * @param fieldName     privacy info field name
     * @param deviceId      char(36) id, default random
     * @param realInfo      privacy info
     * @return              sham info, failed is null
     */
    String generateShadowInfo(String fieldName, String deviceId, String realInfo);

    /**
     * Generate ShadowInfo
     *
     * @param fieldName     privacy info field name
     * @param deviceId      char(36) id, default random
     * @param userId        char(36) id
     * @param realInfo      privacy info
     * @return              sham info, failed is null
     */
    String generateShadowInfo(String fieldName, String deviceId, String userId, String realInfo);

    /**
     * Generate ShadowInfo
     *
     * @param fieldName     privacy info field name
     * @param deviceId      char(36) id, default random
     * @param userId        char(36) id
     * @param realInfo      privacy info
     * @param remarks       additional info, str or json type, default empty
     * @return              sham info, failed is null
     */
    String generateShadowInfo(String fieldName, String deviceId, String userId, String realInfo, String remarks);

    /**
     * Get ShadowInfo
     *
     * @param fieldName     privacy info field name
     * @param deviceId      char(36) id, default random
     * @param userId        char(36) id
     * @param realInfo      privacy info
     * @param shadowInfo    sham info, can be empty
     * @param remarks       additional info, str or json type, default empty
     * @return              sham info, failed is null
     */
    String generateShadowInfo(String fieldName, String deviceId, String userId, String realInfo, String shadowInfo, String remarks);

}
