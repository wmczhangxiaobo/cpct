FROM centosjava8:1.7 


COPY target/cpct-web.jar /app/cpct-web.jar
COPY target/classes/dubbo/unregister.sh /app/unregister.sh
COPY target/classes/file/templete.xlsx /app/templete.xlsx
COPY target/classes/file/trialOperationTemplate.xlsx /app/trialOperationTemplate.xlsx
COPY target/classes/file/offerTemplate.xlsx /app/offerTemplate.xlsx
COPY target/classes/file/blacklist_template.xlsx /app/blacklist_template.xlsx
COPY target/classes/file/servicePackageTemplate.xlsx /app/servicePackageTemplate.xlsx
RUN cd /app && mkdir project && mkdir logs && mv /app/cpct-web.jar /app/project && mv /app/unregister.sh /app/project && chmod 777 -R /app  && chown -R isale:isale /app

WORKDIR /app
ENV LANG en_US.UTF-8
ENV TZ Asia/Shanghai

#VOLUME /app/dubbo/logs
USER isale 
ENTRYPOINT ["nohup","java", "-Xmx7000m","-Xms4000m","-Xmn256m","-XX:PermSize=128m","-Xss256k","-Dcom.sun.management.jmxremote","-Dcom.sun.management.jmxremote.port=12345","-Dcom.sun.management.jmxremote.ssl=false","-Dcom.sun.management.jmxremote.authenticate=false", "-jar","/app/project/cpct-web.jar","&"]
