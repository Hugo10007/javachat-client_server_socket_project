package client_server_model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ClientServerTest {
    @Test //Testing ClientServer start method. In the CLI,we have two options,
          // create or join,this method tests if the user gives something else instead of
          // the 2 options,like "jargon".This only to test the edge case.
    void startWithInvalidSelectionDoesNotCrash() {
        ClientServer clientServer = new ClientServer();
        assertDoesNotThrow(() ->
                clientServer.start("jargon", "127.0.0.1", 5000, "Amin"));
    }

}
