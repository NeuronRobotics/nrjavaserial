/*-------------------------------------------------------------------------
|   RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface.
|   Copyright 2010 by Archie L. Cobbs and others.
|
|   A copy of the LGPL v 2.1 may be found at
|   http://www.gnu.org/licenses/lgpl.txt on March 4th 2007.  A copy is
|   here for your convenience.
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Lesser General Public
|   License as published by the Free Software Foundation; either
|   version 2.1 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Lesser General Public License for more details.
|
|   An executable that contains no derivative of any portion of RXTX, but
|   is designed to work with RXTX by being dynamically linked with it,
|   is considered a "work that uses the Library" subject to the terms and
|   conditions of the GNU Lesser General Public License.
|
|   The following has been added to the RXTX License to remove
|   any confusion about linking to RXTX.   We want to allow in part what
|   section 5, paragraph 2 of the LGPL does not permit in the special
|   case of linking over a controlled interface.  The intent is to add a
|   Java Specification Request or standards body defined interface in the 
|   future as another exception but one is not currently available.
|
|   http://www.fsf.org/licenses/gpl-faq.html#LinkingOverControlledInterface
|
|   As a special exception, the copyright holders of RXTX give you
|   permission to link RXTX with independent modules that communicate with
|   RXTX solely through the Sun Microsytems CommAPI interface version 2,
|   regardless of the license terms of these independent modules, and to copy
|   and distribute the resulting combined work under terms of your choice,
|   provided that every copy of the combined work is accompanied by a complete
|   copy of the source code of RXTX (the version of RXTX used to produce the
|   combined work), being distributed under the terms of the GNU Lesser General
|   Public License plus this exception.  An independent module is a
|   module which is not derived from or based on RXTX.
|
|   Note that people who make modified versions of RXTX are not obligated
|   to grant this special exception for their modified versions; it is
|   their choice whether to do so.  The GNU Lesser General Public License
|   gives permission to release a modified version without this exception; this
|   exception also makes it possible to release a modified version which
|   carries forward this exception.
|
|   You should have received a copy of the GNU Lesser General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
|   All trademarks belong to their respective owners.
--------------------------------------------------------------------------*/
package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.COM_PORT_OPTION;
import static gnu.io.rfc2217.RFC2217.CONTROL_BREAK_OFF;
import static gnu.io.rfc2217.RFC2217.CONTROL_BREAK_ON;
import static gnu.io.rfc2217.RFC2217.CONTROL_BREAK_REQUEST;
import static gnu.io.rfc2217.RFC2217.CONTROL_DTR_OFF;
import static gnu.io.rfc2217.RFC2217.CONTROL_DTR_ON;
import static gnu.io.rfc2217.RFC2217.CONTROL_DTR_REQUEST;
import static gnu.io.rfc2217.RFC2217.CONTROL_INBOUND_FLOW_DTR;
import static gnu.io.rfc2217.RFC2217.CONTROL_INBOUND_FLOW_HARDWARE;
import static gnu.io.rfc2217.RFC2217.CONTROL_INBOUND_FLOW_NONE;
import static gnu.io.rfc2217.RFC2217.CONTROL_INBOUND_FLOW_REQUEST;
import static gnu.io.rfc2217.RFC2217.CONTROL_INBOUND_FLOW_XON_XOFF;
import static gnu.io.rfc2217.RFC2217.CONTROL_OUTBOUND_FLOW_DCD;
import static gnu.io.rfc2217.RFC2217.CONTROL_OUTBOUND_FLOW_DSR;
import static gnu.io.rfc2217.RFC2217.CONTROL_OUTBOUND_FLOW_HARDWARE;
import static gnu.io.rfc2217.RFC2217.CONTROL_OUTBOUND_FLOW_NONE;
import static gnu.io.rfc2217.RFC2217.CONTROL_OUTBOUND_FLOW_REQUEST;
import static gnu.io.rfc2217.RFC2217.CONTROL_OUTBOUND_FLOW_XON_XOFF;
import static gnu.io.rfc2217.RFC2217.CONTROL_RTS_OFF;
import static gnu.io.rfc2217.RFC2217.CONTROL_RTS_ON;
import static gnu.io.rfc2217.RFC2217.CONTROL_RTS_REQUEST;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;
import static gnu.io.rfc2217.RFC2217.SET_CONTROL;

/**
 * RFC 2217 {@code SET-CONTROL} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public class ControlCommand extends ComPortCommand {

    private int control;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length != 3
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_CONTROL} (client or server)
     *   IllegalArgumentException if {@code bytes[2]} is not a valid RFC 2217 control value
     */
    public ControlCommand(int[] bytes) {
        super("SET-CONTROL", SET_CONTROL, bytes);
        this.control = bytes[2];
        switch (this.control) {
        case CONTROL_OUTBOUND_FLOW_REQUEST:
        case CONTROL_OUTBOUND_FLOW_NONE:
        case CONTROL_OUTBOUND_FLOW_XON_XOFF:
        case CONTROL_OUTBOUND_FLOW_HARDWARE:
        case CONTROL_BREAK_REQUEST:
        case CONTROL_BREAK_ON:
        case CONTROL_BREAK_OFF:
        case CONTROL_DTR_REQUEST:
        case CONTROL_DTR_ON:
        case CONTROL_DTR_OFF:
        case CONTROL_RTS_REQUEST:
        case CONTROL_RTS_ON:
        case CONTROL_RTS_OFF:
        case CONTROL_INBOUND_FLOW_REQUEST:
        case CONTROL_INBOUND_FLOW_NONE:
        case CONTROL_INBOUND_FLOW_XON_XOFF:
        case CONTROL_INBOUND_FLOW_HARDWARE:
        case CONTROL_OUTBOUND_FLOW_DCD:
        case CONTROL_INBOUND_FLOW_DTR:
        case CONTROL_OUTBOUND_FLOW_DSR:
            break;
        default:
            throw new IllegalArgumentException("invalid control value " + this.control);
        }
    }

    /**
     * Encoding constructor.
     *
     * @param command control command
     * @param client true for the client-to-server command, false for the server-to-client command
     *   IllegalArgumentException if {@code command} is not a valid RFC 2217 control value
     */
    public ControlCommand(boolean client, int command) {
        this(new int[] {
            COM_PORT_OPTION,
            client ? SET_CONTROL : SET_CONTROL + SERVER_OFFSET,
            command
        });
    }

    @Override
    public String toString() {
        String desc;
        switch (this.control) {
        case CONTROL_OUTBOUND_FLOW_REQUEST:
            desc = "OUTBOUND-FLOW-REQUEST";
            break;
        case CONTROL_OUTBOUND_FLOW_NONE:
            desc = "OUTBOUND-FLOW-NONE";
            break;
        case CONTROL_OUTBOUND_FLOW_XON_XOFF:
            desc = "OUTBOUND-FLOW-XON-XOFF";
            break;
        case CONTROL_OUTBOUND_FLOW_HARDWARE:
            desc = "OUTBOUND-FLOW-HARDWARE";
            break;
        case CONTROL_BREAK_REQUEST:
            desc = "OUTBOUND-BREAK-REQUEST";
            break;
        case CONTROL_BREAK_ON:
            desc = "OUTBOUND-BREAK-ON";
            break;
        case CONTROL_BREAK_OFF:
            desc = "OUTBOUND-BREAK-OFF";
            break;
        case CONTROL_DTR_REQUEST:
            desc = "OUTBOUND-DTR-REQUEST";
            break;
        case CONTROL_DTR_ON:
            desc = "OUTBOUND-DTR-ON";
            break;
        case CONTROL_DTR_OFF:
            desc = "OUTBOUND-DTR-OFF";
            break;
        case CONTROL_RTS_REQUEST:
            desc = "OUTBOUND-RTS-REQUEST";
            break;
        case CONTROL_RTS_ON:
            desc = "OUTBOUND-RTS-ON";
            break;
        case CONTROL_RTS_OFF:
            desc = "OUTBOUND-RTS-OFF";
            break;
        case CONTROL_INBOUND_FLOW_REQUEST:
            desc = "INBOUND-FLOW-REQUEST";
            break;
        case CONTROL_INBOUND_FLOW_NONE:
            desc = "INBOUND-FLOW-NONE";
            break;
        case CONTROL_INBOUND_FLOW_XON_XOFF:
            desc = "INBOUND-FLOW-XON-OFF";
            break;
        case CONTROL_INBOUND_FLOW_HARDWARE:
            desc = "INBOUND-FLOW-HARDWARE";
            break;
        case CONTROL_OUTBOUND_FLOW_DCD:
            desc = "OUTBOUND-FLOW-DCD";
            break;
        case CONTROL_INBOUND_FLOW_DTR:
            desc = "INBOUND-FLOW-DTR";
            break;
        case CONTROL_OUTBOUND_FLOW_DSR:
            desc = "OUTBOUND-FLOW-DSR";
            break;
        default:
            desc = "?";
            break;
        }
        return this.getName() + " " + desc;
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseControl(this);
    }

    public int getControl() {
        return this.control;
    }

    @Override
    int getMinPayloadLength() {
        return 1;
    }

    @Override
    int getMaxPayloadLength() {
        return 1;
    }
}

