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

import org.rosbuilding.common.media.Speaker;
import org.rosmultimedia.player.onkyo.eiscp.OnkyoEiscp;

import de.csmp.jeiscp.eiscp.EiscpCommmandsConstants;
import smarthome_media_msgs.msg.StateData;
import smarthome_media_msgs.msg.MediaAction;
import smarthome_media_msgs.msg.SpeakerInfo;
import smarthome_media_msgs.srv.ToggleMuteSpeaker_Request;
import smarthome_media_msgs.srv.ToggleMuteSpeaker_Response;


/**
 * Onkyo Speaker module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoSpeaker extends Speaker {
	/**
	 * Onkyo node.
	 */
	private OnkyoNode onkyoNode;

	/**
	 * Onkyo eiscp.
	 */
	private OnkyoEiscp onkyoEiscp;

	/**
	 * OnkyoSpeaker constructor.
	 * @param onkyoEiscp {@link OnkyoEiscp} onkyo eiscp
	 * @param onkyoNode {@link OnkyoNode} onkyo node
	 */
	public OnkyoSpeaker(OnkyoEiscp onkyoEiscp, OnkyoNode onkyoNode) {
		this.onkyoEiscp = onkyoEiscp;
		this.onkyoNode = onkyoNode;
	}

    @Override
    protected void initializeAvailableMethods(List<String> availableMethods) {
        availableMethods.add(OP_MUTE);
        availableMethods.add(OP_MUTE_TOGGLE);
        availableMethods.add(OP_VOLUME_DOWN);
        availableMethods.add(OP_VOLUME_UP);
        availableMethods.add(OP_VOLUME_TO);
        availableMethods.add(OP_CHANNEL);
    }

	@Override
	public void load(StateData stateData) {
	    this.load(stateData.getSpeaker());
	}

	public void load(SpeakerInfo speakerInfo) {
		String muted = this.onkyoEiscp.sendCommand(
				EiscpCommmandsConstants.AUDIO_MUTING_QUERY_ISCP);
		String volume = this.onkyoEiscp.sendCommand(
				EiscpCommmandsConstants.MASTER_VOLUME_QUERY_ISCP);
		String channel = this.onkyoEiscp.sendCommand(
				EiscpCommmandsConstants.INPUT_SELECTOR_QUERY_ISCP);

		speakerInfo.setMuted(muted.equals(
				EiscpCommmandsConstants.AUDIO_MUTING_ON_ISCP));

		int vol = 0;
		//eiscp return MVL (master volume) + volume in hex from 0 to 64 (0-100)
		if (volume.length() >= 0 && volume.startsWith(
				EiscpCommmandsConstants.MASTER_VOLUME_ISCP)) {
			vol = Integer.valueOf(volume.substring(volume.length() - 2), 16);
		}

		speakerInfo.setLevel(vol);
		speakerInfo.setChannel(channel);
	}

	@Override
	public void callbackCmdAction(MediaAction message, StateData stateData) {
		this.onkyoNode.logD("Onkyo Speaker launch command : "
				+ message.getMethod());

		switch (message.getMethod()) {
			case OP_MUTE:
			case OP_MUTE_TOGGLE:
				this.onkyoEiscp.sendCommand(
						EiscpCommmandsConstants.AUDIO_MUTING_TOGGLE_ISCP);
				break;
			case OP_VOLUME_DOWN:
				this.onkyoEiscp.sendCommand(
						EiscpCommmandsConstants.MASTER_VOLUME_LEVEL_DOWN_ISCP);
				break;
			case OP_VOLUME_UP:
				this.onkyoEiscp.sendCommand(
						EiscpCommmandsConstants.MASTER_VOLUME_LEVEL_UP_ISCP);
				break;
			case OP_VOLUME_TO:
				if (!message.getData().isEmpty()) {
					this.onkyoEiscp.sendCommand(
							EiscpCommmandsConstants.MASTER_VOLUME_ISCP,
							message.getData().get(0));
				}
				break;
			case OP_CHANNEL:
				if (!message.getData().isEmpty()) {
					this.onkyoEiscp.sendCommand(
							message.getData().get(0));
				}
				break;
		}
	}

	@Override
	public void handleSpeakerMuteToggle(ToggleMuteSpeaker_Request request,
			ToggleMuteSpeaker_Response response) {
		response.setState(!this.onkyoNode.getStateData().getSpeaker().getMuted());

		this.onkyoNode.logI(String.format("Service call %s : %s",
				OnkyoNode.SRV_MUTE_SPEAKER_TOGGLE,
				this.onkyoNode.getStateData().getSpeaker().getMuted()));

		MediaAction message = new MediaAction();

		message.setMethod(OnkyoSpeaker.OP_MUTE_TOGGLE);

		this.callbackCmdAction(message, this.onkyoNode.getStateData());
	}
}
