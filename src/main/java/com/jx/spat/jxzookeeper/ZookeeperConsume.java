package com.jx.spat.jxzookeeper;

import com.jx.spat.jxzookeeper.constant.ZookeeperConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * Created by xiaowei on 17/8/3.
 */
public class ZookeeperConsume {
    public static void main(String[] args){
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZookeeperConstant.zookeeper_host,new ExponentialBackoffRetry(1000,1));
        client.start();

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryBuilder
                .builder(Void.class)
                .client(client)
                .basePath(ZookeeperConstant.base_path)
                .serializer(new JsonInstanceSerializer<Void>(Void.class))
                .build();
        try {
            serviceDiscovery.start();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
