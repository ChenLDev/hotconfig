package com.boco.config.impl;

import com.boco.config.Constant;
import com.boco.config.IConfig;
import com.boco.config.zk.IConfigMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * 扩展spring配置文件类,
 * 扩展了zk配置管理功能
 *
 * @author ChenLiang
 * @create 2019 03 06
 */
@Component
public class ExtPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements IConfig {

    private static final Logger log = LoggerFactory.getLogger(ExtPropertyPlaceholderConfigurer.class);

    protected Properties properties;

    @Autowired
    private IConfigMonitor monitor;

    @Override
    protected Properties mergeProperties() throws IOException {
        properties = super.mergeProperties();
        loadProperties(properties);
        return properties;
    }

    @Override
    public void loadProperties(Properties properties){
        if (Constant.TRUE_STR.equals(System.getProperty(Constant.LOAD_ZK))) {
            if (Constant.TRUE_STR.equals(System.getProperty(Constant.LOAD_FIRST))) {
                for (Map.Entry prop : properties.entrySet()) {
                    log.debug("初始化创建配置到zk");
                    monitor.createConfig((String) prop.getKey(), (String) prop.getValue());
                    log.debug("初始化创建成功 key [{}],val [{}]", prop.getKey(), prop.getValue());
                }
            }
            properties.setProperty(Constant.LOAD_ZK, Constant.TRUE_STR);
            log.debug("加载zk配置开启，开始加载zk配置");
            monitor.getConfings();
        } else {
            properties.setProperty(Constant.LOAD_ZK, Constant.FALSE_STR);
            log.debug("加载zk配置开启，开始加载zk配置");
        }
    }

    @Override
    public String getConfigVal(String key) {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(key);
    }

    @Override
    public String removeConfig(String key) {
        String oldVal = (String) properties.remove(key);
        log.debug("config value had del,the key is [{}],the  val is [{}]", key, oldVal);
        return oldVal;
    }

    @Override
    public Properties getConfigProperties(){
        return properties;
    }

    @Override
    public String updateConfig(String key, String val) {
        String oldVal = (String) properties.setProperty(key, val);
        log.debug("config had changed,the old val is [{}],the new val is [{}]", oldVal, val);
        return oldVal;
    }
}
