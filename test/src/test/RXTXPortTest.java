/*
 * $Id$
 *
 * Copyright (c) 2018 NCR GmbH. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * NCR GmbH. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with NCR GmbH.
 *
 * NCR GmbH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * NCR GmbH SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED
 * BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 *
 */

package gnu.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.Test;

/**
 * ###TODO
 *     
 * @version 1.0 (2018-04-17)
 * @author  Heri Bender (Ergonomics)
 */
public class RXTXPortTest
{

    @Test
    public void test() throws Throwable
    {
        File buildDir = new File( ".");
        Properties props = new Properties();
            props.load(new FileInputStream(buildDir.getAbsolutePath() + "/src/main/resources/com/neuronrobotics/nrjavaserial/build.properties"));
            System.out.println("build.properties:\n    " + props);
            File userPropsFile = new File(buildDir.getAbsolutePath() + "/user.build.properties");
            if ( userPropsFile.exists() )
            {
                System.out.println("build.properties (2):\n    " + props);
                Properties userProps = new Properties(props);
                System.out.println("after construct with overloaded with user.build.properties:\n    " + userProps);
                userProps.load(new FileInputStream(userPropsFile));
                System.out.println("overloaded with user.build.properties:\n    " + props);
                System.out.println("app.name:\n    " + props.getProperty( "app.name"));
                
                for ( Object key : props.keySet() )
                {
                    String value = props.getProperty( (String) key);
                    userProps.setProperty( (String) key, value);
                }
                
                props = userProps;
                
                System.out.println("overloaded with user.build.properties:\n    " + props);
                System.out.println("app.name:\n    " + props.getProperty( "app.name"));
            }
    } 

    
}
