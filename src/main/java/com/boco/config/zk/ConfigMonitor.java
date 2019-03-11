package com.boco.config.zk;

import com.boco.config.Constant;
import com.boco.config.IConfig;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 配置类watcher
 *
 * @author ChenLiang
 * @create 2019 01 30
 * zk.getData 监听节点的删除和数据变化事件
 */
@Component
public class ConfigMonitor implements Watcher,IConfigMonitor {
    private static final Logger log = LoggerFactory.getLogger(ConfigMonitor.class);
    @Autowired
    private ZooKeeper zk;
    @Autowired
    private IConfig iConfig;

    @Override
    public void process(WatchedEvent watchedEvent) {
        log.debug("trigger the WatcherEvent,the path[{}],the type[{}],the state[{}]",
                watchedEvent.getPath(), watchedEvent.getType(), watchedEvent.getState());
        if (watchedEvent.getType() != Event.EventType.None){
            String key = watchedEvent.getPath();
            switch (watchedEvent.getType()) {
                case NodeChildrenChanged:
                    getConfings();
                    break;
                case NodeCreated:
                case NodeDataChanged:
                    updateConfigAndWatcher(key);
                    break;
                case NodeDeleted:
                    key = prefixFilter(key);
                    iConfig.removeConfig(key);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取zk里所有配置，并监听节点信息
     */
    @Override
    public void getConfings() {
        try {
            List<String> children = zk.getChildren(Constant.ROOT_SEPARATOR, this);
            for (String childrenPath : children) {
                byte[] data = zk.getData(addSeparatorPreFix(childrenPath), this, null);
                iConfig.updateConfig(childrenPath, new String(data));
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createConfig(String key, String data) {
        key = addSeparatorPreFix(key);
        try {
            if (zk.exists(key, false) == null) {
                zk.create(key, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                zk.setData(key, data.getBytes(), -1);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getConfigVal(String key) {
        key = addSeparatorPreFix(key);
        try {
            return new String(zk.getData(key, null, null));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delConfig(String key) {
        key = addSeparatorPreFix(key);
        try {
            if (zk.exists(key, false) != null) {
                zk.delete(key,-1);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String updateConfig(String key, String data) {
        key = addSeparatorPreFix(key);
        try {
            if (zk.exists(key, false) != null) {
                zk.setData(key, data.getBytes(), -1);
            }else{
                zk.create(key, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param key
     */
    private void updateConfigAndWatcher(String key) {
        try {
            String dataVal = new String(zk.getData(key, this, null));
            key = prefixFilter(key);
            iConfig.updateConfig(key, dataVal);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化前面带文件符的路径
     *
     * @param withPrefixPath
     * @return
     */
    private String prefixFilter(String withPrefixPath) {
        if (withPrefixPath.startsWith(Constant.ROOT_SEPARATOR)) {
            return withPrefixPath.substring(1);
        }
        return withPrefixPath;
    }

    /**
     * 对路径加上根节点
     *
     * @param path
     * @return
     */
    private String addSeparatorPreFix(String path) {
        if (path.startsWith(Constant.ROOT_SEPARATOR)) {
            return path;
        } else {
            return Constant.ROOT_SEPARATOR + path;
        }
    }
}
