package org.example.gen;

import org.example.aspect.entity.PrivacyElemEntity;
import org.example.gen.properties.CustomProperties;
import org.example.util.ClassMethodUtil;
import org.example.util.PathUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenConfigurationFile {

    Map<JavaClass, String> clzIdResultMap = new HashMap<>();

    Map<String, Boolean> clzElemResultMap = new HashMap<>();

    Map<String, List<String>> methodCallMap = new HashMap<>();

    Pattern pattern = Pattern.compile("^[0-9]+$");

    public void genConfigFile() {
        System.out.println("\n**************************************************");
        System.out.println("Welcome to use. Please follow the prompts");
        System.out.println("Please input the address of the java file containing the device operation(Multiple are separated by commas): ");
        Scanner sc = new Scanner(System.in);
        String pathAddr = sc.nextLine();
        System.out.println("**************************************************\n");

        JavaProjectBuilder builder =  new  JavaProjectBuilder();

        Launcher spoonLauncher = new Launcher();

        List<String> pathAddrList = new ArrayList<>();

        if (pathAddr.contains(",") || pathAddr.contains("，")) {
            String[] pathNameList = pathAddr.split("[,，]");
            pathAddrList.addAll(Arrays.asList(pathNameList));
            for (String path : pathNameList) {
                builder.addSourceTree(new File(path.trim()));
            }
        }else {
            builder.addSourceTree(new File(pathAddr.trim()));
            pathAddrList.add(pathAddr);
        }
        if (builder.getClasses().size() < 1) {
            System.out.println("Failed to get classes! Please check the address entered!");
            return;
        }

        setupSpoon(builder, spoonLauncher, pathAddrList);

        List<JavaMethod> addDeviceList = new ArrayList<>();
        List<JavaMethod> deleteDeviceList = new ArrayList<>();
        List<JavaMethod> grantList = new ArrayList<>();
        List<JavaMethod> revokeList = new ArrayList<>();
        findInfoMethod(builder, addDeviceList, deleteDeviceList, grantList, revokeList);
        PrivacyElemEntity addStrPee = chooseMethod(addDeviceList, "addDevice", false, false, false);
        PrivacyElemEntity deleteStrPee = chooseMethod(deleteDeviceList, "deleteDevice", false, true, false);
        PrivacyElemEntity grantStrPee = chooseMethod(grantList, "grantAuth", true, false, true);
        PrivacyElemEntity revokeStrPee = chooseMethod(revokeList, "revokeAuth", true, false, true);

        String authorityName = fillingAuthorityName();

        List<PrivacyElemEntity> senPeeList = new ArrayList<>();
        List<PrivacyElemEntity> updatePeeList = new ArrayList<>();
        List<PrivacyElemEntity> gainPeeList = new ArrayList<>();
        List<PrivacyElemEntity> usePeeList = new ArrayList<>();
        fillingUpdateGainUse(sc, builder, senPeeList, updatePeeList, gainPeeList, usePeeList);

        PrivacyElemEntity senStrPee = getFillInInfoPee(senPeeList);
        PrivacyElemEntity updateStrPee = getFillInInfoPee(updatePeeList);
        PrivacyElemEntity gainStrPee = getFillInInfoPee(gainPeeList);
        PrivacyElemEntity useStrPee = getFillInInfoPee(usePeeList);

        generateProperty(addStrPee, deleteStrPee, grantStrPee, revokeStrPee, gainStrPee, updateStrPee, useStrPee,
                senStrPee, authorityName);
    }

    private void setupSpoon(JavaProjectBuilder builder, Launcher spoonLauncher, List<String> pathAddrList) {
        // add interface
        List<JavaClass> interfaceJcList = new ArrayList<>();
        for(JavaClass javaClass : builder.getClasses()) {
            List<JavaClass> interfaceClassList = javaClass.getInterfaces();
            if (interfaceClassList != null && interfaceClassList.size() > 0) {
                interfaceJcList.addAll(interfaceClassList);
            } else {
                for (String pathAddr : pathAddrList) {
                    if (pathAddr.contains(javaClass.getName())) {
                        spoonLauncher.addInputResource(pathAddr.trim());
                        break;
                    }
                }
            }

        }
        if (interfaceJcList.size() > 0) {
            for (JavaClass jc : interfaceJcList) {
                URL url = jc.getSource().getURL();
                if (url != null) {
                    spoonLauncher.addInputResource(url.toString());
                    continue;
                }

                Class<?> jClass = ClassMethodUtil.getClassByName(jc.getFullyQualifiedName());
                String path = PathUtil.getPathFromClass(jClass);
                if (path != null) {
                    spoonLauncher.addInputResource(path.replace("target/classes", "src/main/java").
                            replace("class", "java"));
                }
            }
        }
        // setup
        spoonLauncher.getEnvironment().setNoClasspath(true);
        spoonLauncher.buildModel();
        CtModel spoonCtModel = spoonLauncher.getModel();

        List <CtMethod<?>> ctMethodList = new ArrayList <>();
        for (CtMethod<?> method : spoonCtModel.getElements(new TypeFilter<>(CtMethod.class))) {
            if (method.hasModifier(ModifierKind.PUBLIC)) {
                ctMethodList.add(method);
            }
        }
        List<CtExecutableReference<?>> allMethods = ctMethodList.stream()
                .map(CtExecutable::getReference).collect(Collectors.toList());

        spoonCtModel.getRootPackage().accept(new CtScanner() {
            final List<String> calls = new ArrayList<>();
            @Override
            public <T> void visitCtMethod(CtMethod<T> m) {
                super.visitCtMethod(m);
                if (calls.size() > 0) {
                    List<String> callTempList = new ArrayList<>(calls);
                    methodCallMap.put(m.getSimpleName(), callTempList);
                    calls.clear();
                }
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                if (invocation.getExecutable().isConstructor()) {
                    return;
                }
                List<CtExpression<?>> argumentList = invocation.getArguments();

                for (CtExpression<?> ctArgument : argumentList) {
                    if (ctArgument.toString().contains("(") && ctArgument.toString().contains(")")) {
                        try {
                            CtInvocationImpl<?> ctInvocation = (CtInvocationImpl<?>) ctArgument;
                            executableFilter(ctInvocation, calls);
                        }catch (Exception ignored) {}
                    }
                }
                executableFilter(invocation, calls);
            }

            private void executableFilter(CtInvocation<?> invocation, List<String> calls) {
                for (CtExecutableReference<?> executable : allMethods) {
                    if (invocation.getExecutable().getSimpleName().equals(executable.getSimpleName())
                            && invocation.getExecutable().isOverriding(executable)) {
                        calls.add(executable.getSimpleName());
                    }
                }
            }
        });
    }

    private String fillingAuthorityName() {
        System.out.println("\n**************************************************");
        System.out.print("Please input Non Admin role type(multiple roles separated by ,): ");
        Scanner sc = new Scanner(System.in);
        String authorityName = sc.next();
        System.out.println("**************************************************\n");
        return authorityName;
    }

    private void fillingUpdateGainUse(Scanner sc, JavaProjectBuilder builder, List<PrivacyElemEntity> senPeeList,
                                      List<PrivacyElemEntity> updatePeeList, List<PrivacyElemEntity> gainPeeList,
                                      List<PrivacyElemEntity> usePeeList) {
        while (true) {
            PrivacyElemEntity pee = new PrivacyElemEntity();
            // "credentialsId"
            System.out.println("\n**************************************************");
            System.out.print("\nPlease input the fieldName of sensitive info(Enter 0 to exit): ");
            String fieldName = sc.next();
            if("0".equals(fieldName)) {
                break;
            }
            pee.setMethodOrClassPath(fieldName);
            System.out.print("\nPlease input the package class full path of sensitive info: ");
            String classPath = sc.next();
            pee.setFieldInfo(classPath);
            System.out.println("**************************************************\n");
            senPeeList.add(pee);

            List<JavaMethod> updateMethodList = new ArrayList<>();
            List<JavaMethod> gainMethodList = new ArrayList<>();
            List<JavaMethod> useMethodList = new ArrayList<>();

            clzElemResultMap.clear();

            for(JavaClass javaClass : builder.getClasses()) {
                List<JavaMethod> methods = javaClass.getMethods();
                for(JavaMethod method : methods) {
                    if (!method.isPublic()) {
                        continue;
                    }
                    findInfoMethod(method, fieldName, classPath, updateMethodList, gainMethodList, useMethodList);
                }
            }
            PrivacyElemEntity updMethodPlc = chooseMethod(updateMethodList, "updateSenInfoMethod",
                    true, false, false);
            updatePeeList.add(updMethodPlc);
            PrivacyElemEntity gainMethodPlc = chooseMethod(gainMethodList, "getSenInfoMethod",
                    true, false, false);
            gainPeeList.add(gainMethodPlc);
            PrivacyElemEntity useMethodPlc = chooseMethod(useMethodList, "useSenInfoMethod",
                    true, true, false);
            usePeeList.add(useMethodPlc);
        }
    }

    private PrivacyElemEntity chooseMethod(List<JavaMethod> methodList, String lifeCycleName,
                                           boolean isMulti, boolean isDelOrUse, boolean isAuth) {
        // invocation filter
        List<String> methodStrList = new ArrayList<>();
        for (JavaMethod jm : methodList) {
            if (!methodCallMap.containsKey(jm.getName())) {
                boolean isValueContain = false;
                for (List<String> valueList : methodCallMap.values()) {
                    for (String value : valueList) {
                        if (value.contains(jm.getName())) {
                            isValueContain = true;
                            break;
                        }
                    }
                    if (isValueContain) {
                        break;
                    }
                }
                if (isValueContain) {
                    continue;
                }
                methodStrList.add(jm.getName());
                continue;
            }
            methodStrList.add(jm.getName()  + "(" +
                    String.join(",", methodCallMap.get(jm.getName())) + ")");
        }
        // print
        System.out.println("\n**************************************************");
        System.out.println("----------------------------------------------");
        int i = 0;

        if (methodList.size() == 0) {
            System.out.println("No query! Please complete manually after");
        } else {
            if (isMulti) {
                System.out.println("Y All of the following options");
            }
            System.out.println("X None of the above options. Complete manually after");
        }

        for (; i < methodStrList.size(); i++) {
            System.out.println(i + " " + methodStrList.get(i));
        }
        System.out.println("----------------------------------------------");

        PrivacyElemEntity blankPee = new PrivacyElemEntity("$", "$");

        if (methodList.size() == 0) {
            System.out.println("the method of " + lifeCycleName + " does not query, please add it in the configuration file manually！");
            return blankPee;
        }
        System.out.printf("Please select the method of %s %s: ", lifeCycleName,
                isMulti ? "(multi choices separated by ,)" : "");
        Scanner sc = new Scanner(System.in);
        String choice = sc.next();
        System.out.println("**************************************************\n");

        if ("X".equalsIgnoreCase(choice.trim())) {
            return blankPee;
        }

        if (isMulti) {
            return handleMultiChoice(methodList, lifeCycleName, choice, isDelOrUse, isAuth);
        } else {
            return handleSingleChoice(methodList, lifeCycleName, choice, isDelOrUse, isAuth);
        }
    }

    private PrivacyElemEntity handleSingleChoice(List<JavaMethod> methodList, String lifeCycleName,
                                                 String choice, boolean isDelOrUse, boolean isAuth) {
        if (pattern.matcher(choice).find()) {
            int choiceId = Integer.parseInt(choice);
            if (choiceId >= methodList.size()) {
                return chooseMethod(methodList, lifeCycleName, false, isDelOrUse, isAuth);
            }
            JavaMethod method = methodList.get(choiceId);
            String idPosition = findIdInfo(method, isDelOrUse);
            if (idPosition == null) {
                idPosition = "$";
            }
            PrivacyElemEntity pee = new PrivacyElemEntity();
            pee.setMethodOrClassPath(connectStr(method));
            pee.setFieldInfo(idPosition);
            return pee;
        }
        return chooseMethod(methodList, lifeCycleName, false, isDelOrUse, isAuth);
    }

    private PrivacyElemEntity handleMultiChoice(List<JavaMethod> methodList, String lifeCycleName,
                                                String choice, boolean isDelOrUse, boolean isAuth) {
        String delimiter = "-";
        if (isAuth) {
            delimiter = ",";
        }
        StringJoiner sjPack = new StringJoiner(delimiter);
        StringJoiner sjId = new StringJoiner(delimiter);
        // selectAll
        if ("Y".equalsIgnoreCase(choice.trim())) {
            for (JavaMethod method : methodList) {
                String declareStr = connectStr(method);
                sjPack.add(declareStr);
                String idPosition = findIdInfo(method, isDelOrUse);
                if (idPosition == null) {
                    idPosition = "$";
                }
                sjId.add(idPosition);
            }
        } else {
            String[] methods = choice.split("[,，]");
            boolean isError = false;

            for (String methodStr : methods) {
                if (pattern.matcher(methodStr).find()) {
                    int choiceId = Integer.parseInt(methodStr);
                    if (choiceId >= methodList.size()) {
                        isError = true;
                        break;
                    }
                    JavaMethod method = methodList.get(choiceId);
                    String declareStr = connectStr(method);
                    sjPack.add(declareStr);
                    String idPosition = findIdInfo(method, isDelOrUse);
                    if (idPosition == null) {
                        idPosition = "$";
                    }
                    sjId.add(idPosition);
                } else {
                    isError = true;
                    break;
                }
            }
            if (isError) {
                return chooseMethod(methodList, lifeCycleName, true, isDelOrUse, isAuth);
            }
        }
        PrivacyElemEntity pee = new PrivacyElemEntity();
        pee.setMethodOrClassPath(sjPack.toString());
        pee.setFieldInfo(sjId.toString());
        return pee;
    }

    private PrivacyElemEntity getFillInInfoPee(List<PrivacyElemEntity> peeList) {
        StringJoiner sjPack = new StringJoiner(",");
        StringJoiner sjId = new StringJoiner(",");
        for (PrivacyElemEntity pee : peeList) {
            sjPack.add(pee.getMethodOrClassPath());
            sjId.add(pee.getFieldInfo());
        }
        return new PrivacyElemEntity(sjPack.toString(), sjId.toString());
    }

    private void generateProperty(PrivacyElemEntity addStrPee, PrivacyElemEntity deleteStrPee,
                                  PrivacyElemEntity grantStrPee, PrivacyElemEntity revokeStrPee,
                                  PrivacyElemEntity gainStrPee, PrivacyElemEntity updateStrPee,
                                  PrivacyElemEntity useStrPee, PrivacyElemEntity senStrPee, String authorityName) {
        String errorInfo = "Failed to write configuration information";
        URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        if (url == null) {
            System.out.println(errorInfo);
            return;
        }
        String path = url.getPath();
        if (path.contains("target")) {
            path = path.substring(0, path.indexOf("target"));
        }
        path = path + "src/main/resources/privacyconfig.properties";

        InputStream in;
        try {
            in = new FileInputStream(path);
        } catch (FileNotFoundException notFoundException) {
            try {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream("privacyconfig_bak.properties");
            } catch (Exception e) {
                System.out.println(errorInfo + ": " + e);
                return;
            }
        } catch (Exception e) {
            System.out.println(errorInfo + ": " + e);
            return;
        }

        OutputStream out = null;
        try {
            CustomProperties properties = new CustomProperties();
            properties.load(in);

            properties.put("privacy.addDevice.packageMethodName", addStrPee.getMethodOrClassPath());
            properties.put("privacy.addDevice.idMethodName", addStrPee.getFieldInfo());

            properties.put("privacy.deleteDevice.packageMethodName", deleteStrPee.getMethodOrClassPath());
            properties.put("privacy.deleteDevice.idPosition", deleteStrPee.getFieldInfo());

            properties.put("privacy.grantAuth.packageMethodName", grantStrPee.getMethodOrClassPath());
            properties.put("privacy.grantAuth.idMethodName", grantStrPee.getFieldInfo());

            properties.put("privacy.revokeAuth.packageMethodName", revokeStrPee.getMethodOrClassPath());
            properties.put("privacy.revokeAuth.idMethodName", revokeStrPee.getFieldInfo());

            properties.put("privacy.getInfo.packageMethodName", gainStrPee.getMethodOrClassPath());
            properties.put("privacy.getInfo.idMethodName", gainStrPee.getFieldInfo());

            properties.put("privacy.updateInfo.packageMethodName", updateStrPee.getMethodOrClassPath());
            properties.put("privacy.updateInfo.idMethodName", updateStrPee.getFieldInfo());

            properties.put("privacy.antiMapping.packageMethodName", useStrPee.getMethodOrClassPath());
            properties.put("privacy.antiMapping.mappingInfoPosition", useStrPee.getFieldInfo());

            properties.put("privacy.mapping.fieldName", senStrPee.getMethodOrClassPath());
            properties.put("privacy.mapping.packageClassName", senStrPee.getFieldInfo());

            properties.put("privacy.authority.name", authorityName);

            out = new FileOutputStream(path);
            properties.store(out, null);

            System.out.println("ConfigurationFile generate success! Please check that these info are correct");

        } catch (Exception e) {
            System.out.println("Failed to write configuration information： " + e);
        } finally {
            closeIoStream(in, out);
        }
    }

    private void findInfoMethod(JavaProjectBuilder builder, List<JavaMethod> addDeviceList,
                                       List<JavaMethod> deleteDeviceList, List<JavaMethod> grantList,
                                       List<JavaMethod> revokeList) {
        for(JavaClass javaClass : builder.getClasses()) {
            List<JavaMethod> methods = javaClass.getMethods();
            for(JavaMethod method : methods) {
                if (!method.isPublic()) {
                    continue;
                }
                String methodName = method.getName().toLowerCase();
                if (javaClass.getName().toLowerCase().contains("device")) {
                    if(methodName.contains("add") || methodName.contains("save") || methodName.contains("regist")) {
                        addDeviceList.add(method);
                        continue;
                    }
                    if(methodName.contains("del") || methodName.contains("delete") || methodName.contains("remove")) {
                        deleteDeviceList.add(method);
                        continue;
                    }
                }
                boolean isAssign = methodName.contains("assign") && !methodName.contains("unassign");
                boolean isAuth = methodName.contains("auth") && !methodName.contains("unauth");
                if(methodName.contains("delegate") || isAssign ||
                        isAuth || methodName.contains("grant")) {
                    grantList.add(method);
                    continue;
                }
                if(methodName.contains("revoke") || methodName.contains("unassign") ||
                        methodName.contains("unauth") || methodName.contains("withdrawal")) {
                    revokeList.add(method);
                }
            }
        }

    }

    private void findInfoMethod(JavaMethod method, String fieldName, String fieldClassPath,
                                 List<JavaMethod> updateInfoList,
                                 List<JavaMethod> gainInfoList, List<JavaMethod> useInfoList) {
        // update and use do not coexist
        // gain and use coexist
        String methodName = method.getName().toLowerCase();
        boolean updateOperate = methodName.contains("save") || methodName.contains("update");
        boolean gainOperate = methodName.contains("get") || methodName.contains("find");

        String fieldName2Lower = fieldName.toLowerCase();
        Class<?> fieldClass = ClassMethodUtil.getClassByName(fieldClassPath);
        if (fieldClass == null) {
            return;
        }
        JavaClass returnedClass = method.getReturns();
        // id handle
        if ("id".equals(fieldName2Lower)) {

            String className = ClassMethodUtil.toLowerCaseFirstOne(fieldClass.getSimpleName());
            ArrayList<String> attributeNameArray = new ArrayList<>();
            attributeNameArray.add(className + "Id");
            String[] classNameArray = className.replaceAll("[A-Z]", " $0").split(" ");
            StringBuilder tempName = new StringBuilder();
            for (int i = 0; i < classNameArray.length - 2; i++) {
                tempName.append(classNameArray[i]);
                String strTemp = className.replace(tempName.toString(), "") + "Id";
                attributeNameArray.add(strTemp);
            }

            for (String attributeName : attributeNameArray) {
                // params returnedObj
                if (isParamsContain(method.getParameters(), attributeName, fieldClass)) {
                    if (updateOperate) {
                        updateInfoList.add(method);
                        return;
                    } else {
                        useInfoList.add(method);
                    }
                }
                if (isReturnedObjContain(returnedClass, attributeName, fieldClass)) {
                    gainInfoList.add(method);
                }
                return;
            }
        }
        // by
        if (methodName.contains("by")) {
            String afterBy = methodName.substring(methodName.indexOf("by"));
            if (afterBy.contains(fieldName2Lower)) {
                if (updateOperate) {
                    updateInfoList.add(method);
                    return;
                }
                useInfoList.add(method);
                if (isReturnedObjContain(returnedClass, fieldName, fieldClass)) {
                    gainInfoList.add(method);
                }
                return;
            }
        }
        // normal① --- method name
        if (methodName.contains(fieldName2Lower) || methodName.contains(getFrontFieldName(fieldName))) {
            if (updateOperate) {
                updateInfoList.add(method);
            } else if (gainOperate) {
                gainInfoList.add(method);
                useInfoList.add(method);
            }
            return;
        }
        // normal② --- params & returnedObj
        if (isParamsContain(method.getParameters(), fieldName, fieldClass)) {
            if (updateOperate) {
                updateInfoList.add(method);
                return;
            }
            useInfoList.add(method);
        }
        if (isReturnedObjContain(returnedClass, fieldName, fieldClass)) {
            gainInfoList.add(method);
        }
    }

    private String findIdInfo(JavaMethod method, boolean isParams) {
        String deviceId = "deviceId";
        String idMethodStr1 = "getDeviceId";
        String idMethodStr2 = "getId";
        if (!isParams) {
            // returned class
            Class<?> actualClass = getActualClass(method.getReturns());
            if (isClassContainMethod(actualClass, idMethodStr1)) {
                return idMethodStr1;
            }
            if (isClassContainMethod(actualClass, idMethodStr2)) {
                return idMethodStr2;
            }
            return null;
        }
        // params
        List<JavaParameter> javaParameters = method.getParameters();
        for (int i = 0; i < javaParameters.size(); i++) {
            JavaClass paramClass = javaParameters.get(i).getJavaClass();
            // basic type
            if (ClassMethodUtil.isBasicType(paramClass)) {
                if (javaParameters.get(i).getName().toLowerCase().contains(deviceId.toLowerCase())) {
                    return i + "";
                }
                continue;
            }
            // object
            Class<?> actualClass = getActualClass(paramClass);
            if (isClassContainMethod(actualClass, idMethodStr1) ||
                    isClassContainMethod(actualClass, idMethodStr2)) {
                return i + "";
            }
        }
        // id
        int idCount = 0;
        int idPosition = 0;
        boolean isFist = true;
        for (int i = 0; i < javaParameters.size(); i++) {
            if (javaParameters.get(i).getName().toLowerCase().contains("id")) {
                idCount++;
                if (isFist) {
                    idPosition = i;
                    isFist = false;
                }
            }
        }
        if (idCount == 1) {
            return idPosition + "";
        }
        return null;
    }

    private boolean isParamsContain(List<JavaParameter> jpList, String fieldName, Class<?> fieldClass) {
        String fieldName2Lower = fieldName.toLowerCase();
        for (JavaParameter jp : jpList) {
            if (jp.getName().toLowerCase().contains(fieldName2Lower)) {
                return true;
            }
            // params -> object
            Class<?> jc = getActualClass(jp.getJavaClass());
            if (ClassMethodUtil.isBasicType(jc) || fieldClass == null || !findInfoOfClass(jc, fieldName, fieldClass)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isReturnedObjContain(JavaClass returnClass, String fieldName, Class<?> fieldClass) {
        Class<?> actualClass = getActualClass(returnClass);
        return !ClassMethodUtil.isBasicType(actualClass)
                && fieldClass != null
                && findInfoOfClass(actualClass, fieldName, fieldClass);
    }

    private Class<?> getActualClass(JavaClass javaClass) {
        String actualName = javaClass.getGenericFullyQualifiedName();
        if (actualName.contains("<") && actualName.contains(">")) {
            actualName = actualName.substring(actualName.indexOf("<") + 1,
                    actualName.indexOf(">"));
        }
        return ClassMethodUtil.getClassByName(actualName);
    }

    private boolean isClassContainMethod(Class<?> actualClass, String methodName) {
        try {
            if (actualClass == null) {
                return false;
            }
            ClassMethodUtil.getDeclaredMethodWithParent(actualClass, methodName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean findInfoOfClass(Class<?> actualClass, String fieldName, Class<?> fieldClass) {
        // deviceId Device(id)
        if (actualClass != null && actualClass.getSimpleName().equals(fieldClass.getSimpleName())) {
            return true;
        }
        return isClassContainMethod(actualClass, ClassMethodUtil.connectMethodStr(fieldName, "get"));
    }

    private String connectStr(JavaMethod method) {
        return method.getDeclaringClass().getPackageName() + "." +
                method.getDeclaringClass().getName() + "." + method.getName();
    }

    private String getFrontFieldName(String fieldName) {
        fieldName = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
        if(fieldName.startsWith("_")) {
            fieldName = fieldName.substring(1);
        }
        String[] fieldNameArr = fieldName.split("_");
        return fieldName.replace(fieldNameArr[fieldNameArr.length-1], "");
    }

    private void closeIoStream(InputStream in, OutputStream out) {
        if (in != null) {
            try {
                in.close();
            } catch (Exception ignored) {}
        }

        if (out != null) {
            try {
                out.close();
            } catch (Exception ignored) {}
        }
    }



/****************************             Old              ****************************/
private void findInfoMethod(JavaMethod method, String fieldName, List<JavaMethod> updateInfoList,
                            List<JavaMethod> gainInfoList, List<JavaMethod> useInfoList) {
    String by = "by";

    String[] senInfoArray = convertField2Array(fieldName);
    String fieldName2Lower = fieldName.toLowerCase();

    String methodName = method.getName().toLowerCase();
    String beforeBy = methodName;
    String afterBy = methodName;

    if (methodName.contains(by)) {
        beforeBy = methodName.substring(0, methodName.indexOf(by));
        afterBy = methodName.substring(methodName.indexOf(by));
    }

    boolean updateOperate = methodName.contains("save") || methodName.contains("update");
    boolean gainOperate = methodName.contains("get") || methodName.contains("find");

    boolean containUpdate = false;
    boolean containGain = false;
    if (beforeBy.contains(fieldName2Lower)) {
        if (updateOperate) {
            updateInfoList.add(method);
            containUpdate = true;
        }else if (gainOperate) {
            gainInfoList.add(method);
            containGain = true;
        }
    }
    if (afterBy.contains(fieldName2Lower)) {
        useInfoList.add(method);
        return;
    }
    if (containUpdate || containGain) {
        return;
    }

    boolean isContainBeforeInfo = false;
    boolean isContainAfterInfo = false;

    for (int i = 0; i < senInfoArray.length - 1; i++) {
        if (beforeBy.contains(senInfoArray[i])) {
            isContainBeforeInfo = true;
        }
        if (afterBy.contains(senInfoArray[i])) {
            isContainAfterInfo = true;
        }
    }

    boolean isFound;
    boolean isFoundUse;

    List<JavaParameter> jpList = method.getParameters();
    for (JavaParameter jp : jpList) {
        // params
        if (jp.getName().toLowerCase().contains(fieldName2Lower)) {
            if (updateOperate) {
                updateInfoList.add(method);
                return;
            }
            useInfoList.add(method);
            break;
        }
        // params -> object
        JavaClass jc = jp.getJavaClass();
        isFoundUse = javaClassContainElem(jc, fieldName, isContainAfterInfo);

        if (updateOperate) {
            if (isContainBeforeInfo == isContainAfterInfo) {
                isFound = isFoundUse;
            } else {
                isFound = javaClassContainElem(jc, fieldName, isContainBeforeInfo);
            }
            if (isFound) {
                updateInfoList.add(method);
                return;
            }
        }

        if (isFoundUse) {
            useInfoList.add(method);
            break;
        }

    }

    // gain
    JavaClass returnClass = method.getReturns();
    isFound = javaClassContainElem(returnClass, fieldName, isContainBeforeInfo);
    if (isFound) {
        gainInfoList.add(method);
    }

}

    private String findIdInfo1(JavaMethod method, boolean isParams) {
        String deviceId = "deviceId";
        if (!isParams) {
            // return
            // deviceId
            JavaClass returnClass = method.getReturns();
            String idFieLdStr = getIdFieldStr(returnClass);

            String actualName = getActualClassName(returnClass);
            JavaClass actualClass = getActualClass(returnClass, actualName);
            clzIdResultMap.put(actualClass, idFieLdStr);

            return idFieLdStr;
        }
        // params
        List<JavaParameter> javaParameters = method.getParameters();
        // deviceId/object
        for (int i = 0; i < javaParameters.size(); i++) {
            JavaClass paramClass = javaParameters.get(i).getJavaClass();
            String packageName = paramClass.getPackageName() + ".";
            if (".".equals(packageName)) {
                if (javaParameters.get(i).getName().toLowerCase().contains(deviceId.toLowerCase())) {
                    return i + "";
                }
                continue;
            }
            String simplyPackage = packageName.substring(0, packageName.indexOf(".", packageName.indexOf(".") + 1));
            if (method.getDeclaringClass().getPackageName().contains(simplyPackage)) {
                // object
                String idFieldStr = getIdFieldStr(paramClass);
                if ("id".equals(idFieldStr) ||  deviceId.equals(idFieldStr)) {
                    return i + "";
                }
                return i + "(" + idFieldStr + ")";
            }
            if (javaParameters.get(i).getName().toLowerCase().contains(deviceId.toLowerCase())) {
                return i + "";
            }
        }
        // id
        int idCount = 0;
        int idPosition = 0;
        boolean isFist = true;
        for (int i = 0; i < javaParameters.size(); i++) {
            if (javaParameters.get(i).getName().toLowerCase().contains("id")) {
                idCount++;
                if (isFist) {
                    idPosition = i;
                    isFist = false;
                }
            }
        }
        if (idCount == 1) {
            return idPosition + "";
        }
        return null;
    }

    private String getIdFieldStr(JavaClass javaClass) {
        String id = "id";
        String deviceId = "deviceId";
        String deviceStr = "device";
        // 解决泛型问题
        String actualName = getActualClassName(javaClass);
        if (!actualName.equals(javaClass.getFullyQualifiedName())) {
            JavaProjectBuilder builder = new JavaProjectBuilder();
            javaClass = builder.getClassByName(actualName);
        }

        for (Map.Entry<JavaClass, String> entry : clzIdResultMap.entrySet()){
            if (entry.getKey().getFullyQualifiedName().equals(javaClass.getFullyQualifiedName())) {
                return entry.getValue();
            }
        }

        // 获取returnClass和superClass的所有字段
        List<JavaField> allJavaFields = new ArrayList<>(javaClass.getFields());
        boolean isContainDevice = javaClass.getName().toLowerCase().contains(deviceStr);
        JavaClass superClass = javaClass.getSuperJavaClass();
        while (!superClass.getName().contains("Object")) {
            allJavaFields.addAll(superClass.getFields());
            if (superClass.getName().toLowerCase().contains(deviceStr)) {
                isContainDevice = true;
            }
            superClass = superClass.getSuperJavaClass();
        }

        // 检查deviceID
        JavaField deviceIdField = getFieldByName(allJavaFields, deviceId);
        if (deviceIdField != null) {
            JavaClass jc = deviceIdField.getType();
            // deviceId中的Id
            String idStr = getIdFieldStr(jc);

            if (idStr != null) {
                return deviceId + "&" + idStr;
            }
            return deviceId;
        }
        if (isContainDevice) {
            if (getFieldByName(allJavaFields, id) != null) {
                return id;
            }
        }

        // 字段Class中是否包含
        for (JavaField jf : allJavaFields) {
            JavaClass jc = jf.getType();
            String idStr = getIdFieldStr(jc);

            if (idStr != null) {
                return jf.getName() + "&" + idStr;
            }
        }
        return null;
    }

    private JavaField getFieldByName(List<JavaField> javaFieldList, String name) {
        Iterator<JavaField> it = javaFieldList.iterator();
        JavaField field;
        do {
            if (!it.hasNext()) {
                return null;
            }
            field = it.next();
        } while(!field.getName().equalsIgnoreCase(name));
        return field;
    }

    private String[] convertField2Array(String fieldName) {
        fieldName = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
        if(fieldName.startsWith("_")) {
            fieldName = fieldName.substring(1);
        }
        return fieldName.split("_");
    }

    private boolean javaClassContainElem(JavaClass javaClass, String element, boolean isContainInfo) {

        boolean isFound = isContainElement(javaClass, element, javaClass.getPackageName(), null);

        if (!isFound && element.toLowerCase().contains("id") && isContainInfo) {
            element = "id";
            isFound = isContainElement(javaClass, element, javaClass.getPackageName(), null);
        }
        String actualName = getActualClassName(javaClass);
        if (actualName.equals(javaClass.getFullyQualifiedName())) {
            clzElemResultMap.put(javaClass.getName(), isFound);
        }
        return isFound;
    }

    private boolean isContainElement(JavaClass javaClass, String element, String originClassName, String beforeClassName) {
        if (javaClass == null || javaClass.getName().equals(beforeClassName)
                || javaClass.getName().contains("Object")) {
            return false;
        }
        for (Map.Entry<String, Boolean> entry : clzElemResultMap.entrySet()){
            if (entry.getKey().contains(javaClass.getName())) {
                return entry.getValue();
            }
        }
        beforeClassName = javaClass.getName();

        // field check
        if(getFieldByName(javaClass.getFields(), element) != null) {
            return true;
        }
        for (JavaField field : javaClass.getFields()) {
            JavaClass fieldClass = field.getType();
            String packageName = fieldClass.getPackageName() + ".";
            if (".".equals(packageName)) {
                continue;
            }
            String simplyPackage = packageName.substring(0, packageName.indexOf(".", packageName.indexOf(".") + 1));
            if (originClassName.contains(simplyPackage)) {
                if (isContainElement(fieldClass, element, originClassName, beforeClassName)) {
                    return true;
                }
            }
        }

        // GenericFullyQualified
        String actualName = getActualClassName(javaClass);
        if (!actualName.equals(javaClass.getFullyQualifiedName())) {
            JavaProjectBuilder builder = new JavaProjectBuilder();
            JavaClass actualClass = builder.getClassByName(actualName);
            if (isContainElement(actualClass, element, originClassName, beforeClassName)) {
                return true;
            }
        }
        // Super Class
        JavaClass superClass = javaClass.getSuperJavaClass();
        return isContainElement(superClass, element, originClassName, beforeClassName);

    }

    private JavaClass getActualClass(JavaClass javaClass, String actualName) {
        JavaClass actClass = javaClass;
        if (!actualName.equals(javaClass.getFullyQualifiedName())) {
            JavaProjectBuilder builder = new JavaProjectBuilder();
            actClass = builder.getClassByName(actualName);
        }
        return actClass;
    }

    private String getActualClassName(JavaClass javaClass) {
        String actualName = javaClass.getGenericFullyQualifiedName();
        if (actualName.contains("<") && actualName.contains(">")) {
            actualName = actualName.substring(actualName.indexOf("<") + 1,
                    actualName.indexOf(">"));
        }
        return actualName;
    }

}

