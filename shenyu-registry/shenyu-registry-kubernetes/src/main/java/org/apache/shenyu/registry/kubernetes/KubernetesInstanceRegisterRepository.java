package org.apache.shenyu.registry.kubernetes;

import org.apache.shenyu.registry.api.ShenyuInstanceRegisterRepository;
import org.apache.shenyu.registry.api.config.RegisterConfig;
import org.apache.shenyu.registry.api.entity.InstanceEntity;
import org.apache.shenyu.spi.Join;
import org.checkerframework.checker.units.qual.K;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClientProperties;
import org.springframework.cloud.kubernetes.discovery.KubernetesServiceInstance;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @description The type kubernetes instance register repository
 * @author windsearcher.lq
 * @date 2024/6/24 11:01
 */
@Join
public class KubernetesInstanceRegisterRepository implements ShenyuInstanceRegisterRepository {

    private KubernetesClient kubernetesClient;

    @Override
    public void init(final RegisterConfig config) {
        Properties properties = config.getProps();
        KubernetesConfig kubernetesConfig = new KubernetesConfig();
        kubernetesConfig.setDiscoveryServerUrl(config.getServerLists());
        kubernetesConfig.setEnabled(config.getEnabled());
        kubernetesConfig.setNamespaces(Arrays.asList(properties.getProperty("namespaces").split(",")));
        this.kubernetesClient = new KubernetesClient(kubernetesConfig);
    }

    @Override
    public void persistInstance(InstanceEntity instance) {

    }

    @Override
    public List<InstanceEntity> selectInstances(String selectKey) {
        List<ServiceInstance> serviceInstanceList = kubernetesClient.selectInstances(selectKey);
        return serviceInstanceList.stream().map(instance -> InstanceEntity.builder()
                .appName(instance.getServiceId())
                .host(instance.getHost())
                .port(instance.getPort())
                .build()).collect(Collectors.toList());
    }

    @Override
    public void close() {
        ShenyuInstanceRegisterRepository.super.close();
    }
}
