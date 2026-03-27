package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import gui.Gui;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;

public class Main_controllerTest {

    @BeforeEach //used to reset the GUI after every test. USED FOR RELIABILITY & ROBUSTNESS.
    void resetGuiInstance() throws Exception {
        Field instance = Gui.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null); // ensures no GUI instance exists during tests
    }

    @Test //Tests message_sent_from_user
    void testMessageSentFromUserCorrectly() {

        String expectedMessage = "Welcome to Main Controller!";

        Main_controller.message_sent_from_user(expectedMessage);
        String actualMessage = Main_controller.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test //Tests getMessage() returning Null when it is Null
    void testGetMessageReturnsNullWhenEmpty() {

        Main_controller.getMessage();

        String result = Main_controller.getMessage();

        //If message doesnt exist return Null
        assertNull(result);
    }

    @Test // getMessage() to return a string
    void testGetMessageReturnsStoredMessage() {

        Main_controller.message_sent_from_user("Hello");

        String result = Main_controller.getMessage();

        //See if the message sent is correct
        assertEquals("Hello", result);
    }

    @Test// getMessage() to clear the string after sending the message for the next message
    void testGetMessageClearsAfterReading() {
        Main_controller.message_sent_from_user("Test");

        String first = Main_controller.getMessage();
        String second = Main_controller.getMessage();

        // combine
        assertEquals("Test", first);
        assertNull(second);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testUpdateUserListParsesCoordinatorAndUsersCorrectly() throws Exception {
        //Initiate
        Gui gui = new Gui();

        DefaultListModel<String> model = new DefaultListModel<>();
        setField(gui, "onlineUsersModel", model);

        String data = "Amin|Amin:127.0.0.1:5000,Hugo:127.0.0.1:5001";

        Main_controller.update_user_list(data);

        // Wait for Swing update if needed
        SwingUtilities.invokeAndWait(() -> {});


        assertEquals("Amin", gui.coordinatorUsername);
        assertEquals(2, model.size());
        assertEquals("Amin", model.getElementAt(0));
        assertEquals("Hugo", model.getElementAt(1));
    }

    @Test // getMessage() is thread safe, two threads should not both get the same message, only 1 should. Uses synchronized threads in ClientServer so test SHOULD pass.
    void testGetMessageIsThreadSafe() throws InterruptedException {
        Main_controller.message_sent_from_user("ThreadTest");

        String[] results = new String[2];

        Thread t1 = new Thread(() -> results[0] = Main_controller.getMessage());
        Thread t2 = new Thread(() -> results[1] = Main_controller.getMessage());

        t1.start();
        t2.start();
        
        t1.join();
        t2.join();

        //Only one thread should have retrieved the message, the other should get null
        boolean onlyOneGotIt = ("ThreadTest".equals(results[0]) && results[1] == null) || ("ThreadTest".equals(results[1]) && results[0] == null);
        assertTrue(onlyOneGotIt);
    }

    @Test //Setting a new coordinator overwrites the old one
    void testSetCoordinatorOverwritesPrevious() {
        Gui gui = new Gui();
        Main_controller.set_coordinator("Hugo");
        Main_controller.set_coordinator("Amin");
        assertEquals("Amin", gui.coordinatorUsername);
    }

    @Test //tests to ensure invalid ports aren't considered as valid.
    void testIfInvalidPortIsCaught(){
        assertDoesNotThrow(() -> Main_controller.joinServerButtonPressed("127.0.0.1", 1052005, "Hugoooooo"));
    }
}
