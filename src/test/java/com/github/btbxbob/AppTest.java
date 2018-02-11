package com.github.btbxbob;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception
    {
        //载入配置
        Properties prop = new Properties();
        prop.load(AppTest.class.getResourceAsStream("/PROPERTIES/config.properties"));
        App.wxpayMediaUpload(prop.getProperty("mch_id"),
                            prop.getProperty("media_hash"), 
                            prop.getProperty("sign"), 
                            prop.getProperty("media"), 
                            prop.getProperty("p12cert"));
    }
}
