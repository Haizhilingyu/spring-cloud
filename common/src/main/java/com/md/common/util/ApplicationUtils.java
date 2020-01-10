package com.md.common.util;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * ApplicationUtils : 程序工具类，提供大量的便捷方法
 * @author mac
 */
@Slf4j
public class ApplicationUtils {

    /**
     * 产生一个36个字符的UUID
     *
     * @return UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 本地执行 sh 命令
     *
     * @param cmd shell命令
     * @return 终端内容
     */
    public static StringBuffer exeCMD(String cmd) {
        StringBuffer buffer = new StringBuffer();
        BufferedReader bfRd = null;
        Process process = null;
        try {
            String[] cmdArray = {"/bin/bash", "-c", cmd};
            process = Runtime.getRuntime().exec(cmdArray);
            bfRd = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line;
            while ((line = bfRd.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
            process.destroy();
        } catch (Exception e) {
            log.error("Linux erro ------------ :" + cmd);
            log.error("终端命令异常", e.fillInStackTrace());
            return null;
        } finally {
            try {
                if (bfRd != null) {
                    bfRd.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                log.error("释放资源异常", e.fillInStackTrace());
            }
        }
        return buffer;
    }

    /**
     * 连通性检查
     *
     * @return
     */
    public int ping(String ip) throws IOException {
        StringBuffer sb;
        String exe = String.format("ping %s -c 4", ip);
        sb = exeCMD(exe);
        if (sb != null) {
            return sb.indexOf("ttl") > -1 ? 1 : 2;
        }
        return 2;
    }
}

