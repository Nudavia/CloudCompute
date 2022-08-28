package service;

import entity.VM;
import entity.VMInfo;
import manager.SystemManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.libvirt.*;
import utils.LinuxConnection;
import utils.RandomCreator;
import utils.XMLUtils;
import utils.ZKClient;

import java.io.*;
import java.util.*;

/**
 * @Auther: ldw
 * @Date: 2022/3/17
 */
public class VMService {
    private final Logger logger = Logger.getLogger("VMService");

    public List<VM> listVM() {
        SystemManager.refreshVM();
        List<VM> vmList = new LinkedList<>();
        for (Map.Entry<String, VM> entry : SystemManager.getVmInfoTable().entrySet()) {
            vmList.add(entry.getValue());
        }
        return vmList;
    }

    public VM createVM(String domainName, long capacity, long memory, int vcpu, String ip, String nodeIp) {
        if (nodeIp == null)
            nodeIp = SystemManager.chooseNodeIP();
        String nodeHostname = SystemManager.getDnTable().get(nodeIp);
        String poolName = SystemManager.POOL;
        String poolPath = SystemManager.POOL_PATH;
        Connect connect = SystemManager.getConnectTable().get(nodeIp);
        String volName = domainName + "_" + System.currentTimeMillis();
        String volPath = poolPath + "/" + volName;
        String volXML = XMLUtils.createVolXML(volPath, volName, capacity);
        String mac = RandomCreator.createMac();
        String domainXML = XMLUtils.createDomainXML(domainName, memory, vcpu, volPath, mac);
        try {
            StoragePool pool = connect.storagePoolLookupByName(poolName);
            StorageVol vol = pool.storageVolCreateXML(volXML, 0);
            Domain domain = connect.domainDefineXML(domainXML);
            domain.setAutostart(false);
            VM vm = new VM(nodeIp, domainName, 0, ip, memory, vcpu, capacity, domain.isPersistent() == 1);
            SystemManager.getVmInfoTable().put(domainName, vm);
            SystemManager.getVmTable().put(domainName, domain);
            SystemManager.getVolTable().put(domainName, vol);
            SystemManager.getIpStateTable().put(ip, true);
            ZKClient zkClient = new ZKClient();
            zkClient.createIP4VM(nodeHostname, domainName, ip);
            zkClient.close();
            return vm;
        } catch (LibvirtException e) {
            e.printStackTrace();
            return null;
        }
    }

    public VMInfo getStatistic(String domainName) {
        VM vm = SystemManager.getVmInfoTable().get(domainName);
        Domain domain = SystemManager.getVmTable().get(domainName);
        StorageVol vol = SystemManager.getVolTable().get(domainName);

        try {
            //cpu信息
            long start = System.currentTimeMillis();
            long cpuTime1 = domain.getInfo().cpuTime;
            Thread.sleep(1000);
            long cpuTime2 = domain.getInfo().cpuTime;
            long end = System.currentTimeMillis();
            int nrVirtCpu = domain.getInfo().nrVirtCpu;
            System.out.println(cpuTime2 - cpuTime1);
            System.out.println(end - start);
            double cpuRate = Math.min(1.0, (cpuTime2 - cpuTime1) / ((end - start) * nrVirtCpu * 1e6));
            System.out.println(cpuRate);

            //磁盘信息
            double capacity = vol.getInfo().capacity / SystemManager.B2GB;
            double allocation = vol.getInfo().allocation / SystemManager.B2GB;

            //内存信息
            LinuxConnection linuxConnection = new LinuxConnection(vm.getIp(), SystemManager.LINUX_USER_NAME, SystemManager.LINUX_PASSWORD);
            InputStream inputStream = new ByteArrayInputStream(linuxConnection.execute("cat /proc/meminfo").getBytes());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            double memTotal = Long.parseLong(bufferedReader.readLine().split("\\s+")[1]) / SystemManager.KB2GB;
            bufferedReader.readLine();
            double memAvailable = Long.parseLong(bufferedReader.readLine().split("\\s+")[1]) / SystemManager.KB2GB;
            VMInfo vmInfo = new VMInfo(nrVirtCpu, cpuRate, allocation, capacity, memTotal, memAvailable);
            System.out.println(vmInfo);
            linuxConnection.close();
            return vmInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean closeVM(String domainName) {
        try {
            Domain domain = SystemManager.getVmTable().get(domainName);
            VM vm = SystemManager.getVmInfoTable().get(domainName);
            String hostname = SystemManager.getDnTable().get(vm.getNodeIp());
            domain.destroy();
            vm.setState(0);
            if (!vm.isPersistent()) {
                SystemManager.getVmTable().remove(domainName);
                SystemManager.getVmInfoTable().remove(domainName);
                SystemManager.getVolTable().remove(domainName);
                ZKClient zkClient = new ZKClient();
                zkClient.deleteIP4VM(hostname, vm.getDomainName());
                zkClient.close();
            }
            return true;
        } catch (LibvirtException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteVM(String domainName) {
        try {
            VM vm = SystemManager.getVmInfoTable().get(domainName);
            String hostname = SystemManager.getDnTable().get(vm.getNodeIp());
            Domain domain = SystemManager.getVmTable().get(domainName);
            StorageVol vol = SystemManager.getVolTable().get(domainName);
            if (domain.isActive() == 1)
                domain.destroy();
            domain.undefine();
            vol.wipe();
            vol.delete(0);
            SystemManager.getVmTable().remove(domainName);
            SystemManager.getVmInfoTable().remove(domainName);
            SystemManager.getVolTable().remove(domainName);
            SystemManager.getIpStateTable().put(vm.getIp(), false);
            ZKClient zkClient = new ZKClient();
            zkClient.deleteIP4VM(hostname, vm.getDomainName());
            zkClient.close();
            return true;
        } catch (LibvirtException e) {
            e.printStackTrace();
            return false;
        }
    }

    public VM cloneVM(String srcDomainName, String destDomainName, String ip, String nodeIp) {
        try {
            //获取原domain所在的连接
            VM vm1 = SystemManager.getVmInfoTable().get(srcDomainName);
            Domain domain1 = SystemManager.getVmTable().get(srcDomainName);
            if (domain1.isActive() == 1) {
                closeVM(srcDomainName);
            }
            if (nodeIp == null)
                nodeIp = vm1.getNodeIp();
            Connect connect = SystemManager.getConnectTable().get(nodeIp);
            Connect nfsConnection = SystemManager.getConnectTable().get(SystemManager.NFS_SERVER_IP);
            long mem = vm1.getMemory();
            int vcpu = vm1.getVcpu();
            //获取克隆vol
            StorageVol srcVol = SystemManager.getVolTable().get(srcDomainName);
            long capacity = (long) (srcVol.getInfo().capacity / SystemManager.B2GB);
            //分配,只能本地克隆
            String hostname = SystemManager.getDnTable().get(nodeIp);
            //按时间戳取名
            String destVolName = destDomainName + "_" + System.currentTimeMillis();
            String destVolPath = SystemManager.POOL_PATH + "/" + destVolName;
            //创建vol,注意单位转换
            String volXML = XMLUtils.createVolXML(destVolPath, destVolName, capacity);
            StoragePool nfsPool = nfsConnection.storagePoolLookupByName(SystemManager.POOL);
            //本地克隆
            StorageVol destVol = nfsPool.storageVolCreateXMLFrom(volXML, srcVol, 0);
            String mac = RandomCreator.createMac();
            String domainXML = XMLUtils.createDomainXML(destDomainName, mem, vcpu, destVol.getPath(), mac);
            //远程刷新
            StoragePool destPool = connect.storagePoolLookupByName(SystemManager.POOL);
            destPool.refresh(0);
            //创建domain
            Domain domain = connect.domainDefineXML(domainXML);
            domain.setAutostart(false);
            //修改全集变量
            VM vm2 = new VM(nodeIp, destDomainName, 0, ip, mem, vcpu, capacity, domain.isPersistent() == 1);
            SystemManager.getVmInfoTable().put(destDomainName, vm2);
            SystemManager.getVmTable().put(destDomainName, domain);
            SystemManager.getVolTable().put(destDomainName, destVol);
            SystemManager.getIpStateTable().put(ip, true);
            ZKClient zkClient = new ZKClient();
            zkClient.createIP4VM(hostname, destDomainName, ip);
            zkClient.close();

            openVM(destDomainName);
            Thread.sleep(60000);
            LinuxConnection linuxConnection;
            while (true) {
                try {
                    linuxConnection = new LinuxConnection(vm1.getIp(), SystemManager.LINUX_USER_NAME, SystemManager.LINUX_PASSWORD);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            JSONObject conf = (JSONObject) SystemManager.NET_CONF.clone();
            conf.put("IPADDR", ip);
            StringBuilder stringBuilder = new StringBuilder();
            try {
                stringBuilder.append("rm -f " + SystemManager.HOSTNAME_PATH).append(" && ")
                        .append("touch " + SystemManager.HOSTNAME_PATH).append(" && ").append("echo ")
                        .append(destDomainName).append(">>").append(SystemManager.HOSTNAME_PATH).append(" && ")
                        .append("rm -f " + SystemManager.NET_CONF_PATH).append(" && ")
                        .append("touch " + SystemManager.NET_CONF_PATH).append(" && ");
                for (Object key : conf.keySet()) {
                    String attr = key.toString();
                    String val = conf.get(attr).toString();
                    stringBuilder.append("echo ").append(attr).append("=").append(val).append(">>").append(SystemManager.NET_CONF_PATH).append(" && ");
                }
                stringBuilder.append("reboot");
                linuxConnection.execute(stringBuilder.toString());
                vm2.setState(1);
            } catch (Exception e) {
                e.printStackTrace();
                vm2.setState(-1);
            } finally {
                linuxConnection.close();
            }
            return vm2;
        } catch (LibvirtException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean openVM(String domainName) {
        try {
            VM vm = SystemManager.getVmInfoTable().get(domainName);
            Domain domain = SystemManager.getVmTable().get(domainName);
            domain.create();
            vm.setState(1);
            return true;
        } catch (LibvirtException e) {
            e.printStackTrace();
            return false;
        }
    }


    public VM migrateVM(String srcDomainName, String destDomainName, String nodeIp) {
        String linuxIP = SystemManager.getVmInfoTable().get(srcDomainName).getNodeIp();
        String srcHostname = SystemManager.getDnTable().get(linuxIP);
        String destHostname = SystemManager.getDnTable().get(nodeIp);
        String passwd = SystemManager.getPwdTable().get(srcHostname);
        String usrName = SystemManager.LINUX_USER_NAME;

        LinuxConnection linuxConnection = null;
        try {
            linuxConnection = new LinuxConnection(linuxIP, usrName, passwd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("virsh migrate --live ").append(srcDomainName)
                .append(" --dname ").append(destDomainName)
                .append(" qemu+tcp://").append(nodeIp).append("/system ")
                .append("tcp://").append(nodeIp).append(" --unsafe");
        logger.info(stringBuilder.toString());
        // 执行命令
        try {
            linuxConnection.execute(stringBuilder.toString());
            ZKClient zkClient = new ZKClient();
            VM vm = SystemManager.getVmInfoTable().get(srcDomainName);
            if (!vm.isPersistent()) {
                SystemManager.getVmTable().remove(srcDomainName);
                SystemManager.getVmInfoTable().remove(srcDomainName);
                SystemManager.getVolTable().remove(srcDomainName);
                zkClient.deleteIP4VM(srcHostname, vm.getDomainName());
            }
//            removeVM(srcDomainName);

            VM vm2 = new VM(nodeIp, destDomainName, 1, vm.getIp(), vm.getMemory(), vm.getVcpu(), vm.getCapacity(), false);

            StorageVol vol = SystemManager.getVolTable().get(srcDomainName);
            Connect connect = SystemManager.getConnectTable().get(nodeIp);
            Domain domain2 = connect.domainLookupByName(destDomainName);

            SystemManager.getVmInfoTable().put(destDomainName, vm2);
            SystemManager.getVmTable().put(destDomainName, domain2);
            SystemManager.getVolTable().put(destDomainName, vol);
            zkClient.createIP4VM(destHostname, destDomainName, vm2.getIp());
            zkClient.close();
            return vm2;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            linuxConnection.close();
        }
    }

    public List<String> listIP() {
        List<String> ipList = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : SystemManager.getIpStateTable().entrySet()) {
            if (!entry.getValue()) {
                ipList.add(entry.getKey());
            }
        }
        return ipList;
    }

    public List<String> listNodeIP() {
        return new ArrayList<>(SystemManager.getDnTable().keySet());
    }

}

