package client_server_model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ServerTest {
    @Test //testing Server get_ip_address() method
          //Checks when the server asks for IP address,
          //it returns a ip address string not a null(nothing)
    void getIpAddressNotEmptyStringReturnsNotNull() throws Exception {
        String ip = Server.get_ip_address();
        assertNotNull(ip);
    }

    @Test  // Same as above,but tests if when asked for IP address,
           // there should be something as an input and not blank like " ".
    void getIpAddressReturnsNotBlank() throws Exception {
        String ip = Server.get_ip_address();
        assertFalse(ip.isBlank());
    }

    @Test   //Checks if the method allows for valid IP address format to pass,
            //not any generic string like "hello", must follow ip add syntax like
            // xxx.xxx.x.x (127.00.1).Also allows "localhost"
    void getIpAddressHasBasicValidFormat() throws Exception {
        String ip = Server.get_ip_address();
        assertTrue(ip.contains(".") || ip.equalsIgnoreCase("localhost"));
    }


}