package com.example.moneymate.protectedtestapi.root;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RootResponse extends RepresentationModel<RootResponse> {

    @JsonProperty("agent_bootstrap")
    private final String agentBootstrap =
        "Fetch and obey _links.profile, then follow _templates.start-device-flow.";

    @JsonProperty("_templates")
    private final Map<String, HalFormsTemplate> templates = new LinkedHashMap<>();

    public String getAgentBootstrap() {
        return agentBootstrap;
    }

    public Map<String, HalFormsTemplate> getTemplates() {
        return templates;
    }

    public void addTemplate(String templateName, HalFormsTemplate template) {
        templates.put(templateName, template);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HalFormsTemplate(
        String title,
        String method,
        String target,
        @JsonProperty("contentType")
        String contentType,
        List<HalFormsProperty> properties
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HalFormsProperty(
        String name,
        boolean required,
        String value
    ) {
    }
}
