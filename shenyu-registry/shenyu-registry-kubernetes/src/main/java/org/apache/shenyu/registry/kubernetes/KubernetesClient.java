package org.apache.shenyu.registry.kubernetes;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.discovery.KubernetesServiceInstance;
import org.springframework.cloud.kubernetes.discovery.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description kubernetes client
 * @author windsearcher.lq
 * @date 2024/6/24 11:54
 */
public class KubernetesClient {

    private RestTemplate rest;

    private KubernetesConfig kubernetesConfig;

    public KubernetesClient(KubernetesConfig kubernetesConfig) {
        this.kubernetesConfig = kubernetesConfig;
        this.rest = new RestTemplate();
    }

    public List<ServiceInstance> selectInstances(String serviceId) {
        List<ServiceInstance> response = Collections.emptyList();
        KubernetesServiceInstance[] responseBody = (KubernetesServiceInstance[])this.rest.getForEntity(this.kubernetesConfig.getDiscoveryServerUrl() + "/apps/" + serviceId, KubernetesServiceInstance[].class, new Object[0]).getBody();
        if (responseBody != null && responseBody.length > 0) {
            response = (List) Arrays.stream(responseBody).filter(this::matchNamespaces).collect(Collectors.toList());
        }

        return response;
    }

    public List<String> selectServices() {
        List<String> response = Collections.emptyList();
        Service[] services = (Service[])this.rest.getForEntity(this.kubernetesConfig.getDiscoveryServerUrl() + "/apps", Service[].class, new Object[0]).getBody();
        if (services != null && services.length > 0) {
            response = (List)Arrays.stream(services).filter(this::matchNamespaces).map(Service::getName).collect(Collectors.toList());
        }

        return response;
    }

    private boolean matchNamespaces(KubernetesServiceInstance kubernetesServiceInstance) {
        return CollectionUtils.isEmpty(this.kubernetesConfig.getNamespaces()) ? true : this.kubernetesConfig.getNamespaces().contains(kubernetesServiceInstance.getNamespace());
    }

    private boolean matchNamespaces(Service service) {
        return CollectionUtils.isEmpty(service.getServiceInstances()) ? true : service.getServiceInstances().stream().anyMatch(this::matchNamespaces);
    }
}
