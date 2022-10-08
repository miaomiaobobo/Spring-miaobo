package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MiaoboApplicationContext {
    Class configClass;

    HashMap<String, Object> singlePool = new HashMap<>();//单例池
    ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();//放入BeanDefinition
    ArrayList<BeanPostProcessor> beanPostProcessorArrayList= new ArrayList<>();

    public MiaoboApplicationContext(Class configClass) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.configClass = configClass;
        //扫描路径->获取注解@Component->创建BeanDefinition并放入BeanDefinitionMap
        scan(configClass);
        //根据BeanDefinitionMap中的BeanDefinition创建单例对象
        for(Map.Entry<String, BeanDefinition> entry: beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = createBean(beanName);
                singlePool.put(beanName, o);
            }
        }

    }

    private void scan(Class configClass) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //配置类注解获取扫描路径
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".","/");
        //获取应用类加载器
        ClassLoader configClassLoader = MiaoboApplicationContext.class.getClassLoader();
        //加载扫描路径，获取目录。特别注意所有目录不允许空格和中文
        URL resource = configClassLoader.getResource(path);//类加载器位于的目录为\Spring-miaobo\target\classes"
        File directory = new File(resource.getFile());
        //遍历文件，查看是否带有@Component注解
        if(directory.isDirectory()){
            File[] files = directory.listFiles();
            for(File file: files){
                //绝对路径转为类加载器使用的路径格式
                String fileName = file.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    fileName = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    fileName = fileName.replace("\\", ".");
                    //使用类加载器加载类,并放入单例池
                    Class<?> clazz = configClassLoader.loadClass(fileName);
                    //判断是否有@Component注解
                    if (clazz.isAnnotationPresent(Component.class)) {
                        //判断当前clazz是否实现了BeanPostProcessor接口，是就存入List
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            beanPostProcessorArrayList.add(beanPostProcessor);
                        }

                    //获取BeanName
                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.value();
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);
                        //判断是否为单例是则创建并返回单例池
                        Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                        if (scopeAnnotation.value().equals("prototype")) {
                            beanDefinition.setScope("prototype");
                        }
                        else {
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singlePool.get(beanName);
                return o;
            }
            else {
                return createBean(beanName);
            }
        }
        else {
            throw new  NullPointerException();
        }

    }

    private Object createBean(String beanName) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Class clazz = beanDefinition.getClazz();
        Object o = clazz.getConstructor().newInstance();
        //依赖注入:找所有属性-》哪些有@Autowired
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Autowired.class)) {
                declaredField.setAccessible(true);
                Object bean = getBean(declaredField.getName());
                declaredField.set(o, bean);
            }
        }
        //处理Aware接口
        if (o instanceof BeanNameAware) {
            ((BeanNameAware)o).setBeanName(beanName);
        }
        //初始化前操作
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorArrayList) {
            o = beanPostProcessor.postProcessorBeforeInitializing(o, beanName);
        }
        //初始化
        if (o instanceof InitializingBean) {
            ((InitializingBean)o).afterPropertiesSet();
        }
        //初始化后操作
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorArrayList) {
            o = beanPostProcessor.postProcessorAfterInitializing(o, beanName);
        }
        return o;
    }
}
