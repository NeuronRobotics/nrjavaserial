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

import java.util.Arrays;

import static gnu.io.rfc2217.RFC2217.COM_PORT_OPTION;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;

/**
 * Superclass for RFC 2217 commands.
 *
 *  
 * Instances of this class (and all subclasses) are immutable.
 *  
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public abstract class ComPortCommand {

    final String name;
    final int[] bytes;

    /**
     * Constructor.
     *
     * @param name human readable name of this command
     * @param command required {@code COM-PORT-OPTION} command byte value (must be the client-to-server value)
     * @param bytes encoded command starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length that is too short or too long
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code command} is greater than or equal to {@link RFC2217#SERVER_OFFSET}
     *   IllegalArgumentException if {@code bytes[1]} is not {@code command}
     *  or {@code command} + {@link RFC2217#SERVER_OFFSET}
     */
    protected ComPortCommand(String name, int command, int[] bytes) {
        this.name = name;
        int minLength = 2 + this.getMinPayloadLength();
        int maxLength = 2 + this.getMaxPayloadLength();
        if (bytes.length < minLength || bytes.length > maxLength) {
            throw new IllegalArgumentException("command " + command + " length = "
              + bytes.length + " is not in the range " + minLength + ".." + maxLength);
        }
        this.bytes = bytes.clone();                 // maintain immutability
        if (this.bytes[0] != COM_PORT_OPTION)
            throw new IllegalArgumentException("not a COM-PORT-OPTION");
        if (command >= SERVER_OFFSET)
            throw new IllegalArgumentException("invalid command " + command);
        if (this.getCommand() != command && this.getCommand() != command + SERVER_OFFSET)
            throw new IllegalArgumentException("not a " + name + " option");
    }

    /**
     * Determine if this command is client-to-server or server-to-client.
     *
     * @return true if this command is sent from the server to the client, false for the opposite
     */
    public final boolean isServerCommand() {
        return this.getCommand() >= SERVER_OFFSET;
    }

    /**
     * Get the encoding of this instance.
     *
     * @return encoding starting with {@code COM-PORT-OPTION}
     */
    public final int[] getBytes() {
        return this.bytes.clone();                  // maintain immutability
    }

    /**
     * Get the command byte.
     *
     * @return RFC 2217-defined byte value for this command
     */
    public final int getCommand() {
        return this.bytes[1] & 0xff;
    }

    /**
     * Get the human-readable name of this option.
     */
    public String getName() {
        return this.name + (this.isServerCommand() ? "[S]" : "[C]");
    }

    /**
     * Get the human-readable description of this option.
     */
    @Override
    public abstract String toString();

    /**
     * Apply visitor pattern.
     *
     * @param sw visitor switch handler
     */
    public abstract void visit(ComPortCommandSwitch sw);

    /**
     * Get the option payload as bytes.
     */
    byte[] getPayload() {
        byte[] buf = new byte[this.bytes.length - 2];
        for (int i = 2; i < bytes.length; i++)
            buf[i - 2] = (byte)this.bytes[i];
        return buf;
    }

    /**
     * Get minimum required length of the payload of this command.
     */
    abstract int getMinPayloadLength();

    /**
     * Get maximum required length of the payload of this command.
     */
    abstract int getMaxPayloadLength();

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass())
            return false;
        ComPortCommand that = (ComPortCommand)obj;
        return Arrays.equals(this.bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.bytes.length; i++)
            hash = hash * 37 + this.bytes[i];
        return hash;
    }
}

