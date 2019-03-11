package com.boco.config.zk;

public interface IConfigMonitor {
    /**
     * 获取所有配置
     */
    void getConfings();

    /**
     * @param key
     * @param data
     */
    void createConfig(String key, String data);

    /**
     * @param key
     * @return
     */
    String getConfigVal(String key);

    /**
     * @param key
     */
    void delConfig(String key);

    /**
     * @param key
     * @param data
     * @return
     */
    String updateConfig(String key,String data);
}
