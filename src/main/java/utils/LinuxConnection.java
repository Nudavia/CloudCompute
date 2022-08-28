package utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.lang.StringUtils;

import java.io.*;

/**
 * @Auther: ldw
 * @Date: 2022/3/25
 */
public class LinuxConnection {
    private final Connection connection;

    public LinuxConnection(String ip, String userName, String userPwd) throws IOException {
        System.out.println(ip);
        connection = new Connection(ip);
        connection.connect();
        connection.authenticateWithPassword(userName, userPwd);
    }

    public String execute(String cmd) throws Exception {
        System.out.println(cmd);
        String result;
        Session session = connection.openSession();
        session.execCommand(cmd);
        // 字符编码默认是utf-8
        String encoding = "UTF-8";
        result = processStdout(session.getStdout(), encoding);
        // 如果为输出为空，说明脚本执行出错了
        if (StringUtils.isBlank(result)) {
            result = processStdout(session.getStderr(), encoding);
        }
        return result;
    }


    private String processStdout(InputStream in, String charset) throws Exception {
        InputStream stdout = new StreamGobbler(in);
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(stdout, charset);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (UnsupportedEncodingException e) {
            throw new Exception("不支持的编码字符集异常", e);
        } catch (IOException e) {
            throw new Exception("读取指纹失败", e);
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedReader != null)
                inputStreamReader.close();
            if (bufferedReader != null)
                stdout.close();
        }
        return stringBuilder.toString();
    }

    public void close() {
        connection.close();
    }
}