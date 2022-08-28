package controller;

import entity.Image;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import service.ImageService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Auther: ldw
 * @Date: 2022/3/31
 */
public class ImageServlet extends HttpServlet {
    private final ImageService service = new ImageService();
    private final Logger logger = Logger.getLogger("ImageServlet");

    public static JSONObject receivePost(HttpServletRequest request) throws IOException {

        // 读取请求内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        //将json字符串转换为json对象
        return JSONObject.fromObject(stringBuilder.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");

        JSONObject parameter = receivePost(req);
        String type = String.valueOf(parameter.get("type"));
        System.out.println(type);
        PrintWriter writer = resp.getWriter();
        boolean success = false;
        JSONObject jsonObject = new JSONObject();
        switch (type) {
            case "list":
                List<Image> vmList = service.listImage();
                String jsonArray = JSONArray.fromObject(vmList).toString();
                jsonObject.put("imageList", jsonArray);
                success = true;
                break;
            case "pull":
                String name = String.valueOf(parameter.get("name"));
                String nodeIp = String.valueOf(parameter.get("nodeIp"));
                Image image = service.pullImage(nodeIp, name);
                success = image != null;
                jsonObject.put("image", image);
                break;
            case "delete":
                name = String.valueOf(parameter.get("name"));
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                success = service.deleteImage(nodeIp, name);
                break;
            case "push":
                name = String.valueOf(parameter.get("name"));
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                success = service.pushImage(nodeIp, name);
                break;
        }
        jsonObject.put("success", success);
        writer.append(jsonObject.toString());
        logger.info(jsonObject.toString());
    }
}