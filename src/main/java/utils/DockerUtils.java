package utils;

import entity.Container;
import entity.Image;
import manager.SystemManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: ldw
 * @Date: 2022/3/31
 */
public class DockerUtils {
    private LinuxConnection linuxConnection;
    private final String ip;

    public DockerUtils(String ip) {
        String hostname = SystemManager.getDnTable().get(ip);
        String passwd = SystemManager.getPwdTable().get(hostname);
        String usrName = SystemManager.LINUX_USER_NAME;
        this.ip = ip;
        try {
            linuxConnection = new LinuxConnection(ip, usrName, passwd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Image> listImages() throws Exception {
        String results = linuxConnection.execute("docker images");
        InputStream inputStream = new ByteArrayInputStream(results.getBytes());
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        bufferedReader.readLine();
        String line;
        List<Image> imageList = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            String[] split = line.split("\\s+");
            String repository = split[0];
            String tag = split[1];
            String id = split[2];
            String size = split[split.length - 1];
            Image image = new Image(id, repository + ":" + tag, this.ip, size);
            imageList.add(image);
        }
        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
        return imageList;
    }

    public List<Container> listContainer() throws Exception {
        String results = linuxConnection.execute("docker ps -a");
        InputStream inputStream = new ByteArrayInputStream(results.getBytes());
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        bufferedReader.readLine();
        String line;
        List<Container> containerList = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            String[] split = line.split("\\s+");
            String id = split[0];
            String image = split[1];
            boolean running = isRunning(id);
            String ip = getIP(id);
            String name = split[split.length - 1];
            Container container = new Container(id, image, running, ip, this.ip, name);
            containerList.add(container);
        }
        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
        return containerList;
    }

    public void run(String name, String imageName) throws Exception {
        String commend = " docker run -d " + " --name " + name +
                " --network share " +
                " --privileged=true " + imageName +
                " /usr/sbin/init ";
        linuxConnection.execute(commend);
    }

    public void rmi(String name) throws Exception {
        linuxConnection.execute("docker rmi -f " + name);
    }

    public void rm(String name) throws Exception {
        stop(name);
        linuxConnection.execute("docker rm -f " + name);
    }

    public void commit(String name, String author, String describe, String repository, String tag) throws Exception {
        String commend = " docker commit " + " -a " + author + " -m " +
                describe + " " + name + " " + repository + ":" + tag;
        linuxConnection.execute(commend);
    }

    public void start(String name) throws Exception {
        linuxConnection.execute("docker start " + name);
    }

    public void stop(String name) throws Exception {
        linuxConnection.execute("docker stop " + name);
    }

    public void pull(String repository) throws Exception {
        linuxConnection.execute("docker pull " + repository);
    }

    public void pull(String repository, String tag) throws Exception {
        linuxConnection.execute("docker stop " + repository + ":" + tag);
    }


    public void push(String name, String ip, int port) throws Exception {
        String newName = ip + ":" + port + "/" + name;
        String commend = "docker tag " + name + " " + newName;
        linuxConnection.execute(commend);
        commend = "docker push " + newName;
        linuxConnection.execute(commend);
//            commend = "docker rmi -f " + newRepository + ":" + tag;
//            linuxConnection.execute(commend);
    }

    public String getIP(String name) throws Exception {
        String result = linuxConnection.execute("docker inspect " + name + " |grep IPAddress|tail -1");
        String[] s = result.trim().split("[:\",\\s+]");
        String ip = s[s.length - 1];
        return ip.equals("IPAddress") ? "" : ip;
    }

    public boolean isRunning(String name) throws Exception {
        String result = linuxConnection.execute("docker inspect " + name + " |grep Status");
        String[] s = result.trim().split("[:\",\\s+]");
        return s[s.length - 1].equals("running");
    }

    public Container getCTByName(String name) throws Exception {
        String result = linuxConnection.execute("docker ps -a |grep " + name);
        String[] split = result.split("\\s+");
        String id = split[0];
        String image = split[1];
        boolean running = isRunning(id);
        String ip = getIP(id);
        String name2 = split[split.length - 1];
        return name.equals(name2) ? new Container(id, image, running, ip, this.ip, name) : null;
    }
}
