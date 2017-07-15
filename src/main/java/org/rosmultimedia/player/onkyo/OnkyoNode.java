/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo;

import org.ros2.rcljava.RCLJava;
import org.ros2.rcljava.namespace.GraphName;
import org.ros2.rcljava.node.Node;
import org.ros2.rcljava.node.service.RMWRequestId;
import org.ros2.rcljava.node.service.ServiceCallback;
import org.rosbuilding.common.BaseDriverNode;
import org.rosbuilding.common.media.MediaMessageConverter;
import org.rosbuilding.common.media.MediaStateDataComparator;
import org.rosmultimedia.player.onkyo.eiscp.OnkyoEiscp;

import de.csmp.jeiscp.eiscp.EiscpCommmandsConstants;

import smarthome_media_msgs.msg.StateData;
import smarthome_media_msgs.msg.MediaAction;
import smarthome_media_msgs.srv.ToggleMuteSpeaker_Request;
import smarthome_media_msgs.srv.ToggleMuteSpeaker_Response;
import smarthome_media_msgs.srv.MediaGetItem;


/**
 * Onkyo ROS Node.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoNode extends BaseDriverNode<OnkyoConfig, StateData, MediaAction> {

    public static final String SRV_MUTE_SPEAKER_TOGGLE = "speaker_mute_toggle";

    private OnkyoEiscp onkyoEiscp;
    private OnkyoSpeaker speaker;

    public OnkyoNode() {
        super(
                new MediaStateDataComparator(),
                new MediaMessageConverter(),
                MediaAction.class.getName(),
                StateData.class.getName());
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

    @Override
    protected void initTopics() {
        super.initTopics();

        try {
            this.getConnectedNode().<MediaGetItem>createService(MediaGetItem.class,
                    GraphName.getFullName(this.connectedNode, SRV_MUTE_SPEAKER_TOGGLE, null),
                    new ServiceCallback<ToggleMuteSpeaker_Request, ToggleMuteSpeaker_Response>() {
                        @Override
                        public void dispatch(
                                final RMWRequestId header,
                                final ToggleMuteSpeaker_Request request,
                                final ToggleMuteSpeaker_Response response) {
                            OnkyoNode.this.speaker.handleSpeakerMuteToggle(request, response);
                        }
                    });
        } catch (Exception e) {

        }
    }

    @Override
    protected OnkyoConfig makeConfiguration() {
        return new OnkyoConfig(this.getConnectedNode());
    }

    public static void main(String[] args) throws InterruptedException {
        RCLJava.rclJavaInit();

        final OnkyoNode onkyo = new OnkyoNode();
        final Node node = RCLJava.createNode("onkyo");

        onkyo.onStart(node);
        onkyo.onStarted();

        RCLJava.spin(node);

        onkyo.onShutdown();
        onkyo.onShutdowned();

        RCLJava.shutdown();
    }
}
