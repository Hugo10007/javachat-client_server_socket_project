package client_server_model;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;



public class ClientThreadTest {

    //We first create a helper class to act as a mock socket for the testing.
    //This class creates a fake socket and overides the input and output stream to
    // test the ClientThread constructor method.

    static class FakeSocket extends Socket {
        private InputStream input;
        private OutputStream output;

        public FakeSocket(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override //override the input data when socket.getInputStream is called
        public InputStream getInputStream() {
            return input;
        }

        @Override //override the output data when socket.getOutputStream is called
        public OutputStream getOutputStream() {
            return output;
        }
    }

    @Test //This tests the ClientThread constructor which creates new client object
          // for communication and stores the input and output from the user.
          //Checks if the constructor works correctly.
    void clientThreadConstructorWorksWithFakeSocket() throws Exception {

        ByteArrayInputStream fakeInput =
                new ByteArrayInputStream("Amin\n".getBytes());

        ByteArrayOutputStream fakeOutput =
                new ByteArrayOutputStream();
        //Create the fake socket
        Socket fakeSocket = new FakeSocket(fakeInput, fakeOutput);
        //Call the constructor
        ClientThread clientThread = new ClientThread(fakeSocket);
        //Checks if the object has been created successfully or not.
        assertNotNull(clientThread);
    }
}
