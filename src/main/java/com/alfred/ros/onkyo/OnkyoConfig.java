package com.alfred.ros.onkyo;

import org.ros.dynamic_reconfigure.server.BaseConfig;
import org.ros.node.ConnectedNode;

public class OnkyoConfig extends BaseConfig {

    public static final String RATE = "rate";

    public OnkyoConfig(ConnectedNode connectedNode) {
        super(connectedNode);
        this.addField(RATE, "int", 0, "rate processus", 1, 0, 200);
    }

}
