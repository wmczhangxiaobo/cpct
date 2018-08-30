package com.zjtelcom.cpct.elastic.controller;

import com.alibaba.fastjson.JSONObject;
import com.zjtelcom.cpct.elastic.util.EsSearchUtil;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@EnableAutoConfiguration
@RequestMapping("/esFile")
public class EsFileController {

    private static final Logger logger = LoggerFactory.getLogger(EsFileController.class);
    @Autowired
    private TransportClient client;
    /**
     * 测试索引
     */
    private String indexName="test_index";

    /**
     * 类型
     */
    private String esType="external";

    /**
     * 本机id
     */
    private String localIp="192.168.0.124";

    /**
     * 本机id
     */
    private String filePass="d://testDemo.json";


    /**
     * 从json 文件导入测试
     * @throws IOException
     */
    @RequestMapping("/testImporter")
    public void testImporter()throws IOException {
        long begin = System.currentTimeMillis();
        File article = new File(filePass);
        FileReader fr=new FileReader(article);
        BufferedReader bfr=new BufferedReader(fr);
        String line=null;
        BulkRequestBuilder bulkRequest=client.prepareBulk();
        int count=0;
        while((line=bfr.readLine())!=null){
            JSONObject object = JSONObject.parseObject(line);
            bulkRequest.add(client.prepareIndex(indexName,esType).setSource(object));
            if (count%10==0) {
                bulkRequest.execute().actionGet();
            }
            count++;
            //System.out.println(line);
        }
        bulkRequest.execute().actionGet();
        bfr.close();
        fr.close();
        long cost = System.currentTimeMillis() - begin;
        logger.info("import end. cost:[{}ms]", cost);
        logger.info("***************总条数**************：");
    }

    /**
     * 生成.json格式文件
     */
    @RequestMapping("/createJsonFile")
    public static boolean createJsonFile(String fileName) {
        // 标记文件生成是否成功
        boolean flag = true;
     List<JSONObject> jsonObjects = new ArrayList<>();
     long begin = System.currentTimeMillis();
     for (int i=0;i<10000 ;i++){
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("id",System.currentTimeMillis()+ EsSearchUtil.getRandomStr(15));
         jsonObject.put("age", 25);
         jsonObject.put("name", "j-" + new Random(100).nextInt());
         jsonObject.put("date", new Date());
         jsonObject.put("zhangsanfeng","张三丰");
         for (int j = 0;j<700 ;j++){
             jsonObject.put("TEST"+j,j+1);
         }
         jsonObjects.add(jsonObject);
     }
        // 拼接文件完整路径
        String fullPath = "d://testDemo.json";

        // 生成json格式文件
        try {
            // 保证创建一个新文件
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { // 如果已存在,删除旧文件
                file.delete();
            }
            file.createNewFile();
            // 格式化json字符串
            List<String> jsonStringList = new ArrayList<>();
            for (JSONObject jsonString : jsonObjects){
                String json = JSONObject.toJSONString(jsonString);
                jsonStringList.add(json);
            }
            FileWriter  out=new FileWriter (file);
            BufferedWriter bw= new BufferedWriter(out);
            for (String jsonL : jsonStringList){
                bw.write(jsonL);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        long cost = System.currentTimeMillis() - begin;
        logger.info("import end. cost:[{}ms]", cost);
        // 返回是否成功的标记
        return flag;
    }

}