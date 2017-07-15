/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo;

import org.ros2.rcljava.node.Node;
import org.rosbuilding.common.NodeDriverConnectedConfig;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoConfig extends NodeDriverConnectedConfig {
    public OnkyoConfig(Node connectedNode) {
        super(
                connectedNode,
                "/home/salon",
                "onkyo",
                "fixed_frame",
                1,
                "00:00:00:00:00:00",
                "192.168.0.68",
                60128L,
                "",
                "");
    }
}
