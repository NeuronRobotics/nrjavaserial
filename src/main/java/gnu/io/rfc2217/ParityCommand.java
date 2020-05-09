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
import static gnu.io.rfc2217.RFC2217.PARITY_EVEN;
import static gnu.io.rfc2217.RFC2217.PARITY_MARK;
import static gnu.io.rfc2217.RFC2217.PARITY_NONE;
import static gnu.io.rfc2217.RFC2217.PARITY_ODD;
import static gnu.io.rfc2217.RFC2217.PARITY_REQUEST;
import static gnu.io.rfc2217.RFC2217.PARITY_SPACE;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;
import static gnu.io.rfc2217.RFC2217.SET_PARITY;

/**
 * RFC 2217 {@code SET-PARITY} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public class ParityCommand extends ComPortCommand {

    private int parity;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length != 3
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_PARITY} (client or server)
     *   IllegalArgumentException if {@code bytes[2]} is not a valid RFC 2217 parity value
     */
    public ParityCommand(int[] bytes) {
        super("SET-PARITY", SET_PARITY, bytes);
        this.parity = bytes[2];
        switch (this.parity) {
        case PARITY_REQUEST:
        case PARITY_NONE:
        case PARITY_ODD:
        case PARITY_EVEN:
        case PARITY_MARK:
        case PARITY_SPACE:
            break;
        default:
            throw new IllegalArgumentException("invalid parity value " + this.parity);
        }
    }

    /**
     * Encoding constructor.
     *
     * @param parity parity value
     * @param client true for the client-to-server command, false for the server-to-client command
     *   IllegalArgumentException if {@code parity} is not a valid RFC 2217 parity value
     */
    public ParityCommand(boolean client, int parity) {
        this(new int[] {
            COM_PORT_OPTION,
            client ? SET_PARITY : SET_PARITY + SERVER_OFFSET,
            parity
        });
    }

    @Override
    public String toString() {
        String desc;
        switch (this.parity) {
        case PARITY_REQUEST:
            desc = "REQUEST";
            break;
        case PARITY_NONE:
            desc = "NONE";
            break;
        case PARITY_ODD:
            desc = "ODD";
            break;
        case PARITY_EVEN:
            desc = "EVEN";
            break;
        case PARITY_MARK:
            desc = "MARK";
            break;
        case PARITY_SPACE:
            desc = "SPACE";
            break;
        default:
            desc = "?";
            break;
        }
        return this.getName() + " " + desc;
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseParity(this);
    }

    public int getParity() {
        return this.parity;
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

