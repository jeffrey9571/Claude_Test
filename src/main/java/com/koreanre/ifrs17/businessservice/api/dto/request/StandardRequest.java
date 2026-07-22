package com.koreanre.ifrs17.businessservice.api.dto.request;

import java.util.Collections;
import java.util.Map;

/**
 * 5.3 표준 Request.
 */
public class StandardRequest {

    private String serviceVersion;
    private Map<String, Object> parameters = Collections.emptyMap();
    private RequestOptions options = new RequestOptions();

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters == null ? Collections.emptyMap() : parameters;
    }

    public RequestOptions getOptions() {
        return options;
    }

    public void setOptions(RequestOptions options) {
        this.options = options == null ? new RequestOptions() : options;
    }

    public static class RequestOptions {
        private String locale = "ko-KR";
        private boolean includeDetails = false;

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public boolean isIncludeDetails() {
            return includeDetails;
        }

        public void setIncludeDetails(boolean includeDetails) {
            this.includeDetails = includeDetails;
        }
    }
}
