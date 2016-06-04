/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo;

import org.ros.exception.ServiceException;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.service.ServiceResponseBuilder;
import org.rosbuilding.common.BaseNodeMain;
import org.rosbuilding.common.media.MediaMessageConverter;
import org.rosbuilding.common.media.MediaStateDataComparator;
import org.rosmultimedia.player.onkyo.eiscp.OnkyoEiscp;

import de.csmp.jeiscp.eiscp.EiscpCommmandsConstants;
import smarthome_media_msgs.MediaAction;
import smarthome_media_msgs.StateData;
import smarthome_media_msgs.ToggleMuteSpeaker;
import smarthome_media_msgs.ToggleMuteSpeakerRequest;
import smarthome_media_msgs.ToggleMuteSpeakerResponse;


/**
 * Onkyo ROS Node.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoNode extends BaseNodeMain<OnkyoConfig, StateData, MediaAction> {

    public static final String SRV_MUTE_SPEAKER_TOGGLE = "speaker_mute_toggle";

    private OnkyoEiscp onkyoEiscp;
    private OnkyoSpeaker speaker;

    public OnkyoNode() {
        super("onkyo",
                new MediaStateDataComparator(),
                new MediaMessageConverter(),
                MediaAction._TYPE,
                StateData._TYPE);
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
    	super.onStart(connectedNode);
        this.startFinal();
    }

    @Override
    public void onShutdown(Node node) {
        super.onShutdown(node);
    }

    @Override
    protected void onConnected() {
        this.getStateData().setState(StateData.ENABLE);
    }

    @Override
    protected void onDisconnected() {
        this.getStateData().setState(StateData.UNKNOWN);
    }

    @Override
    public void onNewMessage(MediaAction message) {
        if (message != null) {
            this.logI(String.format("Command \"%s\"... for %s",
                    message.getMethod(),
                    message.getUri()));

            super.onNewMessage(message);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.onkyoEiscp = new OnkyoEiscp(this.configuration.getHost(), this.configuration.getPort());

        this.addModule(new OnkyoPlayer(this.onkyoEiscp, this));
//        this.addModule(new OnkyoSpeaker(this.onkyoEiscp, this));
        this.speaker = new OnkyoSpeaker(this.onkyoEiscp, this);
        this.addModule(new OnkyoSystem(this.onkyoEiscp, this));
    }

    /**
     * Try to connect the node to Onkyo receiver.
     */
    @Override
    protected boolean connect() {
        boolean isConnected = false;
        this.logI(String.format("Connecting to %s:%s...", this.configuration.getHost(), this.configuration.getPort()));

        String response = this.onkyoEiscp.sendCommand(
                EiscpCommmandsConstants.SYSTEM_POWER_QUERY_ISCP);

        if (response != null &&
                response.equals(EiscpCommmandsConstants.SYSTEM_POWER_ON_ISCP)) {
            this.getStateData().setState(StateData.INIT);
            isConnected = true;
            this.logI("\tConnected done.");
        } else {
            this.getStateData().setState(StateData.SHUTDOWN);

            try {
                Thread.sleep(10000 / this.configuration.getRate());
            } catch (InterruptedException e) {
                this.logE(e);
            }
        }

        return isConnected;
    }

    /**
     * Initialize all node services.
     */
    protected void initServices() {
        this.getConnectedNode().newServiceServer(
                this.configuration.getPrefix() + SRV_MUTE_SPEAKER_TOGGLE,
                ToggleMuteSpeaker._TYPE,
                new ServiceResponseBuilder<ToggleMuteSpeakerRequest, ToggleMuteSpeakerResponse>() {
                    @Override
                    public void build(ToggleMuteSpeakerRequest request,
                            ToggleMuteSpeakerResponse response) throws ServiceException {
                        OnkyoNode.this.speaker.handleSpeakerMuteToggle(request, response);
                    }
                });
    }

    @Override
    protected OnkyoConfig getConfig() {
        return new OnkyoConfig(this.getConnectedNode());
    }
}
