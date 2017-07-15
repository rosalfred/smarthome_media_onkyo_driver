/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo;

import java.util.List;

import org.rosbuilding.common.media.Player;
import org.rosmultimedia.player.onkyo.eiscp.OnkyoEiscp;

import smarthome_media_msgs.msg.StateData;
import smarthome_media_msgs.msg.MediaAction;

/**
 * Onkyo Player module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoPlayer extends Player {
	/**
	 * Onkyo node.
	 */
	@SuppressWarnings("unused")
	private OnkyoNode onkyoNode;

	/**
	 * Onkyo eiscp.
	 */
	@SuppressWarnings("unused")
	private OnkyoEiscp onkyoEiscp;

	/**
	 * OnkyoPlayer constructor.
	 * @param onkyoEiscp {@link OnkyoEiscp} onkyo eiscp
	 * @param onkyoNode {@link OnkyoNode} onkyo node
	 */
	public OnkyoPlayer(OnkyoEiscp onkyoEiscp, OnkyoNode node) {
		this.onkyoEiscp = onkyoEiscp;
		this.onkyoNode = node;
	}

    @Override
    protected void initializeAvailableMethods(List<String> arg0) {

    }

	@Override
	public void callbackCmdAction(MediaAction message, StateData stateData) {

	}

    @Override
    public void load(StateData stateData) {
        // TODO Auto-generated method stub

    }
}
