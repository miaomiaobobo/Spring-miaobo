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

    HashMap<String, Object> singlePool = new HashMap<>();//������
    ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();//����BeanDefinition
    ArrayList<BeanPostProcessor> beanPostProcessorArrayList= new ArrayList<>();

    public MiaoboApplicationContext(Class configClass) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.configClass = configClass;
        //ɨ��·��->��ȡע��@Component->����BeanDefinition������BeanDefinitionMap
        scan(configClass);
        //����BeanDefinitionMap�е�BeanDefinition������������
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
        //������ע���ȡɨ��·��
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".","/");
        //��ȡӦ���������
        ClassLoader configClassLoader = MiaoboApplicationContext.class.getClassLoader();
        //����ɨ��·������ȡĿ¼���ر�ע������Ŀ¼������ո������
        URL resource = configClassLoader.getResource(path);//�������λ�ڵ�Ŀ¼Ϊ\Spring-miaobo\target\classes"
        File directory = new File(resource.getFile());
        //�����ļ����鿴�Ƿ����@Componentע��
        if(directory.isDirectory()){
            File[] files = directory.listFiles();
            for(File file: files){
                //����·��תΪ�������ʹ�õ�·����ʽ
                String fileName = file.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    fileName = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    fileName = fileName.replace("\\", ".");
                    //ʹ���������������,�����뵥����
                    Class<?> clazz = configClassLoader.loadClass(fileName);
                    //�ж��Ƿ���@Componentע��
                    if (clazz.isAnnotationPresent(Component.class)) {
                        //�жϵ�ǰclazz�Ƿ�ʵ����BeanPostProcessor�ӿڣ��Ǿʹ���List
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            beanPostProcessorArrayList.add(beanPostProcessor);
                        }

                    //��ȡBeanName
                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.value();
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);
                        //�ж��Ƿ�Ϊ�������򴴽������ص�����
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
        //����ע��:����������-����Щ��@Autowired
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Autowired.class)) {
                declaredField.setAccessible(true);
                Object bean = getBean(declaredField.getName());
                declaredField.set(o, bean);
            }
        }
        //����Aware�ӿ�
        if (o instanceof BeanNameAware) {
            ((BeanNameAware)o).setBeanName(beanName);
        }
        //��ʼ��ǰ����
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorArrayList) {
            o = beanPostProcessor.postProcessorBeforeInitializing(o, beanName);
        }
        //��ʼ��
        if (o instanceof InitializingBean) {
            ((InitializingBean)o).afterPropertiesSet();
        }
        //��ʼ�������
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorArrayList) {
            o = beanPostProcessor.postProcessorAfterInitializing(o, beanName);
        }
        return o;
    }
}
