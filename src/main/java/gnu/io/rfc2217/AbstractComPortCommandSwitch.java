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

/**
 * Adapter class for {@link ComPortCommandSwitch} implementations.
 *
 * @see ComPortCommandSwitch
 * @see ComPortCommand#visit
 */
public class AbstractComPortCommandSwitch implements ComPortCommandSwitch {

    /**
     * Visit method invoked by {@link SignatureCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseSignature(SignatureCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link BaudRateCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseBaudRate(BaudRateCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link DataSizeCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseDataSize(DataSizeCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link ParityCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseParity(ParityCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link StopSizeCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseStopSize(StopSizeCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link ControlCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseControl(ControlCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link NotifyLineStateCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseNotifyLineState(NotifyLineStateCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link NotifyModemStateCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseNotifyModemState(NotifyModemStateCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link FlowControlSuspendCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseFlowControlSuspend(FlowControlSuspendCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link FlowControlResumeCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseFlowControlResume(FlowControlResumeCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link LineStateMaskCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseLineStateMask(LineStateMaskCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link ModemStateMaskCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void caseModemStateMask(ModemStateMaskCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link PurgeDataCommand} instances.
     *
     *  
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *  
     */
    @Override
	public void casePurgeData(PurgeDataCommand command) {
        caseDefault(command);
    }

    /**
     * Default handler.
     *
     *  
     * All other methods in {@link AbstractComPortCommandSwitch} delegate to this method;
     * the implementation in {@link AbstractComPortCommandSwitch} does nothing.
     *  
     */
    protected void caseDefault(ComPortCommand command) {
    }
}

