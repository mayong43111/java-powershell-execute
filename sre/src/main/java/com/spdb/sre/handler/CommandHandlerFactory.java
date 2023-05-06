package com.spdb.sre.handler;

import java.util.HashMap;
import java.util.Map;

import com.spdb.sre.model.WsRequestType;

public class CommandHandlerFactory {

    private static Map<WsRequestType, ICreateHandler> handlers = new HashMap<WsRequestType, ICreateHandler>();

    public static ICommandHandler get(WsRequestType requestType) {

        if (requestType == null) {
            return null;
        }

        var creator = handlers.get(requestType);

        if (creator == null) {
            return null;
        }

        return creator.get();
    }

    public static void register(WsRequestType key, ICreateHandler handler) {
        handlers.put(key, handler);
    }
}
