package controller;

import entity.VM;
import entity.VMInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import service.VMService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Auther: ldw
 * @Date: 2022/3/18
 */

public class VMServlet extends HttpServlet {
    private final VMService service = new VMService();
    private final Logger logger = Logger.getLogger("VMServlet");

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
                List<VM> vmList = service.listVM();
                vmList.sort(Comparator.comparing(VM::getDomainName));
                String jsonArray = JSONArray.fromObject(vmList).toString();
                jsonObject.put("vmList", jsonArray);
                success = true;
                break;
            case "create":
                String domainName = String.valueOf(parameter.get("domainName"));
                String ip = String.valueOf(parameter.get("ip"));
                Object para = parameter.get("nodeIp");
                String nodeIp = para == null ? null : String.valueOf(para);
                long capacity = Long.parseLong(String.valueOf(parameter.get("capacity")));
                long memory = Long.parseLong(String.valueOf(parameter.get("memory")));
                int vcpu = Integer.parseInt(String.valueOf(parameter.get("cpu")));
                VM vm = service.createVM(domainName, capacity, memory, vcpu, ip, nodeIp);
                success = vm != null;
                jsonObject.put("vm", vm);
                break;
            case "open":
                domainName = String.valueOf(parameter.get("domainName"));
                success = service.openVM(domainName);
                break;
            case "close":
                domainName = String.valueOf(parameter.get("domainName"));
                success = service.closeVM(domainName);
                break;
            case "delete":
                domainName = String.valueOf(parameter.get("domainName"));
                success = service.deleteVM(domainName);
                break;
            case "clone":
                String srcDomainName = String.valueOf(parameter.get("srcDomainName"));
                String destDomainName = String.valueOf(parameter.get("destDomainName"));
                para = parameter.get("nodeIp");
                nodeIp = para == null ? null : String.valueOf(para);
                ip = String.valueOf(parameter.get("ip"));
                vm = service.cloneVM(srcDomainName, destDomainName, ip, nodeIp);
                success = vm != null;
                jsonObject.put("vm", vm);
                break;
            case "migrate":
                nodeIp = String.valueOf(parameter.get("nodeIp"));
                srcDomainName = String.valueOf(parameter.get("srcDomainName"));
                destDomainName = String.valueOf(parameter.get("destDomainName"));
                vm = service.migrateVM(srcDomainName, destDomainName, nodeIp);
                success = vm != null;
                jsonObject.put("vm", vm);
                break;
            case "ips":
                List<String> ipList = service.listIP();
                jsonArray = JSONArray.fromObject(ipList).toString();
                jsonObject.put("ips", jsonArray);
                success = true;
                break;
            case "nodeIps":
                ipList = service.listNodeIP();
                jsonArray = JSONArray.fromObject(ipList).toString();
                jsonObject.put("nodeIps", jsonArray);
                success = true;
                break;
            case "info":
                domainName = String.valueOf(parameter.get("domainName"));
                VMInfo vmInfo = service.getStatistic(domainName);
                jsonObject.put("vmInfo", vmInfo);
                success = true;
                break;
        }
        jsonObject.put("success", success);
        writer.append(jsonObject.toString());
        logger.info(jsonObject.toString());
    }
}

