package com.zjtelcom.cpct.util;

import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

/**
 * @Auther: anson
 * @Date: 2018/9/30
 * @Description:文件读取，写入工具类
 */
public class FileUtil {


    /**
     * 判断文件是否存在 并写入内容到该文件
     * @param path
     * @param content
     * @throws IOException
     */
    public void writeToFile(String path,String content) throws IOException {
        boolean tip = createFile(path);
        if(!tip){
            throw new RuntimeException("生成指定目录"+path+"文件失败!");
        }
        writeFileContent(path,content);
    }


    /**
     * 创建文件
     * @param filenameTemp  文件路径+名称
     * @return  是否创建成功，成功则返回true
     */
    public static boolean createFile(String filenameTemp){
        Boolean bool = true;
        File file = new File(filenameTemp);
        try {
            //如果文件不存在，则创建新的文件
            if(!file.exists()){
                System.out.println("文件不存在！");
                file.createNewFile();
                System.out.println("文件创建成功"+filenameTemp);
            }else{
                System.out.println("文件存在："+filenameTemp);
            }
        } catch (Exception e) {
            bool=false;
            e.printStackTrace();
        }
        return bool;
    }



    /**
     * 向文件中写入内容  每次写入 转行追加数据
     * @param filepath 文件路径与名称
     * @param str  写入的内容
     * @return
     */
    public static boolean writeFileContent(String filepath,String str){
        boolean tip=false;
        FileWriter writer=null;
        try {
            writer = new FileWriter(filepath, true);
            writer.write(str);
            tip=true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return tip;
    }



    /**
     *  以字节为单位读取文件，通常用于读取二进制文件，如图片
     * @param path
     * @param code 编码
     * @return
     */
    public static String readByBytes(String path,String code) {
        String content = null;

        try {
            InputStream inputStream = new FileInputStream(path);
            StringBuffer sb = new StringBuffer();
            int c = 0;
            byte[] bytes = new byte[1024];
            /*
             * InputStream.read(byte[] b)
             *
             * Reads some number of bytes from the input stream and stores them into the buffer array b. 从输入流中读取一些字节存入缓冲数组b中
             * The number of bytes actually read is returned as an integer.  返回实际读到的字节数
             * This method blocks until input data is available, end of file is detected, or an exception is thrown.
             * 该方法会一直阻塞，直到输入数据可以得到、或检测到文件结束、或抛出异常  -- 意思是得到数据就返回
             */
            while ((c = inputStream.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, c, code));
            }

            content = sb.toString();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     *  以行为单位读取文件，常用于读取面向行的格式化文件
     * @param path
     * @param code 编码
     * @return
     */
    public static String readByLines(String path,String code) {
        String content = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), code));

            StringBuffer sb = new StringBuffer();
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp);
            }

            content = sb.toString();
            bufferedReader.close();
        } catch (UnsupportedEncodingException  e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     *  以字符为单位读取文件，常用于读取文本文件
     * @param path
     * @param code 编码
     * @return
     */
    public static String readByChars(String path,String code) {
        String content = null;
        try {
            Reader reader = new InputStreamReader(new FileInputStream(path), code);
            StringBuffer sb = new StringBuffer();
            char[] tempchars = new char[1024];
            while (reader.read(tempchars) != -1) {
                sb.append(tempchars);
            }

            content = sb.toString();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }


    //base64字符串转化成图片
    public static boolean GenerateImage(String imgStr, String filePath, String fileName)
    {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try
        {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for(int i=0;i<b.length;++i)
            {
                if(b[i]<0)
                {//调整异常数据
                    b[i]+=256;
                }
            }
            //生成jpeg图片
            File dir = new File(filePath);
            if (!dir.exists()) {//判断文件目录是否存在
                dir.mkdirs();
                System.out.println("创建文件夹");
            }

            String imgFilePath = filePath + File.separator + fileName;//新生成的图片
            System.out.println(imgFilePath);
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    /**
     * 本地文件（图片、excel等）转换成Base64字符串
     *
     * @param imgPath
     */

    public static String convertFileToBase64(String imgPath) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imgPath);
            System.out.println("文件大小（字节）="+in.available());
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组进行Base64编码，得到Base64编码的字符串
        BASE64Encoder encoder = new BASE64Encoder();
        String base64Str = encoder.encode(data);
        return base64Str;
    }
}
