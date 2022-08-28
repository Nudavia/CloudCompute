package service;

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
public class ImageService {

    public List<Image> listImage() {
        SystemManager.refreshDocker();
        List<Image> imageList = new LinkedList<>();
        for (Map.Entry<String, Map<String, Image>> mapEntry : SystemManager.getImageTable().entrySet()) {
            for (Map.Entry<String, Image> entry : mapEntry.getValue().entrySet()) {
                imageList.add(entry.getValue());
            }
        }
        return imageList;
    }

    public boolean deleteImage(String nodeIp, String name) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.rmi(name);
            SystemManager.getImageTable().remove(name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Image pullImage(String nodeIp, String name) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            if (!SystemManager.getImageTable().get(nodeIp).containsKey(name))
                docker.pull(name);
            SystemManager.refreshDocker();
            return SystemManager.getImageTable().get(nodeIp).get(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean pushImage(String nodeIp, String name) {
        DockerUtils docker = new DockerUtils(nodeIp);
        try {
            docker.push(name, SystemManager.REGISTRY_IP, SystemManager.REGISTRY_PORT);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
