package utils;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: ldw
 * @Date: 2022/3/20
 */
public class ZKClient {
    private ZooKeeper zooKeeper;
    private final static String IP_PARENT = "/ip";
    private final static String PSW_PARENT = "/psw";
    private final static String connectionString = "192.168.43.130:2181,192.168.43.128:2181";

    public ZKClient() {
        try {
            zooKeeper = new ZooKeeper(connectionString, 15000, watchedEvent -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIP4PM(String hostname) {
        try {
            String path = IP_PARENT + "/" + hostname;
            return new String(zooKeeper.getData(path, true, new Stat()));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getIP4VM(String hostname, String domainName) {
        try {
            String path = IP_PARENT + "/" + hostname + "/" + domainName;
            return new String(zooKeeper.getData(path, true, new Stat()));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> listIP4PM() {
        Map<String, String> ipTable = new HashMap<>();
        try {
            List<String> children = zooKeeper.getChildren(IP_PARENT, true);
            for (String child : children) {
                String ip = (new String(zooKeeper.getData(IP_PARENT + "/" + child, true, new Stat())));
                ipTable.put(child, ip);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return ipTable;
    }

    public Map<String, String> listPSW4PM() {
        Map<String, String> pwdTable = new HashMap<>();
        try {
            List<String> children = zooKeeper.getChildren(PSW_PARENT, true);
            for (String child : children) {
                String pwd = (new String(zooKeeper.getData(PSW_PARENT + "/" + child, true, new Stat())));
                pwdTable.put(child, pwd);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return pwdTable;
    }


    public Map<String, String> listIP4VM() {
        Map<String, String> ipTable = new HashMap<>();
        try {
            List<String> children = zooKeeper.getChildren(IP_PARENT, true);
            for (String child : children) {
                String pmPath = IP_PARENT + "/" + child;
                List<String> children2 = zooKeeper.getChildren(pmPath, true);
                for (String child2 : children2) {
                    String vmPath = pmPath + "/" + child2;
                    String ip = (new String(zooKeeper.getData(vmPath, true, new Stat())));
                    ipTable.put(child2, ip);
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return ipTable;
    }


    public void createIP4PM(String hostname, String ip) {
        try {
            String path = IP_PARENT + "/" + hostname;
            zooKeeper.create(path, ip.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createIP4VM(String hostname, String domainName, String ip) {
        try {
            String path = IP_PARENT + "/" + hostname + "/" + domainName;
            zooKeeper.create(path, ip.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteIP4PM(String hostname) {
        try {
            String path = IP_PARENT + "/" + hostname;
            List<String> children = zooKeeper.getChildren(path, true);
            for (String child : children) {
                zooKeeper.delete(path + "/" + child, -1);
            }
            zooKeeper.delete(path, -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteIP4VM(String hostname, String domainName) {
        try {
            String path = IP_PARENT + "/" + hostname + "/" + domainName;
            zooKeeper.delete(path, -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean existsIP(String path) {
        try {
            Stat exists = zooKeeper.exists(path, true);
            return exists != null;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
