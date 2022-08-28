package service;

import entity.Container;
import entity.Image;
import manager.SystemManager;
import utils.DockerUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: ldw
 * @Date: 2022/3/31
 */
public class CTService {
    public List<Container> listCT() {
        SystemManager.refreshDocker();
        List<Container> ctList = new LinkedList<>();
        for (Map.Entry<String, Map<String, Container>> mapEntry : SystemManager.getCtTable().entrySet()) {
            for (Map.Entry<String, Container> entry : mapEntry.getValue().entrySet()) {
                ctList.add(entry.getValue());
            }
        }
        return ctList;
    }

    public Container createCT(String nodeIp, String name,String imageName) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.run(name, imageName);
            Container container = docker.getCTByName(name);
            SystemManager.getCtTable().get(nodeIp).put(name, container);
            return container;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean closeCT(String nodeIp, String name) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.stop(name);
            Container container = SystemManager.getCtTable().get(nodeIp).get(name);
            container.setRunning(false);
            container.setIp("");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCT(String nodeIp, String name) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.rm(name);
            SystemManager.getCtTable().remove(name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Image commitCT(String nodeIp, String name, String author, String describe, String repository, String tag) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.commit(name, author, describe, repository, tag);
            SystemManager.refreshDocker();
            return SystemManager.getImageTable().get(nodeIp).get(repository + ":" + tag);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Container openCT(String nodeIp, String name) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.start(name);
            Container container = SystemManager.getCtTable().get(nodeIp).get(name);
            container.setIp(docker.getIP(name));
            container.setRunning(true);
            return container;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
