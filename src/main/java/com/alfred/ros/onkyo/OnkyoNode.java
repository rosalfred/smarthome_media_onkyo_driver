/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.alfred.ros.onkyo;

import media_msgs.StateData;
import media_msgs.ToggleMuteSpeaker;
import media_msgs.ToggleMuteSpeakerRequest;
import media_msgs.ToggleMuteSpeakerResponse;

import org.ros.dynamic_reconfigure.server.Server;
import org.ros.dynamic_reconfigure.server.Server.ReconfigureListener;
import org.ros.exception.ServiceException;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.service.ServiceResponseBuilder;

import com.alfred.ros.media.BaseMediaNodeMain;
import com.alfred.ros.onkyo.eiscp.OnkyoEiscp;

import de.csmp.jeiscp.eiscp.EiscpCommmandsConstants;


/**
 * Onkyo ROS Node.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoNode extends BaseMediaNodeMain
        implements ReconfigureListener<OnkyoConfig> {

    public static final String SRV_MUTE_SPEAKER_TOGGLE = "speaker_mute_toggle";

    private OnkyoEiscp onkyoEiscp;

    static {
        nodeName = "onkyo";
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

    /**
     *
     * @return Current {@link StateData}
     */
    public StateData getStateData() {
        return this.stateData;
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.onkyoEiscp = new OnkyoEiscp(this.host, this.port);

        this.player = new OnkyoPlayer(this.onkyoEiscp, this);
        this.speaker = new OnkyoSpeaker(this.onkyoEiscp, this);
        this.system = new OnkyoSystem(this.onkyoEiscp, this);
    }

    @Override
    protected void refreshStateData() throws InterruptedException {
        if (!this.onkyoEiscp.isConnected()) {
            this.isConnected = false;
        }

        super.refreshStateData();
    }

    /**
     * Try to connect the node to Onkyo receiver.
     */
    @Override
    protected void connect() {
        this.logI(String.format("Connecting to %s:%s...", this.host, this.port));

        String response = this.onkyoEiscp.sendCommand(
                EiscpCommmandsConstants.SYSTEM_POWER_QUERY_ISCP);

        if (response != null &&
                response.equals(EiscpCommmandsConstants.SYSTEM_POWER_ON_ISCP)) {
            this.stateData.setState(StateData.INIT);
            this.isConnected = true;
            this.logI("\tConnected done.");
        } else {
            this.stateData.setState(StateData.SHUTDOWN);

            try {
                Thread.sleep(10000 / this.rate);
            } catch (InterruptedException e) {
                this.logE(e);
            }
        }
    }

    /**
     * Load config from ros master (launch file).
     */
    @Override
    protected void loadParameters() {
        this.logI("Load parameters.");

        this.prefix = String.format("/%s/", this.connectedNode.getParameterTree()
                .getString("~tf_prefix", "onkyo_salon"));
        this.logI(String.format("prefix :", this.prefix));

        this.fixedFrame = this.connectedNode.getParameterTree()
                .getString("~fixed_frame", "fixed_frame");
        this.logI(String.format("fixedFrame :", this.fixedFrame));

        this.rate = this.connectedNode.getParameterTree()
                .getInteger("~" + OnkyoConfig.RATE, 1);

        if (this.rate <= 0) {
            this.rate = 1;
        }

        this.logI(String.format("rate :", this.rate));

        this.mac = this.connectedNode.getParameterTree()
                .getString("~mac", "00:01:2E:BC:16:33");
        this.logI(String.format("mac :", this.mac));

        this.host = this.connectedNode.getParameterTree()
                .getString("~ip", "192.168.0.12");
        this.logI(String.format("ip :", this.host));

        this.port = this.connectedNode.getParameterTree()
                .getInteger("~port", 60128);
        this.logI(String.format("port :", this.port));

        this.serverReconfig = new Server<OnkyoConfig>(connectedNode, new OnkyoConfig(connectedNode), this);
    }

    /**
     * Initialize all node services.
     */
    protected void initServices() {
        this.connectedNode.newServiceServer(
                this.prefix + SRV_MUTE_SPEAKER_TOGGLE,
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
    public OnkyoConfig onReconfigure(OnkyoConfig config, int level) {
        this.rate = config.getInteger(OnkyoConfig.RATE, this.rate);
        return config;
    }

    public ConnectedNode getNode() {
        return this.connectedNode;
    }
}
