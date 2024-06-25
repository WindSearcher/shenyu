package org.apache.shenyu.registry.kubernetes;

import java.util.ArrayList;
import java.util.List;

public class KubernetesConfig {
    private String discoveryServerUrl;
    private boolean enabled = true;
    private List<String> namespaces = new ArrayList();

    public KubernetesConfig() {
    }

    public String getDiscoveryServerUrl() {
        return this.discoveryServerUrl;
    }

    public void setDiscoveryServerUrl(String discoveryServerUrl) {
        this.discoveryServerUrl = discoveryServerUrl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    List<String> getNamespaces() {
        return this.namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }
}
