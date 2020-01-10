package com.md.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author mac
 */
@Slf4j
public class PropertyUtils {

    public static Map<String,String> readPropertyVlaue(String filePath){
        Properties prop = new Properties();
        Map<String,String> map = new HashMap<>();
        try{
            //读取属性文件a.properties
            InputStream in = new BufferedInputStream (new FileInputStream(filePath));
            // 加载属性列表
            prop.load(in);
            for (String key : prop.stringPropertyNames()) {
                map.put(key, prop.getProperty(key));
            }
            in.close();

            ///保存属性到b.properties文件

        } catch(Exception e){
            log.error(e.getMessage(),e);
        }
        return map;
    }

    public static void writePropertyValue(String filePath,Map<String,String> data){
        try {
            Properties prop = new Properties();
            FileOutputStream oFile = new FileOutputStream(filePath);
            for (String key :
                    data.keySet()) {
                prop.setProperty(key, data.get(key));
            }
            prop.store(oFile, "This is config file!");
            oFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Map<String,String> data = PropertyUtils.readPropertyVlaue("/Users/mac/IdeaProject/anhui/server/src/main/resources/a.properties");
        for (String key :
                data.keySet()) {
            System.out.println(key+":"+data.get(key));
        }
        data.put("test","11111");
        PropertyUtils.writePropertyValue("/Users/mac/IdeaProject/anhui/server/src/main/resources/a.properties",data);

    }
}
