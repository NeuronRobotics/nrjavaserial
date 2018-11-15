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
import static gnu.io.rfc2217.RFC2217.PURGE_DATA;
import static gnu.io.rfc2217.RFC2217.PURGE_DATA_BOTH_DATA_BUFFERS;
import static gnu.io.rfc2217.RFC2217.PURGE_DATA_RECEIVE_DATA_BUFFER;
import static gnu.io.rfc2217.RFC2217.PURGE_DATA_TRANSMIT_DATA_BUFFER;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;

/**
 * RFC 2217 {@code PURGE-DATA} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public class PurgeDataCommand extends ComPortCommand {

    private int purgeData;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length != 3
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#PURGE_DATA} (client or server)
     *   IllegalArgumentException if {@code bytes[2]} is not a valid RFC 2217 purge data value
     */
    public PurgeDataCommand(int[] bytes) {
        super("PURGE-DATA", PURGE_DATA, bytes);
        this.purgeData = bytes[2];
        switch (this.purgeData) {
        case PURGE_DATA_RECEIVE_DATA_BUFFER:
        case PURGE_DATA_TRANSMIT_DATA_BUFFER:
        case PURGE_DATA_BOTH_DATA_BUFFERS:
            break;
        default:
            throw new IllegalArgumentException("invalid purge data value " + this.purgeData);
        }
    }

    /**
     * Encoding constructor.
     *
     * @param purgeData purge data value
     * @param client true for the client-to-server command, false for the server-to-client command
     *   IllegalArgumentException if {@code purgeData} is not a valid RFC 2217 purge data value
     */
    public PurgeDataCommand(boolean client, int purgeData) {
        this(new int[] {
            COM_PORT_OPTION,
            client ? PURGE_DATA : PURGE_DATA + SERVER_OFFSET,
            purgeData
        });
    }

    @Override
    public String toString() {
        String desc;
        switch (this.purgeData) {
        case PURGE_DATA_RECEIVE_DATA_BUFFER:
            desc = "RECEIVE-DATA-BUFFER";
            break;
        case PURGE_DATA_TRANSMIT_DATA_BUFFER:
            desc = "TRANSMIT-DATA-BUFFER";
            break;
        case PURGE_DATA_BOTH_DATA_BUFFERS:
            desc = "BOTH-DATA-BUFFERS";
            break;
        default:
            desc = "?";
            break;
        }
        return this.getName() + " " + desc;
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.casePurgeData(this);
    }

    public boolean isPurgeReceiveDataBuffer() {
        return (this.purgeData & PURGE_DATA_RECEIVE_DATA_BUFFER) != 0;
    }

    public boolean isPurgeTransmitDataBuffer() {
        return (this.purgeData & PURGE_DATA_TRANSMIT_DATA_BUFFER) != 0;
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

