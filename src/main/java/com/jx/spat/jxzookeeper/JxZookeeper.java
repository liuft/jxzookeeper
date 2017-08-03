package com.jx.spat.jxzookeeper;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaowei on 17/8/2.
 */
public class JxZookeeper {
    private static String zookeeper_host = "192.168.2.78:2181";
    private static String service_host = "192.168.2.77";
    private static int service_port = 12309;
    private static String service_name = "union";
    private static String base_path = "/";
    private static List<Closeable> closeablelist = Lists.newArrayList();
    public static void main(String[] argo){
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zookeeper_host,new ExponentialBackoffRetry(1000,1));
        curatorFramework.start();

        try {
            ServiceInstance<Void> serviceInstance = ServiceInstance.<Void>builder().name(service_name).
                    port(service_port).address(service_host).uriSpec(new UriSpec("tcp://union/EmployService")).build();

            ServiceDiscovery<Void> serviceDiscovery = registerInZookeeper(curatorFramework,serviceInstance);

            Iterator<String> iterator = serviceDiscovery.queryForNames().iterator();
            while (iterator.hasNext()){
                String service_name = iterator.next();
                System.out.println("service name is "+service_name);
            }
            serviceDiscovery.registerService(serviceInstance);
            Iterator serviceit = serviceDiscovery.queryForInstances(service_name).iterator();
            while (serviceit.hasNext()){

                ServiceInstance<Void> instance = (ServiceInstance<Void>) serviceit.next();
                System.out.println(instance.getAddress());
            }
            ServiceInstance<Void> instance_loopu = getForInstanceByname(serviceDiscovery,service_name);
            System.out.println("address is "+instance_loopu.buildUriSpec());
            System.out.println("playload is "+instance_loopu.getPayload());
            close();
            CloseableUtils.closeQuietly(curatorFramework);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ServiceInstance<Void> getForInstanceByname(ServiceDiscovery<Void> serviceDiscovery, String es1)throws Exception {
        ServiceProvider<Void> provider = serviceDiscovery.serviceProviderBuilder()
                .serviceName(es1).providerStrategy(new RandomStrategy<Void>()).build();
        provider.start();
        closeablelist.add(provider);
        return provider.getInstance();
    }

    public static synchronized void close(){
        for(Closeable closeble : closeablelist){
            CloseableUtils.closeQuietly(closeble);
        }
    }
    public static ServiceDiscovery<Void> registerInZookeeper(CuratorFramework client,ServiceInstance<Void> instance)throws Exception{
        ServiceDiscovery<Void> sd = ServiceDiscoveryBuilder.builder(Void.class)
                .client(client)
                .serializer(new JsonInstanceSerializer<Void>(Void.class))
                .basePath(base_path)
                .thisInstance(instance)
                .build();
        sd.start();
        return sd;
    }
}
