package controller;

import entity.Container;
import entity.Image;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import service.CTService;

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
public class CTServlet extends HttpServlet {
    private final CTService service = new CTService();
    private final Logger logger = Logger.getLogger("CTServlet");

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
                List<Container> vmList = service.listCT();
                String jsonArray = JSONArray.fromObject(vmList).toString();
                jsonObject.put("ctList", jsonArray);
                success = true;
                break;
            case "create":
                String name = String.valueOf(parameter.get("name"));
                String imageName = String.valueOf(parameter.get("imageName"));
                String nodeIp = String.valueOf(parameter.get("nodeIp"));
                Container ct = service.createCT(nodeIp, name, imageName);
                success = ct != null;
                jsonObject.put("ct", ct);
                break;
            case "open":
                name = String.valueOf(parameter.get("name"));
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                ct = service.openCT(nodeIp, name);
                success = ct != null;
                jsonObject.put("ct", ct);
                break;
            case "close":
                name = String.valueOf(parameter.get("name"));
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                success = service.closeCT(nodeIp, name);
                break;
            case "delete":
                name = String.valueOf(parameter.get("name"));
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                success = service.deleteCT(nodeIp, name);
                break;
            case "commit":
                name = String.valueOf(parameter.get("name"));
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                String author = String.valueOf(parameter.get("author"));
                String describe = String.valueOf(parameter.get("describe"));
                String repository = String.valueOf(parameter.get("repository"));
                String tag = String.valueOf(parameter.get("tag"));
                Image image = service.commitCT(nodeIp, name, author, describe, repository, tag);
                success = image != null;
                jsonObject.put("image", image);
                break;
        }
        jsonObject.put("success", success);
        writer.append(jsonObject.toString());
        logger.info(jsonObject.toString());
    }
}
