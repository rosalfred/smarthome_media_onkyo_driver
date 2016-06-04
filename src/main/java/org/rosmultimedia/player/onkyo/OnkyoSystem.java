/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo;

import org.rosbuilding.common.ISystem;
import org.rosmultimedia.player.onkyo.eiscp.OnkyoEiscp;

import de.csmp.jeiscp.eiscp.EiscpCommmandsConstants;

import smarthome_media_msgs.MediaAction;
import smarthome_media_msgs.StateData;

/**
 * Onkyo System module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoSystem implements ISystem<StateData, MediaAction> {
	/**
	 * Onkyo node.
	 */
	private OnkyoNode onkyoNode;

	/**
	 * Onkyo eiscp.
	 */
	private OnkyoEiscp onkyoEiscp;

	/**
	 * OnkyoSystem constructor.
	 * @param onkyoEiscp {@link OnkyoEiscp} onkyo eiscp
	 * @param onkyoNode {@link OnkyoNode} onkyo node
	 */
	public OnkyoSystem(OnkyoEiscp onkyoEiscp, OnkyoNode onkyoNode) {
		this.onkyoEiscp = onkyoEiscp;
		this.onkyoNode = onkyoNode;
	}

	@Override
	public void load(StateData stateData) {
		String power = this.onkyoEiscp.sendCommand(
				EiscpCommmandsConstants.SYSTEM_POWER_QUERY_ISCP);

		if (power.equals(EiscpCommmandsConstants.SYSTEM_POWER_ON_ISCP)) {
			stateData.setState(StateData.ENABLE);
		} else  if (power.equals(EiscpCommmandsConstants.SYSTEM_POWER_STANDBY_ISCP)) {
			stateData.setState(StateData.SHUTDOWN);
		} else {
			stateData.setState(StateData.UNKNOWN);
		}
	}

	@Override
	public void callbackCmdAction(MediaAction message, StateData stateData) {
		this.onkyoNode.logD("Onkyo System launch command : "
				+ message.getMethod());

		switch (message.getMethod()) {
			case OP_POWER:
				this.onkyoEiscp.sendCommand(
						EiscpCommmandsConstants.SYSTEM_POWER_ON_ISCP);
				break;
			case OP_SHUTDOWN:
				this.onkyoEiscp.sendCommand(
						EiscpCommmandsConstants.SYSTEM_POWER_STANDBY_ISCP);
				break;
		}
	}
}
