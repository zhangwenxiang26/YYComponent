package com.example.lib_common.bean;

public class PathBean {
    private String pathName;//路径
    private Class<?> aClass;//对应的类
    public PathBean(){}

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    public PathBean(String path, Class aClass){

    }


}
