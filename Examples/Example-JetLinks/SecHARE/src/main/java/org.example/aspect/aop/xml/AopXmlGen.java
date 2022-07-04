package org.example.aspect.aop.xml;

import org.example.aspect.aop.PtAbstractAspect;
import org.example.aspect.entity.AopXmlEntity;
import org.example.aspect.entity.PrivacyElemEntity;
import org.example.aspect.entity.PrivacySensitiveInfoEntity;
import org.example.global.PrivacyGlobalValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
public class AopXmlGen {

    private static final String EXPRESS_STR = "execution(public * xx.xxx.xxx.xxx..*(..))";

    private static final HashSet<String> W_INCLUDE_SET = new HashSet<>();

    public static void execute() {
        try {
            AopXmlEntity aopXmlEntity = getAopXmlEntity();
            createXml(aopXmlEntity, createAopXmlFile());
            log.info("[PrivacyTool] Successfully generated META-INF/aop.xml! ");
        } catch (Exception e) {
            log.error("[PrivacyTool] Error generated META-INF/aop.xml! Because {}", e.getMessage(), e);
        }
    }

    public static AopXmlEntity getAopXmlEntity() {
        AopXmlEntity entity = new AopXmlEntity();
        String pkgName = PtAbstractAspect.class.getPackage().getName();
        entity.setConcreteAspectName(pkgName + ".ConcreteAspect");
        entity.setConcreteAspectExtends(PtAbstractAspect.class.getName());

        Map<String, String> pointcutMap = new HashMap<>(1);
        pointcutMap.put("ptAroundPointcut", getAroundExpression());
        entity.setPointcutMap(pointcutMap);

        entity.setWeaverOptions("-Xlint:ignore");
        entity.setWeaverIncludeSet(W_INCLUDE_SET);
        return entity;
    }

    public static void createXml(AopXmlEntity aopEntity, File file) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        Document document = db.newDocument();
        document.setXmlStandalone(true);

        Element aspectj = document.createElement("aspectj");

        Element aspects = document.createElement("aspects");
        Element concreteAspect = document.createElement("concrete-aspect");
        concreteAspect.setAttribute("name", aopEntity.getConcreteAspectName());
        concreteAspect.setAttribute("extends", aopEntity.getConcreteAspectExtends());
        for(Map.Entry<String, String> pointcutEntry : aopEntity.getPointcutMap().entrySet()){
            Element pointcut = document.createElement("pointcut");
            pointcut.setAttribute("name", pointcutEntry.getKey());
            pointcut.setAttribute("expression", pointcutEntry.getValue());

            concreteAspect.appendChild(pointcut);
        }
        aspects.appendChild(concreteAspect);

        Element weaver = document.createElement("weaver");
        weaver.setAttribute("options", aopEntity.getWeaverOptions());
        for (String wIncludeWithin : aopEntity.getWeaverIncludeSet()) {
            Element include = document.createElement("include");
            include.setAttribute("within", wIncludeWithin);

            weaver.appendChild(include);
        }

        aspectj.appendChild(aspects);
        aspectj.appendChild(weaver);
        document.appendChild(aspectj);

        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = tff.newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.transform(new DOMSource(document), new StreamResult(file));

    }

    private static File createAopXmlFile() throws Exception {
        String resourcePath = AopXmlGen.class.getResource("/").getPath();
        String folderName = "/META-INF";
        String fileName = "/aop.xml";
        File dir = new File(resourcePath + folderName);
        File aopFile = new File(dir.getPath() + fileName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (aopFile.exists()) {
            aopFile.delete();
        }
        aopFile.createNewFile();
        return aopFile;
    }

    private static String getAroundExpression() {
        StringBuilder expSb = new StringBuilder();
        // add/del device, del user
        PrivacyElemEntity addDevicePee = PrivacyGlobalValue.get().getPlcEntity().getAddDevice();
        PrivacyElemEntity delDevicePee = PrivacyGlobalValue.get().getPlcEntity().getDeleteDevice();
        PrivacyElemEntity delUserPee = PrivacyGlobalValue.get().getPlcEntity().getDeleteUser();
        addExpression(addDevicePee, expSb);
        addExpression(delDevicePee, expSb);
        addExpression(delUserPee, expSb);
        // update sensitive info
        PrivacySensitiveInfoEntity[] psEntityArray = PrivacyGlobalValue.get().getPlcEntity().getSenInfoOperateEntity();
        for (PrivacySensitiveInfoEntity pse : psEntityArray) {
            PrivacyElemEntity[] updateSenInfoPeeArray = pse.getUpdateSenInfo();
            addExpression(updateSenInfoPeeArray, expSb);
        }

        // grant revoke
        PrivacyElemEntity[] grantAuthPeeArray = PrivacyGlobalValue.get().getPlcEntity().getGrantAuth();
        PrivacyElemEntity[] revokeAuthPeeArray = PrivacyGlobalValue.get().getPlcEntity().getRevokeAuth();
        addExpression(grantAuthPeeArray, expSb);
        addExpression(revokeAuthPeeArray, expSb);

        // verify and get
        for (PrivacySensitiveInfoEntity pse : psEntityArray) {
            // verify sensitive info
            PrivacyElemEntity[] verifySenInfoPeeArray = pse.getVerifySenInfo();
            addExpression(verifySenInfoPeeArray, expSb);
            // get protected info
            PrivacyElemEntity[] getProtectedPeeArray = pse.getGetProtectedInfo();
            addExpression(getProtectedPeeArray, expSb);
        }

        if (expSb.length() != 0) {
            expSb.delete(expSb.length() - 4, expSb.length());
        } else {
            expSb.append(EXPRESS_STR);
        }
        return expSb.toString();
    }

    private static void addExpression(PrivacyElemEntity pee, StringBuilder sb) {
        if (pee != null && StringUtils.isNotEmpty(pee.getMethodOrClassPath())) {
            String methodPath = pee.getMethodOrClassPath();
            sb.append("execution(* ").append(methodPath).append("(..))").append(" || ");

            String[] pathSplit = methodPath.split("\\.");
            W_INCLUDE_SET.add(methodPath.replace("." + pathSplit[pathSplit.length - 1], ""));
        }
    }

    private static void addExpression(PrivacyElemEntity[] peeArray, StringBuilder sb) {
        if (ArrayUtils.isEmpty(peeArray)) {
            return;
        }
        for (PrivacyElemEntity pee : peeArray) {
            addExpression(pee, sb);
        }
    }

}
