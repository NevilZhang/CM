package org.nevil.handler;

import org.nevil.request.RequestInfo;

public interface ControllerHandler {
    Object handle(RequestInfo requestInfo);
}
