package cpct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
//@ImportResource("classpath:dubbo/dubbo-provider.xml")  //加载xml文件  pst环境
@ImportResource("classpath:dubbo/dubbo-test.xml")  //加载xml文件  dev环境
public class DubboServerApplication {

    public static Logger logger = LoggerFactory.getLogger(DubboServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DubboServerApplication.class, args);
        logger.info("***********CPCT*****DUBBO****启动**********");
    }
}