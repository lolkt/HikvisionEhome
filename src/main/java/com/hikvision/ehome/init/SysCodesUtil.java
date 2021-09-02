package com.hikvision.ehome.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;


/**
 * 初始化缓存数据code码
 *
 * @author maq
 */
@Component
public class SysCodesUtil implements InitializingBean {

    private Logger log = LoggerFactory.getLogger(SysCodesUtil.class);
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        initSysCode();
    }

    public void initSysCode() {
        Terms.ip = getIpAddress();
    }


    private String getIpAddress() {
        InetUtilsProperties properties = new InetUtilsProperties();
        properties.setIgnoredInterfaces(Arrays.asList("docker0", "veth.*", ".*VMware.*", ".*VirtualBox.*"));
        InetUtils inetUtils = new InetUtils(properties);
        String ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        log.info("ip=={}", ip);
        return ip;
    }

    /**
     * 针对多网卡、容器内部署等情况，可借助 "spring-cloud-commons" 提供的 "InetUtils" 组件灵活定制注册IP；
     *
     *      1、引入依赖：
     *          <dependency>
     *             <groupId>org.springframework.cloud</groupId>
     *             <artifactId>spring-cloud-commons</artifactId>
     *             <version>${version}</version>
     *         </dependency>
     *
     *      2、配置文件，或者容器启动变量
     *          spring.cloud.inetutils.preferred-networks: 'xxx.xxx.xxx.'
     *
     *      3、获取IP
     *          String ip_ = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
     */
}
