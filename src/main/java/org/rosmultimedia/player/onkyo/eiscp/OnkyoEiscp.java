/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.onkyo.eiscp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.common.base.Strings;

import de.csmp.jeiscp.EiscpConnector;
import de.csmp.jeiscp.EiscpListener;
import de.csmp.jeiscp.eiscp.EiscpCommmandsConstants;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class OnkyoEiscp {

	private final String host;
	private final int port;
	private EiscpConnector eiscpConnector;
	private Map<String, String> iscpResult;

	private EiscpListener messageListener = new EiscpListener() {

		@Override
		public void receivedIscpMessage(String message) {
			iscpResult.put(message.substring(0, 3), message);
		}

	};

	public OnkyoEiscp(String host, int port) {
		this.host = host;
		this.port = port;

		this.iscpResult = new HashMap<String, String>();

		this.initialize();
	}

	private void initialize() {
		try {
			this.iscpResult.clear();
			this.eiscpConnector = new EiscpConnector(this.host, this.port);
			this.eiscpConnector.attachListener(this.messageListener);
		} catch (IOException e) {

        }
	}

	public String getIscpResponse(String prefix) {
		String result = null;

		DateTime startTime = DateTime.now();

		while(true) {
			prefix = this.getCommandPrefix(prefix);

			if (this.iscpResult.containsKey(prefix)) {
				result = this.iscpResult.get(prefix);
				break;
			}

			if (new Period(startTime, DateTime.now()).getMillis() > 50) {
				break;
			}

			try {
				Thread.sleep(0, 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private String getCommandPrefix(String command) {
		String result = command;

		if (result.length() > 3) {
			result = result.substring(0, 3);
		}

		return result;
	}

	public String sendCommand(String command) {
		return this.sendCommand(command, null);
	}

	public String sendCommand(String command, String args) {
		String result = null;

		if (!this.isConnected()) {
			this.initialize();
		}

		if (!Strings.isNullOrEmpty(args)
				&& command == EiscpCommmandsConstants.MASTER_VOLUME_ISCP) {
			String hexVol = Integer.toHexString(Integer.valueOf(args));

			if (hexVol.length() == 1) {
				hexVol = "0" + hexVol;
			}

			command += hexVol;
		}

		if (this.isConnected()) {
    		try {
    			if (!this.iscpResult.containsKey(this.getCommandPrefix(command))
    			        || !command.endsWith("QSTN")) {
    				this.eiscpConnector.sendIscpCommand(command);
    			}

    			result = this.getIscpResponse(command);

    			if (result == null) {
    				result = "";
    			}
    		} catch (IOException e) {
                this.eiscpConnector = null;
            }
		}

		return result;
	}

	public boolean isConnected() {
		return this.eiscpConnector != null && this.eiscpConnector.isConnected()
				&& !this.eiscpConnector.isClosed();
	}
}
