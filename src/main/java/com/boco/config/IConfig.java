package com.boco.config;

import java.util.Properties;

/**
 * 配置对外提供的操作接口
 */
public interface IConfig {
    /**
     * 装载配置，实现该方法扩展配置
     * @param properties
     */
    void loadProperties(Properties properties);
    /**
     * 获取配置内存对象，
     * @return
     */
    Properties getConfigProperties();

    /**
     * 获取特定key的配置值，若没有该对应值返回null
     * @param key 内存映射key
     * @return
     */
    String getConfigVal(String key);

    /**
     * 删除相应key的配置
     * @param key
     * @return 被删除的值
     */
    String removeConfig(String key);

    /**
     * 更新配置
     * @param key
     * @return 返回更新前的配置
     */
    String updateConfig(String key, String val);
}
