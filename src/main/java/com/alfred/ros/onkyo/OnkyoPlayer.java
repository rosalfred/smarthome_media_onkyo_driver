/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.alfred.ros.onkyo;

import media_msgs.MediaAction;
import media_msgs.PlayerInfo;
import media_msgs.StateData;

import com.alfred.ros.media.IPlayer;
import com.alfred.ros.onkyo.eiscp.OnkyoEiscp;

/**
 * Onkyo Player module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoPlayer implements IPlayer {
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
	public void load(PlayerInfo playerInfo) {

	}

	@Override
	public void callbackCmdAction(MediaAction message, StateData stateData) {

	}
}
