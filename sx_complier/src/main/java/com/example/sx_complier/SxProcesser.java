package com.example.sx_complier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.sx_annotation.SxArouter;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * 注解处理器  处理注解
 * 是Java工程
 */

@AutoService(Process.class)//启用autoService服务
@SupportedAnnotationTypes({"com.example.sx_annotation.SxArouter"})//支持处理哪些注解
@SupportedSourceVersion(SourceVersion.RELEASE_11)//支持的jdk版本

//接收Android 工程的传参  HOST  要保持一致相当于 key
@SupportedOptions("HOST")
public class SxProcesser extends AbstractProcessor {
    private static final String OUTPUT_FILE_NAME = "destination.json";
    //操作类 函数 属性
    private Elements elementTool;
    //类信息工具 包含操作  TypeMirror的方法
    private Types typeTool;
    //打印日志
    private Messager messager;
    //文件生成器  类资源等 最终要生成的文件 需要通过 Filer
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        Map<String, String> options = processingEnv.getOptions();
        String host = options.get("HOST");
        // Diagnostic.Kind.ERROR 是用来处理异常的
        messager.printMessage(Diagnostic.Kind.NOTE,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+host);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>process");
        if (set == null || set.isEmpty()){
            return false;
        }

        //收集信息
        HashMap<String,JSONObject> hashMap = new HashMap<>();
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(SxArouter.class);
        for (Element element:elements) {
            SxArouter annotation = element.getAnnotation(SxArouter.class);

            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getQualifiedName().toString();
            boolean isFragment = true;
            boolean asStarter = annotation.asStarter();
            boolean needLogin = annotation.needLogin();
            String pageUrl = annotation.pageUrl();
            long id = Math.abs(className.hashCode());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("className",className);
            jsonObject.put("isFragment",isFragment);
            jsonObject.put("asStarter",asStarter);
            jsonObject.put("needLogin",needLogin);
            jsonObject.put("pageUrl",pageUrl);
            jsonObject.put("id",id);
            hashMap.put(pageUrl,jsonObject);
        }

        //输出文件
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        FileObject resource = null;
        try {
            //app/src/main/assets/destination.json
            resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
            String resourcePath = resource.toUri().getPath();
            messager.printMessage(Diagnostic.Kind.NOTE, "resourcePath: " + resourcePath);
            String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
            String assetsPath = appPath + "src/main/assets";

            File file = new File(assetsPath);
            if (!file.exists()) {
                file.mkdir();
            }
            File outputFile = new File(file, OUTPUT_FILE_NAME);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            String content = JSON.toJSONString(hashMap);
            fileOutputStream = new FileOutputStream(outputFile);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            outputStreamWriter.write(content);
            outputStreamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (outputStreamWriter!= null){
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fileOutputStream!= null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }





        return true;
    }

//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }
//
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        return super.getSupportedAnnotationTypes();
//    }


    private void generateFindClass(Element element){
        //通过 element 获取包节点
        String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
        SxArouter annotation = element.getAnnotation(SxArouter.class);
        //类名
        String findClassName = element.getSimpleName().toString()+"$$$SxArouter";
        // 1.方法
        MethodSpec methodSpec = MethodSpec
                .methodBuilder("findTargetClass")
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .returns(Class.class)
                .addParameter(String.class,"path")
                .addStatement("return path.equals($S) ? $T.class : null",annotation.pageUrl(), ClassName.get((TypeElement) element))
                .build();

        //2.类
        TypeSpec typeSpec = TypeSpec
                .classBuilder(findClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();

        //3.包
        JavaFile javaFile = JavaFile
                .builder(packageName,typeSpec)
                .build();

        //4.写入文件
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+e.toString());
        }
    }


    /**
     * public final class SxTest {
     *   public static void main(String[] args) {
     *     System.out.println("hello javapoet!");
     *   }
     * }
     */

    private void generateTestClass(){
        //1.创建方法   T 表示类 接口   S 表示字符串
        MethodSpec methodSpec = MethodSpec
                .methodBuilder("main")//方法名
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)//方法修饰符
                .returns(void.class)
                .addParameter(String[].class,"args")//参数
                .addStatement("$T.out.println($S)",System.class,"hello javapoet!")//方法体
                .build();


        //2.类
        TypeSpec typeSpec = TypeSpec
                .classBuilder("SxTest")
                .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                .addMethod(methodSpec)
                .build();


        //3.包
        JavaFile javaFile = JavaFile
                .builder("com.example.sxarouter",typeSpec)
                .build();


        //4.写入文件
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+e.toString());
        }
    }
}