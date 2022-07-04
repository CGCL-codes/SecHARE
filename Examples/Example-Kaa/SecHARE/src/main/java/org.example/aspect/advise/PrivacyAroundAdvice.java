package org.example.aspect.advise;

import org.example.aspect.entity.PrivacyElemEntity;
import org.example.aspect.entity.PrivacySensitiveInfoEntity;
import org.example.global.PrivacyGlobalValue;
import org.example.protect.PrivacyHandle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import static org.example.aspect.advise.PrivacyBaseAdvice.*;
import static org.example.aspect.advise.PrivacyCycleEnum.*;
import static org.example.aspect.advise.PrivacyCycleOperate.*;

@Slf4j
public class PrivacyAroundAdvice {

    private static PrivacyHandle privacyHandle = null;

    private static PrivacySensitiveInfoEntity[] pseArray = null;

    public static Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        // 0-isExecuted, 1-addDevice, 2-delDevice, 3-delUser, 4-update, 5-grant, 6-get, 7-verify
        boolean[] cycleStepExecuted = new boolean[]{false, true, true, true, true, true, true, true};
        try {
            if (pseArray == null) {
                pseArray = PrivacyGlobalValue.get().getPlcEntity().getSenInfoOperateEntity();
            }
            if (!ArrayUtils.isEmpty(pseArray)) {
                if (privacyHandle == null) {
                    privacyHandle = getPrivacyHandle();
                    init(privacyHandle, pseArray);
                }

                // before
                Object[] arguments = joinPoint.getArgs();
                // start
                handleAddDeviceBefore(methodName, arguments, cycleStepExecuted);
                handleDelDeviceBefore(methodName, arguments, cycleStepExecuted);
                handleDelUserBefore(methodName, arguments, cycleStepExecuted);
                handleUpdateInfoBefore(methodName, arguments, cycleStepExecuted);
                handleAuthBefore(methodName, arguments, cycleStepExecuted);
                handleVerifySenInfoBefore(methodName, arguments, cycleStepExecuted);

                Object o = joinPoint.proceed(arguments);

                // after
                return executeAopAfterFull(methodName, o, cycleStepExecuted);
            }
        } catch (Exception e) {
            log.error("[PrivacyTool] handle {} error! {}", methodName, e.getLocalizedMessage(), e);
        }
        return joinPoint.proceed();
    }

    private static Object executeAopAfterFull(String methodName, Object o, boolean[] cycleStepExecuted) {
        handleAddDeviceAfter(o, cycleStepExecuted);
        handleDelDeviceAfter(o, cycleStepExecuted);
        handleDelUserAfter(o, cycleStepExecuted);
        handleUpdateInfoAfter(methodName, o, cycleStepExecuted);
        handleAuthAfter(methodName, o, cycleStepExecuted);
        handleGetInfoAfter(methodName, o, cycleStepExecuted);
        return handleVerifySenInfoAfter(methodName, o, cycleStepExecuted);
    }

    /**
     * addDevice -> generate shadowInfo / save deviceId / save userId / save realInfo (possible)
     */
    private static void handleAddDeviceBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[STEP_DEFAULT.value()]) {
            return;
        }
        PrivacyElemEntity addDevicePee = PrivacyGlobalValue.get().getPlcEntity().getAddDevice();
        // filter
        if (addDevicePee != null && checkMethodName(methodName, addDevicePee.getMethodOrClassPath())) {
            cycleStepExecuted[STEP_DEFAULT.value()] = true;
            parametersHandle(addDevicePee.getFieldInfo(), objects, cycleStepExecuted,
                    null, false, false, ADD_DEVICE);
        }
    }
    private static void handleAddDeviceAfter(Object o, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[ADD_DEVICE.value()]) {
            return;
        }
        PrivacyElemEntity addDevicePee = PrivacyGlobalValue.get().getPlcEntity().getAddDevice();
        // returnedObj
        String idMethodName = addDevicePee.getFieldInfo();
        handleAddDevice(o, idMethodName);
    }

    /**
     * delete device -> gain id value from parameters
     */
    private static void handleDelDeviceBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[STEP_DEFAULT.value()]) {
            return;
        }
        // filter
        PrivacyElemEntity delDevicePee = PrivacyGlobalValue.get().getPlcEntity().getDeleteDevice();
        if (delDevicePee != null && checkMethodName(methodName, delDevicePee.getMethodOrClassPath())) {
            cycleStepExecuted[STEP_DEFAULT.value()] = true;
            parametersHandle(delDevicePee.getFieldInfo(), objects, cycleStepExecuted,
                    null, false, false, DEL_DEVICE);
        }
    }
    private static void handleDelDeviceAfter(Object o, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[DEL_DEVICE.value()]) {
            return;
        }
        PrivacyElemEntity delDevicePee = PrivacyGlobalValue.get().getPlcEntity().getDeleteDevice();
        // returnedObj
        String idMethodName = delDevicePee.getFieldInfo();
        handleDelDevice(o, idMethodName);
    }

    /**
     * delete user -> gain userId value from parameters
     */
    private static void handleDelUserBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[STEP_DEFAULT.value()]) {
            return;
        }
        // filter
        PrivacyElemEntity delUserPee = PrivacyGlobalValue.get().getPlcEntity().getDeleteUser();
        if (delUserPee != null && checkMethodName(methodName, delUserPee.getMethodOrClassPath())) {
            cycleStepExecuted[STEP_DEFAULT.value()] = true;
            parametersHandle(delUserPee.getFieldInfo(), objects, cycleStepExecuted,
                    null, false, false, DEL_USER);
        }
    }
    private static void handleDelUserAfter(Object o, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[DEL_USER.value()]) {
            return;
        }
        PrivacyElemEntity delUserPee = PrivacyGlobalValue.get().getPlcEntity().getDeleteUser();
        // returnedObj
        String idMethodName = delUserPee.getFieldInfo();
        handleDelDevice(o, idMethodName);
    }

    /**
     * update info -> gain id value and sensitiveField value from returned object
     */
    private static void handleUpdateInfoBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[STEP_DEFAULT.value()]) {
            return;
        }
        // multi sensitiveInfo
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            PrivacyElemEntity[] updateInfoPeeArray = pse.getUpdateSenInfo();
            if (ArrayUtils.isEmpty(updateInfoPeeArray)) {
                return;
            }
            // updateInfo
            for (PrivacyElemEntity pee : updateInfoPeeArray) {
                if (checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    cycleStepExecuted[STEP_DEFAULT.value()] = true;
                    // id/realInfo getMethodName
                    parametersHandle(pee.getFieldInfo(), objects, cycleStepExecuted,
                            null, false, false, UPDATE);
                    break;
                }
            } // for
        } // for
    }
    private static void handleUpdateInfoAfter(String methodName, Object o, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[UPDATE.value()]) {
            return;
        }
        // multi sensitiveInfo
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            String sensitiveFieldName = pse.getSensitiveInfo().getFieldInfo();
            PrivacyElemEntity[] updateInfoPeeArray = pse.getUpdateSenInfo();
            // updateInfo
            for (PrivacyElemEntity pee : updateInfoPeeArray) {
                if (checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    // id/realInfo getMethodName
                    String line = "-";
                    String infoMethodName = pee.getFieldInfo();
                    if (StringUtils.isEmpty(infoMethodName)) {
                        continue;
                    }
                    String[] idAndRealInfo = infoMethodName.split(line);
                    String realInfoMethodName = idAndRealInfo[0];
                    String idMethodName = idAndRealInfo[1];
                    handleUpdateInfo(o, idMethodName, o, realInfoMethodName, sensitiveFieldName);
                    break;
                }
            } // for
        } // for
    }

    private static void handleAuthBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[STEP_DEFAULT.value()]) {
            return;
        }
        // base
        PrivacyElemEntity[] grantAuthPlcArray = PrivacyGlobalValue.get().getPlcEntity().getGrantAuth();
        PrivacyElemEntity[] revokeAuthPlcArray = PrivacyGlobalValue.get().getPlcEntity().getRevokeAuth();
        // grant
        grantOrRevokeAuthBefore(methodName, objects, cycleStepExecuted, grantAuthPlcArray, true);
        // revoke
        grantOrRevokeAuthBefore(methodName, objects, cycleStepExecuted, revokeAuthPlcArray, false);
    }
    private static void grantOrRevokeAuthBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted,
                                                PrivacyElemEntity[] authPeeArray, boolean isGrant) {
        if (ArrayUtils.isEmpty(authPeeArray)) {
            return;
        }
        // multi sensitiveInfo
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            String senInfoFieldName = pse.getSensitiveInfo().getFieldInfo();
            if (StringUtils.isEmpty(senInfoFieldName)) {
                continue;
            }
            // multi auth info
            for (PrivacyElemEntity pee : authPeeArray) {
                // filter
                if (!checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    continue;
                }
                cycleStepExecuted[STEP_DEFAULT.value()] = true;
                parametersHandle(pee.getFieldInfo(), objects, cycleStepExecuted, pse.getSensitiveInfo(),
                        isGrant, false, GRANT_REVOKE_AUTH);
                // The following methodName cannot match
                break;
            }
        } // for
    }
    private static void handleAuthAfter(String methodName, Object obj, boolean[] cycleStepExecuted) {
        // After
        if(cycleStepExecuted[GRANT_REVOKE_AUTH.value()]) {
            return;
        }
        // base
        PrivacyElemEntity[] grantAuthPlcArray = PrivacyGlobalValue.get().getPlcEntity().getGrantAuth();
        PrivacyElemEntity[] revokeAuthPlcArray = PrivacyGlobalValue.get().getPlcEntity().getRevokeAuth();
        // grant
        grantOrRevokeAuthAfter(methodName, obj, grantAuthPlcArray, true);
        // revoke
        grantOrRevokeAuthAfter(methodName, obj, revokeAuthPlcArray, false);
    }
    private static void grantOrRevokeAuthAfter(String methodName, Object o,
                                               PrivacyElemEntity[] authPeeArray, boolean isGrant) {
        // multi sensitiveInfo
        for (PrivacySensitiveInfoEntity pse : pseArray) {
            // multi auth info
            for (PrivacyElemEntity pee : authPeeArray) {
                // filter
                if (!checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    continue;
                }
                String getMethodName = pee.getFieldInfo().trim();
                String[] userIdAndDvPmId = getMethodName.split("-");
                // userIdAndDvPmId[1]
                grantOrRevokeAuth(o, userIdAndDvPmId[0], o,
                        getMethodName.replace(userIdAndDvPmId[0]+"-", ""), pse.getSensitiveInfo(), isGrant);
                // The following methodName cannot match
                break;
            }
        } // for
    }

    /**
     * get info -> id real info to shadow info
     */
    private static Object handleGetInfoAfter(String methodName, Object o, boolean[] cycleStepExecuted) {
        return handleGetInfo(methodName, o, cycleStepExecuted[STEP_DEFAULT.value()]);
    }

    /**
     *  shadow info[Object[] parameters] -> real info
     */
    private static void handleVerifySenInfoBefore(String methodName, Object[] objects, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[STEP_DEFAULT.value()]) {
            return;
        }

        for (PrivacySensitiveInfoEntity pse : pseArray) {
            PrivacyElemEntity[] verifySenPeeArray = pse.getVerifySenInfo();
            String senInfoFieldName = pse.getSensitiveInfo().getFieldInfo();
            if (ArrayUtils.isEmpty(verifySenPeeArray) || StringUtils.isEmpty(senInfoFieldName)) {
                return;
            }

            for (PrivacyElemEntity pee : verifySenPeeArray) {
                if (!checkMethodName(methodName, pee.getMethodOrClassPath())) {
                    continue;
                }
                cycleStepExecuted[STEP_DEFAULT.value()] = true;

                parametersHandle(pee.getFieldInfo(), objects, cycleStepExecuted, pse.getSensitiveInfo(),
                        false, true, VERIFY);
                break;
            } // for
        }
    }
    /**
     *  shadow info[returned obj] -> real info
     */
    private static Object handleVerifySenInfoAfter(String methodName, Object o, boolean[] cycleStepExecuted) {
        if(cycleStepExecuted[VERIFY.value()]) {
            return o;
        }
        return handleVerifySenInfo(methodName, o);
    }

    private static void parametersHandle(String infoMethodName, Object[] objects,
                                         boolean[] cycleStepExecuted, PrivacyElemEntity sensitivePee,
                                         Boolean isGrant, Boolean isVerify, PrivacyCycleEnum cycleEnum) {
        // returnedObj? parameters?
        if(StringUtils.isEmpty(infoMethodName)) {
            return;
        }
        String firstStr = infoMethodName.trim().charAt(0) + "";
        // parameters
        if(isNumeric(firstStr)) {
            if(ArrayUtils.isEmpty(objects)) {
                return;
            }
            // realInfo and deviceId etc (all parameters or all returnedObj)
            String line = "-";
            String realInfoMethodName;
            String idMethodName;
            if(infoMethodName.contains(line)) {
                String[] idAndRealInfo = infoMethodName.split(line);
                realInfoMethodName = idAndRealInfo[0];
                // idMethodName = idAndRealInfo[1];
                idMethodName = infoMethodName.replace(idAndRealInfo[0] + "-", "");
                int realInfoPosition = Integer.parseInt(realInfoMethodName.charAt(0) + "");
                int idPosition = Integer.parseInt(idMethodName.charAt(0) + "");
                if(realInfoPosition >= objects.length || idPosition >= objects.length) {
                    return;
                }
                if (realInfoMethodName.length() == 1) {
                    realInfoMethodName = "object";
                } else {
                    realInfoMethodName = realInfoMethodName.substring(2);
                }
                if (idMethodName.length() == 1) {
                    idMethodName = "object";
                } else {
                    idMethodName = idMethodName.substring(2);
                }

                if(isVerify) {
                    setVerifyOfObject(objects[realInfoPosition], realInfoMethodName, sensitivePee.getFieldInfo());
                }
                callOperateByEnum(objects[realInfoPosition], realInfoMethodName, objects[idPosition], idMethodName, sensitivePee, isGrant, cycleEnum);

                return;
            }
            int position = Integer.parseInt(firstStr);
            if(position >= objects.length) {
                return;
            }
            if (infoMethodName.length() > 1) {
                infoMethodName = infoMethodName.substring(2);
            } else {
                infoMethodName = "object";
            }
            if(isVerify) {
                setVerifyOfObject(objects[position], infoMethodName, sensitivePee.getFieldInfo());
            }
            callOperateByEnum(objects[position], infoMethodName, null, null, sensitivePee, isGrant, cycleEnum);
        } else {
            // after need execute
            cycleStepExecuted[cycleEnum.value()] = false;
        }
    }

    private static void callOperateByEnum(Object o, String methodStr, Object objSpare, String spareMethodStr,
                                          PrivacyElemEntity sensitivePee, Boolean isGrant, PrivacyCycleEnum cycleEnum) {
        switch (cycleEnum) {
            case ADD_DEVICE:
                handleAddDevice(o, methodStr);
                break;
            case DEL_DEVICE:
                handleDelDevice(o, methodStr);
                break;
            case DEL_USER:
                handleDelUser(o, methodStr);
                break;
            case UPDATE:
                handleUpdateInfo(o, methodStr, objSpare, spareMethodStr, sensitivePee.getFieldInfo());
                break;
            case GRANT_REVOKE_AUTH:
                grantOrRevokeAuth(o, methodStr, objSpare, spareMethodStr, sensitivePee, isGrant);
                break;
            case GET:
            case VERIFY:
            default: break;
        }
    }

}
