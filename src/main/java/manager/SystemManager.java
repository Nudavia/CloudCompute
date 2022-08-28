package manager;

import entity.Container;
import entity.Image;
import entity.VM;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.libvirt.*;
import utils.DockerUtils;
import utils.XMLUtils;
import utils.ZKClient;

import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @Auther: ldw
 * @Date: 2022/3/14
 */
public class SystemManager {

    private static final Map<String, Domain> vmTable;//name-domain
    private static final Map<String, VM> vmInfoTable;//name-vm
    private static final Map<String, Connect> connectTable;//ip-connect
    private static final Map<String, Boolean> ipStateTable;//ip-bool
    private static final Map<String, String> dnTable;//nodeIp-dn
    private static final Map<String, StorageVol> volTable;//name-vol
    private static final Map<String, String> pwdTable;//name-vol
    private static final Map<String, Map<String, Container>> ctTable;//name-container
    private static final Map<String, Map<String, Image>> imageTable;//p:v-image
    public static final double B2GB = 1 << 30;
    public static final double KB2GB = 1 << 20;
    public static final String POOL = "pool01";
    public static final String POOL_PATH = "/opt/kvm/images";
    public static final String LINUX_USER_NAME = "root";
    public static final String LINUX_PASSWORD = "ldw20000702";
    public static final String NET_CONF_PATH = "/etc/sysconfig/network-scripts/ifcfg-ens3";
    public static final String HOSTNAME_PATH = "/etc/hostname";
    public static final String NFS_SERVER_IP = "192.168.43.130";
    public static final String REGISTRY_IP = "192.168.43.130";
    public static final int REGISTRY_PORT = 5000;
    public static JSONObject NET_CONF;
    public static final DecimalFormat round2 = new DecimalFormat("0.00");
    public static final DecimalFormat round4 = new DecimalFormat("0.0000");
//    public static Connect NFS_SERVER = null;

    static {
        round2.setRoundingMode(RoundingMode.HALF_UP);
        round4.setRoundingMode(RoundingMode.HALF_UP);
        vmTable = new HashMap<>();
        vmInfoTable = new HashMap<>();
        connectTable = new HashMap<>();
        ipStateTable = new TreeMap<>();
        dnTable = new TreeMap<>();
        volTable = new HashMap<>();
        pwdTable = new HashMap<>();
        ctTable = new HashMap<>();
        imageTable = new HashMap<>();
        try {
            //解析JSON文件的内容
            JSONParser jsonParser = new JSONParser();
            String path = Objects.requireNonNull(SystemManager.class.getClassLoader().getResource("ifcfg-ens3.json")).getPath();
            NET_CONF = (JSONObject) jsonParser.parse(new FileReader(path));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        refreshVM();
        refreshDocker();
    }

    public static void refreshVM() {
        vmTable.clear();
        vmInfoTable.clear();
        connectTable.clear();
        ipStateTable.clear();
        dnTable.clear();
        volTable.clear();
        pwdTable.clear();
        try {
            for (int i = 128; i < 255; i++) {
                ipStateTable.put("192.168.43." + i, false);
            }
            ZKClient zkClient = new ZKClient();
            Map<String, String> ip4PM = zkClient.listIP4PM();
            pwdTable.putAll(zkClient.listPSW4PM());
            for (Map.Entry<String, String> entry : ip4PM.entrySet()) {
                String hostname = entry.getKey();
                String nodeIp = entry.getValue();
                String uri = "qemu+tcp://" + nodeIp + ":16509/system";
                Connect connect;
                try {
                    connect = new Connect(uri);
                } catch (Exception e) {
                    continue;
                }
                connectTable.put(nodeIp, connect);
                dnTable.put(nodeIp, hostname);
                ipStateTable.put(nodeIp, true);

                int[] ids = connect.listDomains();
                String[] names = connect.listDefinedDomains();
                List<String> nameList = new LinkedList<>();
                for (int id : ids) {
                    String name = connect.domainLookupByID(id).getName();
                    nameList.add(name);
                }
                nameList.addAll(Arrays.asList(names));

                for (String domainName : nameList) {
                    Domain domain = connect.domainLookupByName(domainName);
                    long memory = (long) (domain.getInfo().memory / SystemManager.KB2GB);
                    int vcpu = domain.getInfo().nrVirtCpu;
                    int state = domain.isActive();
                    String ip = zkClient.getIP4VM(hostname, domainName);
                    String volPath = XMLUtils.getVolPath(domain.getXMLDesc(0));
                    StoragePool pool = connect.storagePoolLookupByName(SystemManager.POOL);
                    pool.refresh(0);
                    StorageVol vol = connect.storageVolLookupByPath(volPath);
                    long capacity = (long) (vol.getInfo().capacity / SystemManager.B2GB);
                    VM vm = new VM(nodeIp, domainName, state, ip, memory, vcpu, capacity, domain.isPersistent() == 1);
                    vmTable.put(domainName, domain);
                    vmInfoTable.put(domainName, vm);
                    volTable.put(domainName, vol);
                    ipStateTable.put(ip, true);
                }
            }
            zkClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void refreshDocker() {
        imageTable.clear();
        ctTable.clear();
        dnTable.clear();
        pwdTable.clear();
        try {
            ZKClient zkClient = new ZKClient();
            pwdTable.putAll(zkClient.listPSW4PM());
            Map<String, String> ip4PM = zkClient.listIP4PM();
            for (Map.Entry<String, String> entry : ip4PM.entrySet()) {
                String hostname = entry.getKey();
                String nodeIp = entry.getValue();
                dnTable.put(nodeIp, hostname);
                DockerUtils dockerUtils = new DockerUtils(nodeIp);
                imageTable.put(nodeIp, new HashMap<>());
                ctTable.put(nodeIp, new HashMap<>());
                for (Image image : dockerUtils.listImages()) {
                    imageTable.get(nodeIp).put(image.getName(), image);
                }
                for (Container container : dockerUtils.listContainer()) {
                    ctTable.get(nodeIp).put(container.getName(), container);
                }
            }
            zkClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String chooseNodeIP() {
        String ip = null;
        double bestScore = 0;
        try {
            for (Map.Entry<String, Connect> entry : connectTable.entrySet()) {

                Connect connect = entry.getValue();
                StoragePool pool = connect.storagePoolLookupByName(SystemManager.POOL);
                if (pool.getInfo().state != StoragePoolInfo.StoragePoolState.VIR_STORAGE_POOL_RUNNING)
                    continue;
                long freeMem = connect.getFreeMemory();
                int maxVcpu = connect.getMaxVcpus("kvm");
                long curVcpu = 0;
                long curMem = 0;

                int[] ids = connect.listDomains();
                for (int id : ids) {
                    Domain domain = connect.domainLookupByID(id);
                    if (domain.isActive() == 1) {
                        curVcpu += domain.getInfo().nrVirtCpu;
                        curMem += domain.getInfo().memory;
                    }
                }
                double curScore = freeMem + maxVcpu - curMem - curVcpu;
                if (curScore > bestScore) {
                    bestScore = curScore;
                    ip = entry.getKey();
                }
            }
        } catch (LibvirtException e) {
            e.printStackTrace();
        }
        return ip;
    }

//    public static String chooseIP() {
//        for (Map.Entry<String, Boolean> entry : ipStateTable.entrySet()) {
//            if (!entry.getValue())
//                return entry.getKey();
//        }
//        return null;
//    }

    public static Map<String, Domain> getVmTable() {
        return vmTable;
    }

    public static Map<String, VM> getVmInfoTable() {
        return vmInfoTable;
    }

    public static Map<String, Connect> getConnectTable() {
        return connectTable;
    }

    public static Map<String, Boolean> getIpStateTable() {
        return ipStateTable;
    }

    public static Map<String, StorageVol> getVolTable() {
        return volTable;
    }

    public static Map<String, String> getDnTable() {
        return dnTable;
    }

    public static Map<String, String> getPwdTable() {

        return pwdTable;
    }

    public static Map<String, Map<String, Container>> getCtTable() {
        return ctTable;
    }

    public static Map<String, Map<String, Image>> getImageTable() {
        return imageTable;
    }
}

