package org.nevil.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestInfo {
    private String method;
    private String url;
    private Map<String, String> headers = new HashMap<>();
    private String body;
}
