package com.md.common.util;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Created by zlei on 6/13/17.
 */
@Slf4j
public class NetworkUtil {

    private static final String NETWORKPATH = "/etc/sysconfig/network-scripts/";

    /**
     * 获取指定网卡名称的ip地址
     *
     * @param networkName
     * @return
     */
    public static String getIpByNetworkName(String networkName) {
        String ip = null;
        Enumeration<NetworkInterface> networks = null;
        try {
            networks = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return ip;
        }

        NetworkInterface networkif;
        String name = "", ipv4 = "";
        while (networks.hasMoreElements()) {
            networkif = networks.nextElement();
            name = networkif.getName();
            if (!networkName.equals(name)) {
                continue;
            }
            //ipv4
            for (Enumeration<InetAddress> i = networkif.getInetAddresses(); i.hasMoreElements(); ) {
                InetAddress ia = i.nextElement();
                if (ia instanceof Inet4Address) {
                    ipv4 = ia.getHostAddress();
                }
            }
        }
        return ipv4;
    }

    /**
     * 获取网卡信息
     *
     * @return 网卡列表
     */
    public static List<Network> getNetwork() {

        List<Network> rsList = new ArrayList();
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            NetworkInterface networkif;

            String name = "", ipv4 = "", ipv6 = "", mac = "", code = "", gate = "";
            int manager = 0;

            while (networks.hasMoreElements()) {

                networkif = networks.nextElement();

                name = networkif.getName();
                log.debug(" name:" + name);
                if ("lo".equalsIgnoreCase(name)) {
                    continue;
                }

                Network network = new Network();
                //ipv4 & ipv6
                for (Enumeration<InetAddress> i = networkif.getInetAddresses(); i.hasMoreElements(); ) {
                    InetAddress ia = i.nextElement();
                    log.debug(" InetAddress:" + ia.getHostAddress());
                    if (ia instanceof Inet6Address) {
                        ipv6 = ia.getHostAddress();
                    }

                    if (ia instanceof Inet4Address) {
                        ipv4 = ia.getHostAddress();
                    }
                }

                //code
                List<InterfaceAddress> list = networkif.getInterfaceAddresses();
                for (InterfaceAddress ifa : list) {
                    InetAddress address = ifa.getAddress();
                    if (address.isLoopbackAddress() == true || address.getHostAddress().contains(":")) {
                        continue;
                    }
                    code = getMask(ifa.getNetworkPrefixLength());
                }

                gate = getGateway(name);
                mac = getMac(networkif);

                network.setName(name);
                network.setIpv4(ipv4);
                network.setIpv6(ipv6);
                network.setMac(mac);
                network.setCode(code);
                network.setGate(gate);
                network.setManager(manager);

                log.info("ipv4:" + ipv4
                        + " ipv6:" + ipv6
                        + " name:" + name
                        + " mac:" + mac
                        + " gate:" + gate
                        + " manager:" + manager
                        + " code:" + code);

                rsList.add(network);
            }
        } catch (Exception e) {
            log.error("网卡获取失败", e);
        }
        return rsList;
    }

    /**
     * 获取给定网卡名称的网关信息
     *
     * @return
     */
    private static String getGateway(String name) {
        String gate = "";
        String path = NETWORKPATH + "ifcfg-" + name;
        File file = new File(path);
        log.debug("path:" + path);
        if (!file.exists()) {
            log.info("file not exists");
            return gate;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String rs;
            while ((rs = br.readLine()) != null) {
                if (rs.contains("GATEWAY")) {
                    log.debug(rs.substring(rs.indexOf("=") + 1));
                    return rs.substring(rs.indexOf("=") + 1);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("", e);
        } catch (Exception e) {
            log.error("", e);
        }
        return gate;
    }

    /**
     * 更新网卡配置文件
     * 0失败，1成功
     *
     * @param network
     * @return
     */
    private static int updateNetwork(Network network) {
        String name, code, gate, ip;
        final String NETMASK = "NETMASK", IPADDR = "IPADDR", GATEWAY = "GATEWAY", DEVICE = "DEVICE";
        PrintWriter printwriter = null;
        StringBuffer sb = new StringBuffer();

        name = network.getName();
        if (StringUtils.isEmpty(name)) {
            return NetworkEnum.FAIL.getStateNum();
        }

        String path = NETWORKPATH + "ifcfg-" + name;
        log.debug("path:" + path);
        File file = new File(path);
        try {
            if (!file.exists() && file.createNewFile()) {
                //文件不存在，新建文件成功
                code = StringUtils.isEmpty(network.getCode()) ? null : network.getCode();
                gate = StringUtils.isEmpty(network.getGate()) ? null : network.getGate();
                ip = StringUtils.isEmpty(network.getIpv4()) ? null : network.getIpv4();
                sb.append(DEVICE + "=").append(name).append("\n");
                sb.append("BOOTPROTO=static" + "\n");
                sb.append("NM_CONTROLLED=\"yes\"\n");
                sb.append("ONBOOT=\"yes\"\n");
                sb.append(IPADDR + "=").append(ip).append("\n");
                sb.append(NETMASK + "=").append(code).append("\n");
                sb.append(GATEWAY + "=").append(gate).append("\n");
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String rs;
                while ((rs = br.readLine()) != null && !StringUtils.isEmpty(rs)) {
                    if (rs.contains(GATEWAY) && StringUtils.isEmpty(network.getGate())) {
                        continue;
                    }
                    if (rs.contains(DEVICE) && !StringUtils.isEmpty(name)) {
                        sb.append(DEVICE + "=" + name + "\n");
                        continue;
                    } else if (rs.contains(NETMASK) && !StringUtils.isEmpty(network.getCode())) {
                        sb.append(NETMASK + "=" + network.getCode() + "\n");
                        continue;
                    } else if (rs.contains(IPADDR) && !StringUtils.isEmpty(network.getIpv4())) {
                        sb.append(IPADDR + "=" + network.getIpv4() + "\n");
                        continue;
                    } else if (rs.contains(GATEWAY) && !StringUtils.isEmpty(network.getGate())) {
                        sb.append(GATEWAY + "=" + network.getGate() + "\n");
                        continue;
                    } else {
                        sb.append(rs + "\n");
                    }
                }

                if (!sb.toString().contains(NETMASK) && !StringUtils.isEmpty(network.getCode())) {
                    sb.append(NETMASK + "=" + network.getCode() + "\n");
                }

                if (!sb.toString().contains(IPADDR) && !StringUtils.isEmpty(network.getIpv4())) {
                    sb.append(IPADDR + "=" + network.getIpv4() + "\n");
                }

                if (!sb.toString().contains(GATEWAY) && !StringUtils.isEmpty(network.getGate())) {
                    sb.append(GATEWAY + "=" + network.getGate() + "\n");
                }

                if (!sb.toString().contains(DEVICE) && !StringUtils.isEmpty(name)) {
                    sb.append(DEVICE + "=" + name + "\n");
                }
            }
            log.debug(sb.toString());
            printwriter = new PrintWriter(new FileWriter(path, false));
            printwriter.println(sb.toString());
            printwriter.flush();

            return NetworkEnum.SUCCESS.getStateNum();
        } catch (Exception e) {
            log.error("修改网卡失败", e);
        } finally {
            if (printwriter != null) {
                printwriter.close();
            }
        }
        return NetworkEnum.FAIL.getStateNum();
    }

    /**
     * 网卡修改
     *
     * @param network 网卡信息
     * @return 1成功，0失败,2同网关失败，3同ip失败
     */
    public static int networkUpdate(Network network) {
        //检查是否和已有网卡的网段相同
        //默认两张网卡，此处的检查方法只针对两张网卡的情况
        String networkName = network.getName();
        File dir = new File(NETWORKPATH);
        if (!dir.exists() || !dir.isDirectory()) {
            return NetworkEnum.FAIL.getStateNum();
        }

        File[] files = dir.listFiles();
        if (files.length <= 0) {
            return NetworkEnum.FAIL.getStateNum();
        }

        for (File file : files) {
            //排除 ifcfg-lo 文件
            String fileName = file.getName();
            if (fileName.startsWith("ifcfg-") && !fileName.equalsIgnoreCase("ifcfg-lo") &&
                    !fileName.equalsIgnoreCase("ifcfg-" + networkName)) {
                //禁止两张网卡同ip，同网关，允许同网段
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String str;
                    while ((str = br.readLine()) != null && !StringUtils.isEmpty(str)) {
                        //GATEWAY IPADDR
                        if (str.contains("IPADDR") && !StringUtils.isEmpty(network.getIpv4())) {
                            if (str.contains(network.getIpv4())) {
                                return NetworkEnum.FAIL_IP.getStateNum();
                            }
                            continue;
                        }
                        if (str.contains("GATEWAY") && !StringUtils.isEmpty(network.getGate())) {
                            if (str.contains(network.getGate())) {
                                return NetworkEnum.FAIL_GATE.getStateNum();
                            }
                            continue;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

        int rs = NetworkUtil.updateNetwork(network);
        if (rs == 1) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    String cmd = "/etc/init.d/network restart";
                    log.info(cmd);
                    ApplicationUtils.exeCMD(cmd);
                } catch (Exception e) {
                    log.error("网卡修改失败",e);
                }
            });
            thread.start();
        }
        return rs;
    }


    /**
     * 获取给定网卡的MAC地址
     *
     * @return
     */
    private static String getMac(NetworkInterface networkInterface) {
        StringBuffer sb = new StringBuffer();
        try {
            if (networkInterface.getHardwareAddress() != null) {
                // 获得MAC地址
                //结果是一个byte数组，每项是一个byte，我们需要通过parseByte方法转换成常见的十六进制表示
                byte[] addres = networkInterface.getHardwareAddress();
                if (addres != null && addres.length > 1) {
                    sb.append(parseByte(addres[0])).append(":").append(
                            parseByte(addres[1])).append(":").append(
                            parseByte(addres[2])).append(":").append(
                            parseByte(addres[3])).append(":").append(
                            parseByte(addres[4])).append(":").append(
                            parseByte(addres[5]));
                }
            }
        } catch (SocketException e) {
            log.error("网卡Mac获取失败", e);
        }
        return sb.toString();
    }

    /**
     * 获取子网掩码
     *
     * @param length
     * @return
     */
    private static String getMask(int length) {
        int mask = -1 << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;
        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }
        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }

    /**
     * 格式化二进制
     *
     * @param b
     * @return
     */
    private static String parseByte(byte b) {
        String s = "00" + Integer.toHexString(b);
        return s.substring(s.length() - 2);
    }

}
