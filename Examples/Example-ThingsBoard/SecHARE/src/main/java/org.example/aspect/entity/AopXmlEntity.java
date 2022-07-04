package org.example.aspect.entity;

import java.util.HashSet;
import java.util.Map;

public class AopXmlEntity {

    private String concreteAspectName;
    private String concreteAspectExtends;

    private Map<String ,String> pointcutMap;

    private String weaverOptions;

    private HashSet<String> weaverIncludeSet;

    public String getConcreteAspectName() {
        return concreteAspectName;
    }

    public void setConcreteAspectName(String concreteAspectName) {
        this.concreteAspectName = concreteAspectName;
    }

    public String getConcreteAspectExtends() {
        return concreteAspectExtends;
    }

    public void setConcreteAspectExtends(String concreteAspectExtends) {
        this.concreteAspectExtends = concreteAspectExtends;
    }

    public Map<String, String> getPointcutMap() {
        return pointcutMap;
    }

    public void setPointcutMap(Map<String, String> pointcutMap) {
        this.pointcutMap = pointcutMap;
    }

    public String getWeaverOptions() {
        return weaverOptions;
    }

    public void setWeaverOptions(String weaverOptions) {
        this.weaverOptions = weaverOptions;
    }

    public HashSet<String> getWeaverIncludeSet() {
        return weaverIncludeSet;
    }

    public void setWeaverIncludeSet(HashSet<String> weaverIncludeSet) {
        this.weaverIncludeSet = weaverIncludeSet;
    }
}
