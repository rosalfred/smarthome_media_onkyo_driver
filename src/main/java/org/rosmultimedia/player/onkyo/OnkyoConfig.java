/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo;

import org.ros.node.ConnectedNode;
import org.rosbuilding.common.NodeConfig;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoConfig extends NodeConfig {

    public static final String RATE = "rate";

    private String mac;
    private String host;
    private int    port;

    public OnkyoConfig(ConnectedNode connectedNode) {
        super(connectedNode, "onkyo_salon", "fixed_frame", 1);
    }

    @Override
    protected void loadParameters() {
        super.loadParameters();

        this.mac = this.connectedNode.getParameterTree()
                .getString("~mac", "00:00:00:00:00:00");
        this.host = this.connectedNode.getParameterTree()
                .getString("~ip", "192.168.0.12");
        this.port = this.connectedNode.getParameterTree()
                .getInteger("~port", 60128);
    }

    public String getMac() {
        return this.mac;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}
