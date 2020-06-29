//package io;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author txb
 * @date 2020/6/29 10:52
 * 从https://geo.datav.aliyun.com爬取中国三级地图json
 */
public class fetchMapJson {

    public static final String URL = "jdbc:mysql://192.168.8.144:3306/TODO";
    public static final String USER = "TODO";
    public static final String PASSWORD = "TODO";

    private static final String ENCODING="utf-8";
    private static final String TARGET_URL_PREFIX="https://geo.datav.aliyun.com/areas_v2/bound/";
    private static final String TARGET_URL_SUFFIX="_full.json";

    private static final String OUTPUT_FILE_PATH="TODO";
    private static final String OUTPUT_FILE_NAME_SUFFIX="_full.json";

    public static byte[] read(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int length = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((length = in.read(buf, 0, buf.length)) > 0) {
            bout.write(buf, 0, length);
        }
        bout.flush();
        return bout.toByteArray();
    }


    public static void main(String[] args) throws Exception {

        //1.加载驱动程序
        Class.forName("com.mysql.jdbc.Driver");
        //2. 获得数据库连接
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        //3.操作数据库，实现增删改查
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM areainfo");

        InputStream in = null;
        BufferedWriter bw = null;

        try {
            //如果有数据，rs.next()返回true
            while (rs.next()) {
                /**
                 * 获取市及以上城市代码
                 */
                String areaCode = rs.getString("areaId");
                if ("00".equals(areaCode.substring(4, 6))) {

                    String targetUrl = TARGET_URL_PREFIX + areaCode + TARGET_URL_SUFFIX;
                    System.out.println("请求url地址：" + targetUrl);

                    URL url = new URL(targetUrl);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setConnectTimeout(10 * 1000);
                    httpConn.setReadTimeout(20 * 1000);
                    httpConn.setRequestProperty("Content-type", "application/json;charset=" + ENCODING);

   


                    StringBuilder sb = new StringBuilder(1024);

                    if (200 == httpConn.getResponseCode()) {
                        in = httpConn.getInputStream();
                        sb.append(new String(read(in), ENCODING));
                        String response = sb.toString();
                        bw = new BufferedWriter(new FileWriter(new File(OUTPUT_FILE_PATH + areaCode + OUTPUT_FILE_NAME_SUFFIX)));
    //                        byte[] bytes=new byte[1024];
    //                        while (int len=re)
                        bw.write(response, 0, response.length());
                        bw.flush();
                    } else {
                        in = httpConn.getErrorStream();
                        throw new Exception("请求出错:" + httpConn.getResponseCode());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bw.close();
            in.close();
        }

    }
}
