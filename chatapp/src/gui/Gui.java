/*
Responsible for displaying all the information to the user via a GUI.
Controlled by the Main_controller.java script.

BY:
AMIN SHEIKH
KIPP SUMMERS
HUGO PIPER
DHARI ALTHUNAYAN
JAHID EMON
*/

package gui;

//We dont need to import the controller because we used its full package name every time.

// All imports
import java.awt.*;            // For colors fonts graphics etc.
import java.awt.event.*;      // For events control like clicks, button action etc
import java.util.ArrayList;   // Arraylist is dynamic , helps to store message
import java.util.List;
import javax.swing.*;               // Helps with the GUI Components
import javax.swing.border.*;        // Tools for drawing borders around panels
import javax.swing.plaf.basic.*;    // Helps to redesign
import java.awt.image.BufferedImage; // Used for Speech buble width sizing

public class Gui {

    //static reference to the GUI object
    private static Gui instance;

    public Gui() {
        instance = this;
    }

    //Returns the current GUI instance, or null if the GUI has not yet been created.  The controller uses this to deliver messages.
    public static Gui getInstance() {
        return instance;
    }

    // SECTION 1 - THEME COLOURS
    // Change colour here to change the whole application Color theme

    // Background colors
    static final Color BG_MAIN = new Color(0x0E1117);        // main window
    static final Color BG_CHAT = new Color(0x161B26);       // chat area
    static final Color BG_INPUT = new Color(0x1C2333);     // input bar
    static final Color BG_SIDEBAR = new Color(0x111827);  // sidebar
    static final Color BG_NAV = new Color(0x0A0D14);     // top nav bar
    // For Borders and accent
    static final Color COL_BORDER = new Color(0x2A3347);     // panel borders
    static final Color COL_ACCENT = new Color(0x00BCD4);    // teal accent colour
    // For Text
    static final Color COL_TEXT = new Color(0xE2E8F0);       // normal text
    static final Color COL_HINT = new Color(0x64748B);      // grey hint / label text
    // For System Notifications
    static final Color COL_ONLINE = new Color(0x22C55E);   // green online dot
    static final Color COL_EXIT = new Color(0xEF4444);    // red (leave / close)
    static final Color COL_INFO = new Color(0xFBBF24);   // yellow system messages
    // For Chat bubbles
    static final Color BUBBLE_IN = new Color(0x1E2D40);   // incoming bubble (left)
    static final Color BUBBLE_OUT = new Color(0x0D3640); // outgoing bubble (right)


    // SECTION 2 - Fixed FONTS
    static final Font FONT_NORMAL = new Font("SANS_SERIF", Font.PLAIN, 13); // Reg Text
    static final Font FONT_BOLD = new Font("SANS_SERIF", Font.BOLD, 13);    // Buttons labels, Sender names
    static final Font FONT_SMALL = new Font("SANS_SERIF", Font.PLAIN, 11);  // Small taglines like usages and username, port etc
    static final Font FONT_TITLE = new Font("SANS_SERIF", Font.BOLD, 22);   // For logo


    // SECTION 3 - CONSTANTS
    // Bubbles grow to at most 65%
    static final double BUBBLE_MAX_FRACTION = 0.65;


    //  SECTION 4 - APP STATE
    //  Variables that hold state

    // used to define which mode user choose
    // true = Create Server, false = Join Server
    boolean isHostMode = false;

    // What username did the user type?
    String username = "TypedUsername";

    //Default server port if a user creates a server without entering a port.
    int serverPort = 7000;

    // GUI components we need to reference from multiple methods
    JFrame MainChatWindow;                     // the main chat window (screen 3)
    JFrame serverWindow;                       // The server GUI window
    JDialog connectDialog;                     // The 'connect to server' window.
    JPanel bubbleContainer;                    // panel that holds all chat bubbles
    JScrollPane chatScroll;                    // scroll pane around bubbleContainer
    DefaultListModel<String> onlineUsersModel; // For online user list
    JTextField MessageTypingBox;               // the message input box
    JButton sendBtn;                           // the send button
    List<UserInfo> pendingUserList = null;     // Cached list of users
    public String coordinatorTimeZone = "UTC";        // Defaults the coordinators time zone to UTC if none is found.

    //Used to open each window where the last window was positioned.
    Point lastWindowPosition = null;

    //Used to determine if we're at the main menu.
    boolean goToMainMenuAfterClose = false;

    // === USER INFO FEATURE ===
    // Per-user info lookup map — populated when users join
    java.util.Map<String, UserInfo> userInfoMap = new java.util.HashMap<>();

    // The coordinator's username
    public String coordinatorUsername = "";

    // Port and IP captured from the connect screen
    String sessionPort = "7000";
    String sessionIp   = "127.0.0.1";
    
    // Every message that has been sent, we store it for future use
    final List<Message> messageHistory = new ArrayList<>();


    //  SECTION 5 - MESSAGE CLASS
    //  One Message object created for every chat bubble or system notice.

    static class Message {

        String text;      // what text
        boolean outgoing; // true = outgoing (right side), false = incoming (left side)
        String sender;    // who sent it (username) (null for system messages)
        boolean isSystem; // true = yellow system notice for notification broadcast
        String timestamp; // Holds the coordinators timestamp.

        Message(String text, boolean outgoing, String sender, boolean isSystem, String timestamp) {
            this.text = text;
            this.outgoing = outgoing;
            this.sender = sender;
            this.isSystem = isSystem;
            this.timestamp = timestamp;
        }
    }

    // === USER INFO CLASS ===
    // Holds connection details for each user shown in the sidebar.
    // Populated by the controller when users join/leave.
    public static class UserInfo {
        String username;
        String ip;
        String port;
        boolean isCoordinator;

        public UserInfo(String username, String ip, String port, boolean isCoordinator) {
            this.username      = username;
            this.ip            = ip;
            this.port          = port;
            this.isCoordinator = isCoordinator;
        }
    }

    // SECTION 6 - THEME INJECTION
    // Our applyTheme() method takes "Themes color" from section 01 and then we
    // Apply it to Swing Default GUI toolkit to get advance theme management of Buttons, Label, text field

    static void applyTheme() {

        // Dialog and panel backgrounds (we change from default to dark navy)
        UIManager.put("OptionPane.background", BG_CHAT);
        UIManager.put("Panel.background", BG_CHAT);
        UIManager.put("OptionPane.messageForeground", COL_TEXT);

        // Labels
        UIManager.put("Label.foreground", COL_TEXT);
        UIManager.put("Label.font", FONT_NORMAL);

        // Text fields
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", COL_TEXT);
        UIManager.put("TextField.caretForeground", COL_ACCENT);
        UIManager.put("TextField.font", FONT_NORMAL);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));

        // Buttons
        UIManager.put("Button.background", BG_INPUT);
        UIManager.put("Button.foreground", COL_TEXT);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Button.border", new LineBorder(COL_BORDER, 1, true));

        // Text areas (used inside chat bubbles)
        UIManager.put("TextArea.background", BG_CHAT);
        UIManager.put("TextArea.foreground", COL_TEXT);
        UIManager.put("TextArea.font", FONT_NORMAL);

        // Sidebar list (using it for Online user list)
        UIManager.put("List.background", BG_SIDEBAR);
        UIManager.put("List.foreground", COL_TEXT);
        UIManager.put("List.font", FONT_NORMAL);
        UIManager.put("List.selectionBackground", COL_ACCENT);
        UIManager.put("List.selectionForeground", Color.WHITE);

        // Scroll panes
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }


    // SECTION 7 - REUSABLE COMPONENTS
    // For Button, Icon, Scrollbar, Card, LabelField etc.

    // [ makePillButton() - METHOD {01} FOR BUTTON ]
    static JButton makePillButton(String label, Color color, int w, int h) {

        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(getModel().isRollover() ? color.brighter() : color);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                graphics.setColor(Color.WHITE);
                graphics.setFont(FONT_BOLD);
                FontMetrics fm = graphics.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(label)) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                graphics.drawString(label, textX, textY);
                graphics.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // [ makeIconButton() - METHOD {02} For TopNav Button Customization ]
    static JButton makeIconButton(String symbol, Color color, ActionListener action) {

        JButton btn = new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded fill on hover
                if (getModel().isRollover()) {
                    graphics.setColor(new Color(color.getRed(), color.getGreen(),
                            color.getBlue(), 60));
                    graphics.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                }

                // Permanent rounded border
                graphics.setColor(new Color(color.getRed(), color.getGreen(),
                        color.getBlue(), getModel().isRollover() ? 180 : 80));
                graphics.setStroke(new BasicStroke(1f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                // Symbol
                graphics.setColor(color);
                graphics.setFont(FONT_BOLD);
                FontMetrics fm = graphics.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(symbol)) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                graphics.drawString(symbol, textX, textY);
                graphics.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.addActionListener(action);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // [ makeScrollBar() - METHOD {03} TO CREATE THE VERTICAL READING SCROLLBAR ]
    static void makeScrollBar(JScrollPane sp) {

        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(4, 0));
        bar.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0x3A4A60);
                trackColor = BG_CHAT;
            }

            private JButton noButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createDecreaseButton(int o) { return noButton(); }

            @Override
            protected JButton createIncreaseButton(int o) { return noButton(); }
        });
    }


    // [ makeNavBar() - METHOD {04} SHARED NAV BAR FOR BOTH WINDOWS ]
    JPanel makeNavBar(JFrame frame, String rightLabel, Rectangle[] savedBounds) {
        JLabel appLogo = new JLabel("\u23FF");
        appLogo.setFont(FONT_BOLD.deriveFont(13f));
        appLogo.setForeground(COL_ACCENT);

        JLabel appName = new JLabel("JAVACHAT");
        appName.setFont(FONT_BOLD.deriveFont(14f));
        appName.setForeground(COL_ACCENT);

        JLabel rightLbl = new JLabel(rightLabel);
        rightLbl.setFont(FONT_NORMAL);
        rightLbl.setForeground(COL_TEXT);

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        navLeft.setOpaque(false);
        navLeft.add(appLogo);
        navLeft.add(appName);
        navLeft.add(rightLbl);

        JButton minBtn = makeIconButton("\u2212", COL_HINT, e -> frame.setState(Frame.ICONIFIED));
        JButton maxBtn = makeIconButton("\u25A1", COL_HINT, e -> {
            if (savedBounds[0] != null) {
                Rectangle r = savedBounds[0];
                savedBounds[0] = null;
                animateWindow(frame, r.x, r.y, r.width, r.height);
            } else {
                savedBounds[0] = frame.getBounds();
                GraphicsConfiguration gc = frame.getGraphicsConfiguration();
                Rectangle s = gc.getBounds();
                Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                animateWindow(frame, s.x + ins.left, s.y + ins.top,
                        s.width - ins.left - ins.right, s.height - ins.top - ins.bottom);
            }
        });
        JButton closeBtn = makeIconButton("\u2715", COL_EXIT, e -> exitApp());

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        navRight.setOpaque(false);
        navRight.add(minBtn);
        navRight.add(maxBtn);
        navRight.add(closeBtn);

        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(navLeft);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(navRight);

        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COL_BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        navBar.setBackground(BG_NAV);
        navBar.setPreferredSize(new Dimension(0, 46));
        navBar.setBorder(new EmptyBorder(0, 14, 0, 10));
        navBar.add(leftWrapper,  BorderLayout.WEST);
        navBar.add(rightWrapper, BorderLayout.EAST);

        Point[] dragStart = {null};
        navBar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) maxBtn.doClick(); }
        });
        navBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (savedBounds[0] != null) return;
                Point pos = frame.getLocation();
                frame.setLocation(pos.x + e.getX() - dragStart[0].x, pos.y + e.getY() - dragStart[0].y);
            }
        });

        return navBar;
    }

    // [ makeRootPanel() - METHOD {05} SHARED THEMED ROOT PANEL ]
    static JPanel makeRootPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MAIN);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(COL_ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        panel.setOpaque(true);
        panel.setBackground(BG_MAIN);
        return panel;
    }

    // [ makeFrame() - METHOD {06} SHARED UNDECORATED FRAME SETUP ]
    static JFrame makeFrame(WindowAdapter onClose) {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setOpacity(1.0f);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(onClose);
        return frame;
    }

    // [ makeDialogNavBar() - METHOD {07} NAV BAR FOR DIALOGS (no maximise) ]
    // Wires drag-to-move onto the given root panel, returns the nav bar to add at NORTH.
    static JPanel makeDialogNavBar(JDialog dialog, String title, JPanel root) {
        JLabel appLogo = new JLabel("\u23FF");
        appLogo.setFont(FONT_BOLD.deriveFont(13f));
        appLogo.setForeground(COL_ACCENT);

        JLabel appName = new JLabel("JAVACHAT");
        appName.setFont(FONT_BOLD.deriveFont(14f));
        appName.setForeground(COL_ACCENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_NORMAL);
        titleLbl.setForeground(COL_HINT);

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        navLeft.setOpaque(false);
        navLeft.add(appLogo);
        navLeft.add(appName);
        navLeft.add(titleLbl);

        JButton minBtn   = makeIconButton("\u2212", COL_HINT,  e -> dialog.setVisible(false));
        JButton closeBtn = makeIconButton("\u2715", COL_EXIT,  e -> dialog.dispose());

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        navRight.setOpaque(false);
        navRight.add(minBtn);
        navRight.add(closeBtn);

        // Wrap in GridBagLayout so both sides center vertically in the 46px bar
        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(navLeft);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(navRight);

        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COL_BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        navBar.setBackground(BG_NAV);
        navBar.setPreferredSize(new Dimension(0, 46));
        navBar.setBorder(new EmptyBorder(0, 14, 0, 10));
        navBar.add(leftWrapper,  BorderLayout.WEST);
        navBar.add(rightWrapper, BorderLayout.EAST);

        // Drag to move using the nav bar
        Point[] dragStart = {null};
        navBar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        navBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(pos.x + e.getX() - dragStart[0].x,
                                   pos.y + e.getY() - dragStart[0].y);
            }
        });

        return navBar;
    }

    // [ makeLabelledField() - METHOD {05} TO CREATE INPUT LABEL ]
    static JPanel makeLabelledField(String caption, JTextField field) {

        JLabel label = new JLabel(caption);
        label.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        label.setForeground(COL_HINT);

        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        row.add(label, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // [ makeModeCard() - METHOD {05} THAT CREATES C OR J SERVER CARD ]
    static JPanel makeModeCard(String icon, String title,
                               String smallTextDetails, Runnable onClick) {

        JPanel card = new JPanel(new GridLayout(3, 1, 0, 6)) {

            boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }

                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }

                    @Override
                    public void mouseClicked(MouseEvent e) { onClick.run(); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(hovered ? new Color(0x232D42) : BG_INPUT);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                graphics.setColor(hovered ? COL_ACCENT : COL_BORDER);
                graphics.setStroke(new BasicStroke(1.5f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                graphics.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 28));
        iconLabel.setForeground(COL_ACCENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FONT_BOLD);
        titleLabel.setForeground(COL_TEXT);

        JLabel STDOnCard = new JLabel(smallTextDetails, SwingConstants.CENTER);
        STDOnCard.setFont(FONT_SMALL);
        STDOnCard.setForeground(COL_HINT);

        card.add(iconLabel);
        card.add(titleLabel);
        card.add(STDOnCard);
        return card;
    }


    //  SECTION 8 - WINDOW 1 LAUNCH DIALOG
    int showLaunchScreen() {

        int[] choice = {-1};

        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);
        dialog.setSize(480, 380);
        if (lastWindowPosition != null) dialog.setLocation(lastWindowPosition);
        else dialog.setLocationRelativeTo(null);

        JLabel logo = new JLabel("JAVACHAT", SwingConstants.CENTER);
        logo.setFont(FONT_TITLE);
        logo.setForeground(COL_ACCENT);

        JLabel tagline1 = new JLabel("A Custom Client-Server Chat Application",
                SwingConstants.CENTER);
        tagline1.setFont(FONT_SMALL);
        tagline1.setForeground(COL_HINT);

        JLabel tagline2 = new JLabel("Made by: Hugo, Amin, Kipp, Emon & Dhari",
                SwingConstants.CENTER);
        tagline2.setFont(FONT_SMALL);
        tagline2.setForeground(COL_HINT);

        JPanel taglines = new JPanel(new GridLayout(2, 1, 0, 3));
        taglines.setOpaque(false);
        taglines.add(tagline1);
        taglines.add(tagline2);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 6));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        header.add(logo);
        header.add(taglines);

        JPanel createCard = makeModeCard(
                "\u2338",
                "Create Server",
                "Host a new room",
                () -> { choice[0] = 0; dialog.dispose(); }
        );

        JPanel joinCard = makeModeCard(
                "\u2192",
                "Join Server",
                "Enter a room",
                () -> { choice[0] = 1; dialog.dispose(); }
        );

        JPanel cards = new JPanel(new GridLayout(1, 2, 14, 0));
        cards.setOpaque(false);
        cards.add(createCard);
        cards.add(joinCard);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 32, 28, 32));
        content.add(header, BorderLayout.NORTH);
        content.add(cards,  BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_CHAT);
        root.setBorder(new LineBorder(COL_BORDER, 1, true));
        root.add(makeDialogNavBar(dialog, "Welcome", root), BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);

        dialog.add(root);
        dialog.setVisible(true);
        lastWindowPosition = dialog.getLocation();
        return choice[0];
    }


    //  SECTION 9 - SCREEN 2: CONNECT DIALOG
    boolean showConnectScreen(int mode) {

        boolean isCreating = (mode == 0);
        isHostMode = isCreating;

        connectDialog = new JDialog((Frame) null, true);
        JDialog dialog = connectDialog;
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        JTextField usernameField = new JTextField(16);
        JTextField portField     = new JTextField("7000", 16);
        JTextField ipField       = new JTextField("127.0.0.1", 16);

        JLabel iconLabel = new JLabel(
                isCreating ? "\u2338" : "\u2192",
                SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 32));
        iconLabel.setForeground(COL_ACCENT);
        iconLabel.setAlignmentX(0.5f);

        JLabel titleLabel = new JLabel(
                isCreating ? "Create Server" : "Join Server",
                SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COL_TEXT);
        titleLabel.setAlignmentX(0.5f);

        JLabel subtitleLabel = new JLabel(
                isCreating ? "Configure your server" : "Connect to a server",
                SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(COL_HINT);
        subtitleLabel.setAlignmentX(0.5f);

        JLabel greenDot = new JLabel("\u25CF");
        greenDot.setForeground(COL_ONLINE);
        greenDot.setFont(FONT_SMALL);

        JLabel hostIp = new JLabel("Hosting on: " + getLocalIPv4());
        hostIp.setForeground(COL_ACCENT);
        hostIp.setFont(FONT_BOLD);

        JPanel hostBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        hostBadge.setBackground(new Color(0x0D2A2E));
        hostBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x1A4A50), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        hostBadge.add(greenDot);
        hostBadge.add(hostIp);
        hostBadge.setAlignmentX(0.5f);
        hostBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                hostBadge.getPreferredSize().height));

        int numRows = isCreating ? 1 : 3;
        JPanel form = new JPanel(new GridLayout(numRows, 1, 0, 14));
        form.setBackground(BG_INPUT);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        if (!isCreating) {
            form.add(makeLabelledField("SERVER IP", ipField));
            form.add(makeLabelledField("USERNAME - (1-20 chars)", usernameField));
        }
        form.add(makeLabelledField("PORT - (1 to 65535)", portField));
        form.setAlignmentX(0.5f);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                form.getPreferredSize().height));

        JButton backBtn = makePillButton("BACK", new Color(0x374151), 100, 38);

        JButton proceedBtn = makePillButton(
                isCreating ? "CREATE  \u25B6" : "JOIN  \u25B6",
                COL_ACCENT, 130, 38);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(backBtn);
        buttonRow.add(proceedBtn);
        buttonRow.setAlignmentX(0.5f);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel navBar = makeDialogNavBar(dialog, isCreating ? "Create Server" : "Join Server", root);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(20, 32, 24, 32)));
        body.add(iconLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(titleLabel);
        body.add(Box.createVerticalStrut(6));
        body.add(subtitleLabel);
        body.add(Box.createVerticalStrut(16));
        if (isCreating) {
            body.add(hostBadge);
            body.add(Box.createVerticalStrut(12));
        }
        body.add(form);
        body.add(Box.createVerticalStrut(20));
        body.add(buttonRow);

        root.add(navBar);
        root.add(body);

        boolean[] submitted = {false};
        boolean[] wentBack  = {false};

        proceedBtn.addActionListener(e -> {
            String typedIP = ipField.getText().trim();
            String typedUsername = isCreating ? "Host" : usernameField.getText().trim();
            String typedPort = portField.getText().trim();

            // Validate username
            if (!isCreating && (typedUsername.isEmpty() || typedUsername.length() > 20)) {
                showErrorPopup("Username must be between 1 and 20 characters.");
                return;
            }

            // Validate port is a number
            int intPort;
            try {
                intPort = Integer.parseInt(typedPort);
            } catch (NumberFormatException exc) {
                showErrorPopup("Port must be a number.");
                return;
            }

            // Validate port range
            if (intPort < 1 || intPort > 65535) {
                showErrorPopup("Port must be between 1 and 65535.");
                return;
            }

            // All valid — proceed
            username    = typedUsername;
            sessionPort = typedPort;
            sessionIp   = isCreating ? getLocalIPv4() : typedIP;
            serverPort  = intPort;
            submitted[0] = true;
            dialog.dispose();

            if (isCreating) {
                controller.Main_controller.createServerButtonPressed(intPort);
            } else {
                controller.Main_controller.joinServerButtonPressed(typedIP, intPort, typedUsername);
            }
        });

        ActionListener pressEnter = e -> proceedBtn.doClick();
        usernameField.addActionListener(pressEnter);
        portField.addActionListener(pressEnter);
        ipField.addActionListener(pressEnter);

        backBtn.addActionListener(e -> {
            wentBack[0] = true;
            dialog.dispose();
        });

        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, dialog.getHeight()));
        if (lastWindowPosition != null) dialog.setLocation(lastWindowPosition);
        else dialog.setLocationRelativeTo(null);

        dialog.setVisible(true); //Blocks here until dialog is closed

        if (wentBack[0] || goToMainMenuAfterClose) {
            goToMainMenuAfterClose = false;
            int newMode = showLaunchScreen();
            if (newMode == -1) { exitApp(); return false; }
            boolean proceed = showConnectScreen(newMode);
            if (proceed) {
                if (isHostMode) showServerWindow();
                else showChatWindow();
            }
            return false;
        }

        if (!submitted[0]) return false;
        return true;
    }


    //  SECTION 10A - SCREEN 3 MAIN CHAT WINDOW
    void showChatWindow() {

        // SUBSECT {01} - CREATES WINDOW
        MainChatWindow = makeFrame(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                lastWindowPosition = MainChatWindow.getLocation();
                exitApp();
            }
        });

        // SUBSECT {02} - FOR NAV BAR
        Rectangle[] savedBounds = { null };
        String displayName = username.length() > 12 ? username.substring(0, 12) + "..." : username;
        JPanel navBar = makeNavBar(MainChatWindow, "@" + displayName, savedBounds);


        // SUBSECT {03} - CREATES CHAT BUBBLE AREA
        bubbleContainer = new JPanel();
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.Y_AXIS));
        bubbleContainer.setBackground(BG_CHAT);
        bubbleContainer.setBorder(new EmptyBorder(14, 16, 14, 16));

        bubbleContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                bubbleContainer.revalidate();
                bubbleContainer.repaint();
            }
        });

        JPanel bubbleWrapper = new JPanel(new BorderLayout());
        bubbleWrapper.setBackground(BG_CHAT);
        bubbleWrapper.add(bubbleContainer, BorderLayout.NORTH);

        chatScroll = new JScrollPane(bubbleWrapper);
        chatScroll.setBackground(BG_CHAT);
        chatScroll.getViewport().setBackground(BG_CHAT);
        chatScroll.setBorder(new MatteBorder(1, 0, 0, 0, COL_BORDER));
        makeScrollBar(chatScroll);

        // SUBSECT {04} - USERLIST
        onlineUsersModel = new DefaultListModel<>();

        JList<String> userList = new JList<>(onlineUsersModel);
        userList.setFixedCellHeight(28);
        userList.setBorder(new EmptyBorder(4, 10, 4, 8));

        // === USER INFO FEATURE: hover highlight tracking ===
        final int[] hoverIndex = {-1};
        userList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int idx = userList.locationToIndex(e.getPoint());
                Rectangle cellBounds = (idx >= 0) ? userList.getCellBounds(idx, idx) : null;
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) {
                    idx = -1;
                }
                if (idx != hoverIndex[0]) {
                    hoverIndex[0] = idx;
                    userList.repaint();
                }
            }
        });
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverIndex[0] = -1;
                userList.repaint();
            }
        });

        // Custom renderer for green dot + username + hover border
        userList.setCellRenderer((list, value, index, selected, focused) -> {
            JLabel dot = new JLabel("\u25CF");
            dot.setForeground(COL_ONLINE);
            dot.setFont(FONT_SMALL);

            String n = value.toString();
            String trimmed = n.length() > 12 ? n.substring(0, 12) + "..." : n;
            JLabel name = new JLabel(trimmed);
            
            name.setFont(FONT_NORMAL);
            name.setForeground(COL_TEXT);

            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            cell.setOpaque(false);

            // === USER INFO FEATURE: teal border highlight on hover ===
            if (index == hoverIndex[0]) {
                cell.setBorder(new LineBorder(COL_ACCENT, 1, true));
            }
            cell.add(dot);
            cell.add(name);
            return cell;
        });

        // === USER INFO FEATURE: single-click opens user info popup ===
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    String clicked = onlineUsersModel.getElementAt(index);
                    showUserInfoPopup(clicked);
                }
            }
        });

        // Sidebar header
        JLabel onlineHeader = new JLabel("ONLINE");
        onlineHeader.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        onlineHeader.setForeground(COL_HINT);
        onlineHeader.setBackground(BG_SIDEBAR);
        onlineHeader.setOpaque(true);
        onlineHeader.setBorder(new EmptyBorder(12, 14, 8, 8));

        JScrollPane sideScroll = new JScrollPane(userList);
        sideScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        makeScrollBar(sideScroll);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 1, 0, 0, COL_BORDER));
        sidebar.setPreferredSize(new Dimension(130, 0));
        sidebar.add(onlineHeader, BorderLayout.NORTH);
        sidebar.add(sideScroll,   BorderLayout.CENTER);

        JPanel chatAndSidebar = new JPanel(new BorderLayout());
        chatAndSidebar.setOpaque(false);
        chatAndSidebar.add(chatScroll, BorderLayout.CENTER);
        chatAndSidebar.add(sidebar,    BorderLayout.EAST);


        // SUBSECT {05} - MESSAGE INPUT BAR
        MessageTypingBox = new JTextField();
        MessageTypingBox.setBackground(BG_INPUT);
        MessageTypingBox.setForeground(COL_TEXT);
        MessageTypingBox.setCaretColor(COL_ACCENT);
        MessageTypingBox.setFont(FONT_NORMAL);
        MessageTypingBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x3A4A60), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        sendBtn = makePillButton("SEND \u25B6", COL_ACCENT, 100, 38);

        JButton leaveBtn = makePillButton("LEAVE", COL_EXIT, 80, 38);

        ActionListener sendAction = e -> {
            String text = MessageTypingBox.getText().trim();
            if (!text.isEmpty()) {
                String timestamp = java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(java.time.ZonedDateTime.now(java.time.ZoneId.of(coordinatorTimeZone)));
                addMessage(text, true, username, timestamp);
                controller.Main_controller.message_sent_from_user(text);
                MessageTypingBox.setText("");
            }
        };
        sendBtn.addActionListener(sendAction);
        MessageTypingBox.addActionListener(sendAction);

        leaveBtn.addActionListener(e -> exitApp());

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(sendBtn);
        btnGroup.add(leaveBtn);

        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(BG_INPUT);
        inputBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COL_BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        inputBar.add(MessageTypingBox, BorderLayout.CENTER);
        inputBar.add(btnGroup,         BorderLayout.EAST);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.add(inputBar, BorderLayout.CENTER);

        // SUBSECT {06} - ROOT ASSEMBLY
        JPanel rootPanel = makeRootPanel();
        rootPanel.add(navBar,         BorderLayout.NORTH);
        rootPanel.add(chatAndSidebar, BorderLayout.CENTER);
        rootPanel.add(bottomBar,      BorderLayout.SOUTH);

        MainChatWindow.setContentPane(rootPanel);
        MainChatWindow.setSize(700, 520);
        MainChatWindow.setMinimumSize(new Dimension(480, 360));
        if (lastWindowPosition != null) MainChatWindow.setLocation(lastWindowPosition);
        else MainChatWindow.setLocationRelativeTo(null);
        addResizeSupport(MainChatWindow, rootPanel);
        MainChatWindow.setVisible(true);
        lastWindowPosition = MainChatWindow.getLocation();

        // Seed own user info and add to sidebar
        coordinatorUsername = isHostMode ? username : "";
        userInfoMap.put(username, new UserInfo(username, sessionIp, sessionPort, isHostMode));
        onlineUsersModel.addElement(username);
        if (pendingUserList != null) {
            updateOnlineUsers(pendingUserList);
            pendingUserList = null;
        }
        MessageTypingBox.requestFocusInWindow();
    }


    //  SECTION 10B - SERVER MONITOR WINDOW
    void showServerWindow() {

        serverWindow = new JFrame();
        serverWindow.setUndecorated(true);
        serverWindow.setBackground(new Color(0, 0, 0, 0));
        serverWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        serverWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exitApp(); }
        });

        Rectangle[] srvSavedBounds = { null };

        // --- NAV BAR ---
        JLabel appLogo = new JLabel("\u23FF");
        appLogo.setFont(FONT_BOLD.deriveFont(13f));
        appLogo.setForeground(COL_ACCENT);

        JLabel appName = new JLabel("JAVACHAT");
        appName.setFont(FONT_BOLD.deriveFont(14f));
        appName.setForeground(COL_ACCENT);

        JLabel modeLabel = new JLabel("SERVER MODE");
        modeLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        modeLabel.setForeground(COL_HINT);

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        navLeft.setOpaque(false);
        navLeft.add(appLogo);
        navLeft.add(appName);
        navLeft.add(modeLabel);

        JButton minimiseBtn = makeIconButton("\u2212", COL_HINT,
                e -> serverWindow.setState(Frame.ICONIFIED));

        JButton maxBtn = makeIconButton("\u25A1", COL_HINT, e -> {
            if (srvSavedBounds[0] != null) {
                Rectangle r = srvSavedBounds[0];
                srvSavedBounds[0] = null;
                animateWindow(serverWindow, r.x, r.y, r.width, r.height);
            } else {
                srvSavedBounds[0] = serverWindow.getBounds();
                GraphicsConfiguration gc = serverWindow.getGraphicsConfiguration();
                Rectangle s = gc.getBounds();
                Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                animateWindow(serverWindow, s.x + ins.left, s.y + ins.top,
                        s.width - ins.left - ins.right, s.height - ins.top - ins.bottom);
            }
        });
        JButton closeBtn = makeIconButton("\u2715", COL_EXIT,
                e -> exitApp());

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        navRight.setOpaque(false);
        navRight.add(minimiseBtn);
        navRight.add(maxBtn);
        navRight.add(closeBtn);

        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(navLeft);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(navRight);

        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COL_BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        navBar.setBackground(BG_NAV);
        navBar.setPreferredSize(new Dimension(0, 46));
        navBar.setBorder(new EmptyBorder(0, 14, 0, 10));
        navBar.add(leftWrapper,  BorderLayout.WEST);
        navBar.add(rightWrapper, BorderLayout.EAST);

        Point[] dragStart = {null};
        navBar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        navBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point pos = serverWindow.getLocation();
                serverWindow.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        // --- STATUS BADGE (IP + PORT) ---
        JLabel dotLabel = new JLabel("\u25CF");
        dotLabel.setForeground(COL_ONLINE);
        dotLabel.setFont(FONT_SMALL);

        JLabel ipPortLabel = new JLabel("IP: " + getLocalIPv4() + "                PORT: " + serverPort);
        ipPortLabel.setForeground(COL_ACCENT);
        ipPortLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        badge.setBackground(new Color(0x0D2A2E));
        badge.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(6, 14, 6, 14)));
        badge.add(dotLabel);
        badge.add(ipPortLabel);

        // --- LOG AREA ---
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(BG_CHAT);
        logArea.setForeground(new Color(0x9DFFB0));
        logArea.setCaretColor(COL_ACCENT);
        logArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBackground(BG_CHAT);
        logScroll.getViewport().setBackground(BG_CHAT);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        makeScrollBar(logScroll);

        java.io.OutputStream logStream = new java.io.OutputStream() {
            @Override
            public void write(int b) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append(String.valueOf((char) b));
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
            @Override
            public void write(byte[] buf, int off, int len) {
                String text = new String(buf, off, len);
                SwingUtilities.invokeLater(() -> {
                    logArea.append(text);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
        };
        System.setOut(new java.io.PrintStream(logStream, true));

        JLabel logHeader = new JLabel("  CONSOLE OUTPUT");
        logHeader.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        logHeader.setForeground(COL_HINT);
        logHeader.setBackground(BG_SIDEBAR);
        logHeader.setOpaque(true);
        logHeader.setBorder(new EmptyBorder(8, 14, 8, 8));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setOpaque(false);
        logPanel.add(logHeader, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        // --- ROOT ASSEMBLY ---
        JPanel rootPanel = makeRootPanel();

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(navBar, BorderLayout.NORTH);
        topSection.add(badge,  BorderLayout.CENTER);

        rootPanel.add(topSection, BorderLayout.NORTH);
        rootPanel.add(logPanel,   BorderLayout.CENTER);

        serverWindow.setContentPane(rootPanel);
        serverWindow.setSize(420, 420);
        serverWindow.setMinimumSize(new Dimension(420, 300));
        addResizeSupport(serverWindow, rootPanel);
        if (lastWindowPosition != null) serverWindow.setLocation(lastWindowPosition);
        else serverWindow.setLocationRelativeTo(null);
        serverWindow.setVisible(true);
        lastWindowPosition = serverWindow.getLocation();
    }


    // SECTION 11 - CHAT BUBBLE RENDERING

    int getBubbleMaxWidth() {
        int panelWidth = (bubbleContainer != null) ? bubbleContainer.getWidth() : 260;
        int usable = Math.max(panelWidth - 52, 80);
        return Math.min((int) (usable * BUBBLE_MAX_FRACTION), 700);
    }

    int getBubbleTextWidth(String text, Font font, int maxWidth) {

    FontMetrics fm;

    if (bubbleContainer != null) {
        fm = bubbleContainer.getFontMetrics(font);
    } else {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        fm = g2.getFontMetrics(font);
        g2.dispose();
    }

    String[] lines = text.split("\n");
    int longestLine = 0;

    for (String line : lines) {
        longestLine = Math.max(longestLine, fm.stringWidth(line));
    }

    int padded = longestLine + 24;

    return Math.max(90, Math.min(padded, maxWidth));
}

    void addMessage(String text, boolean outgoing, String sender, String timestamp) {
        messageHistory.add(new Message(text, outgoing, sender, false, timestamp));
        drawBubble(text, outgoing, sender, timestamp);
    }

    void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = chatScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        });
    }

    void drawBubble(String text, boolean outgoing, String sender, String timestamp) {

        JLabel senderLabel = new JLabel("@" + sender);
        senderLabel.setFont(FONT_BOLD.deriveFont(11f));
        senderLabel.setForeground(COL_ACCENT);
        senderLabel.setBorder(new EmptyBorder(2, 4, 2, 4));

        //Draws the timestamp text under the users message.
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(FONT_SMALL);
        timeLabel.setForeground(COL_HINT);
        timeLabel.setBorder(new EmptyBorder(2, 4, 4, 4));

        JPanel bubble = new JPanel(new BorderLayout()) {

            private JTextArea ta;

            {
                ta = new JTextArea(text);
                ta.setEditable(false);
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                ta.setFont(FONT_NORMAL);
                ta.setOpaque(false);
                ta.setForeground(COL_TEXT);
                ta.setBorder(new EmptyBorder(9, 12, 4, 12));
                setOpaque(false);
                add(ta, BorderLayout.CENTER);
            }

            @Override
            public Dimension getPreferredSize() {
                int containerWidth = (bubbleContainer != null && bubbleContainer.getWidth() > 0)
                    ? bubbleContainer.getWidth() : 300;
                int maxW = (int) Math.min(containerWidth * BUBBLE_MAX_FRACTION, 700);

                FontMetrics fm = ta.getFontMetrics(FONT_NORMAL);
                String[] lines = text.split("\n");
                int naturalTextW = 0;
                for (String line : lines) naturalTextW = Math.max(naturalTextW, fm.stringWidth(line));
                int naturalW = Math.min(naturalTextW + 30, maxW);

                int targetW = Math.max(naturalW, 90);

                //Added a small safety margin so the rounded stroke never sits exactly on the component edge. This only seems to be a Windows issue for some reason.
                final int SAFETY_MARGIN = 6;
                int finalW = targetW + SAFETY_MARGIN;

                //Let the text area calculate height for the inner width (subtract ta insets already included)
                ta.setSize(new Dimension(finalW, Integer.MAX_VALUE));
                int h = ta.getPreferredSize().height;

                //Add a small vertical safety margin too to avoid vertical cropping. Only an issue on Windows.
                int finalH = h + SAFETY_MARGIN;

                return new Dimension(finalW, finalH);
            }

            @Override
            public Dimension getMinimumSize() { return getPreferredSize(); }
            @Override
            public Dimension getMaximumSize() { return getPreferredSize(); }

            @Override
            protected void paintComponent(Graphics g) {
                Color bgCol     = outgoing ? BUBBLE_OUT : BUBBLE_IN;
                Color borderCol = outgoing ? COL_ACCENT : COL_BORDER;
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // inset the drawing so stroke/antialiasing doesn't get clipped by parent
                int inset = 2;
                int w = getWidth() - inset * 2;
                int h = getHeight() - inset * 2;
                int arc = 14;

                if (w > 0 && h > 0) {
                    graphics.setColor(bgCol);
                    graphics.fillRoundRect(inset, inset, w, h, arc, arc);
                    graphics.setColor(borderCol);
                    graphics.setStroke(new BasicStroke(1f));
                    graphics.drawRoundRect(inset, inset, w - 1, h - 1, arc, arc);
                }
                graphics.dispose();
            }
        };

        float align = outgoing ? 1.0f : 0.0f;
        senderLabel.setAlignmentX(align);
        bubble.setAlignmentX(align);
        timeLabel.setAlignmentX(align);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(senderLabel);
        stack.add(bubble);
        stack.add(timeLabel);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(0f);
        row.add(stack, outgoing ? BorderLayout.EAST : BorderLayout.WEST);

        bubbleContainer.add(row);
        bubbleContainer.revalidate();
        scrollToBottom();
    }

    void drawSystemRow(String text) {

        if (bubbleContainer == null) {
            System.out.println("[SYSTEM]  " + text);
            return;
        }

        JLabel label = new JLabel("\u2022 " + text, SwingConstants.CENTER);
        label.setFont(FONT_SMALL.deriveFont(Font.ITALIC));
        label.setForeground(COL_INFO);
        label.setAlignmentX(0.5f);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(1, 0, 1, 0));
        row.add(label, BorderLayout.CENTER);
        row.setAlignmentX(0f);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        bubbleContainer.add(row);
        bubbleContainer.revalidate();
        scrollToBottom();
    }

    public void addSystemMessage(String text) {
        messageHistory.add(new Message(text, false, null, true, ""));
        drawSystemRow(text);
    }

    public void updateOnlineUsers(java.util.List<UserInfo> users) {
        Runnable apply = () -> {
            if (onlineUsersModel == null) {
                pendingUserList = new ArrayList<>(users);
                return;
            }
            pendingUserList = null;
            onlineUsersModel.clear();
            userInfoMap.clear();
            for (UserInfo u : users) {
                onlineUsersModel.addElement(u.username);
                userInfoMap.put(u.username, u);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            apply.run();
        } else {
            SwingUtilities.invokeLater(apply);
        }
    }


    // SECTION 12 - USER INFO POPUP
    // Shown when the user clicks a name in the Online sidebar.
    // Displays the clicked user's IP, port, and whether they are the coordinator.

    void showUserInfoPopup(String clickedUsername) {

        // Look up info; fall back to "Unknown" if not yet populated by the controller
        UserInfo info = userInfoMap.get(clickedUsername);
        if (info == null) {
            info = new UserInfo(clickedUsername, "Unknown", "Unknown", false);
        }

        boolean isCoord = info.isCoordinator;

        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        // Role colours — teal for coordinator, grey for member
        Color roleColor  = isCoord ? COL_ACCENT : COL_HINT;
        Color roleBg     = isCoord ? new Color(0x0D2A2E) : new Color(0x1C2333);
        Color roleBorder = isCoord ? new Color(0x1A4A50) : COL_BORDER;
        String roleText  = isCoord ? "COORDINATOR" : "MEMBER";

        JLabel roleBadgeLabel = new JLabel(roleText, SwingConstants.CENTER);
        roleBadgeLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        roleBadgeLabel.setForeground(roleColor);

        JPanel roleBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        roleBadge.setBackground(roleBg);
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(roleBorder, 1, true),
                new EmptyBorder(4, 14, 4, 14)));
        roleBadge.add(roleBadgeLabel);
        roleBadge.setAlignmentX(0.5f);
        roleBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                roleBadge.getPreferredSize().height));
        
        JLabel atLabel = new JLabel("@" + info.username, SwingConstants.CENTER);
        atLabel.setFont(FONT_TITLE);
        atLabel.setForeground(COL_TEXT);
        atLabel.setAlignmentX(0.5f);

        // Info card showing IP and Port
        JPanel infoCard = new JPanel(new GridLayout(2, 1, 0, 10));
        infoCard.setBackground(BG_INPUT);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        infoCard.setAlignmentX(0.5f);
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                infoCard.getPreferredSize().height + 60));
        infoCard.add(makeInfoRow("\uD83C\uDF10  IP ADDRESS", info.ip));
        infoCard.add(makeInfoRow("\u26A1  PORT",             info.port));

        JButton closePopupBtn = makePillButton("CLOSE  \u2715", new Color(0x374151), 120, 38);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(closePopupBtn);
        btnRow.setAlignmentX(0.5f);
        closePopupBtn.addActionListener(e -> dialog.dispose());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(roleColor, 1, true),
                new EmptyBorder(28, 32, 24, 32)));
        root.add(roleBadge);
        root.add(Box.createVerticalStrut(12));
        root.add(atLabel);
        root.add(Box.createVerticalStrut(18));
        root.add(infoCard);
        root.add(Box.createVerticalStrut(20));
        root.add(btnRow);

        // Drag to move
        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(320, dialog.getHeight()));
        dialog.setLocationRelativeTo(MainChatWindow);
        dialog.setVisible(true);
    }

    // Helper — two-line label row (caption above, value below) used inside the user info card
    private JPanel makeInfoRow(String caption, String value) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        captionLabel.setForeground(COL_HINT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_BOLD);
        valueLabel.setForeground(COL_TEXT);

        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);
        row.add(captionLabel, BorderLayout.NORTH);
        row.add(valueLabel,   BorderLayout.CENTER);
        return row;
    }

     //SECTION 13 - Obtain the users IPv4 address so it is displayed when creating a server.
    static String getLocalIPv4() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                // Skip loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) continue;
                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    // IPv4 only, skip loopback 127.x.x.x
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not determine local IP: " + e);
        }
        return "127.0.0.1"; // fallback if nothing found
    }

    //SECTION 14 - Show error pop-up message
    public void showErrorPopup(String errorMessage) {

        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        JLabel iconLabel = new JLabel("⚠", SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 32));
        iconLabel.setForeground(COL_EXIT);
        iconLabel.setAlignmentX(0.5f);

        JLabel messageLabel = new JLabel("<html><div style='text-align:center'>" + errorMessage + "</div></html>",
                SwingConstants.CENTER);
        messageLabel.setFont(FONT_NORMAL);
        messageLabel.setForeground(COL_TEXT);
        messageLabel.setAlignmentX(0.5f);

        JButton closeBtn = makePillButton("OK", COL_EXIT, 80, 38);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(closeBtn);
        btnRow.setAlignmentX(0.5f);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_EXIT, 1, true),
                new EmptyBorder(28, 32, 24, 32)));
        root.add(iconLabel);
        root.add(Box.createVerticalStrut(12));
        root.add(messageLabel);
        root.add(Box.createVerticalStrut(20));
        root.add(btnRow);

        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(pos.x + e.getX() - dragStart[0].x,
                                pos.y + e.getY() - dragStart[0].y);
            }
        });

        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(300, dialog.getHeight()));
        dialog.setLocationRelativeTo(MainChatWindow);
        dialog.setVisible(true);
    }

    //  SECTION 14A — MAXIMISE / RESTORE TRANSITION
    private void animateWindow(JFrame frame, int tx, int ty, int tw, int th) {
        final int STEPS = 10, DELAY = 10;
        final boolean expanding = tw > frame.getWidth() || th > frame.getHeight();
        final int sx = frame.getX(), sy = frame.getY();
        final int sw = frame.getWidth(), sh = frame.getHeight();

        // Snap at 40% so fade-in starts while fade-out is still finishing (overlap)
        final float SNAP = 0.4f;

        int[] step = {0};
        boolean[] snapped = {false};

        new javax.swing.Timer(DELAY, null) {{
            addActionListener(e -> {
                float p  = (float) ++step[0] / STEPS;
                float t1 = Math.min(p / SNAP, 1f);         // 0→1 over first 40%
                float t2 = Math.max((p - SNAP) / (1f - SNAP), 0f); // 0→1 over last 60%

                if (!snapped[0]) {
                    frame.setOpacity(1f - t1);
                    if (!expanding) {
                        frame.setLocation(sx + Math.round((tx - sx) * t1), sy + Math.round((ty - sy) * t1));
                        frame.setSize(sw + Math.round((tw - sw) * t1), sh + Math.round((th - sh) * t1));
                    }
                    if (p >= SNAP) {
                        snapped[0] = true;
                        frame.setOpacity(0f);
                        if (expanding) {
                            int off = (int)(tw * 0.04f);
                            frame.setLocation(tx + off / 2, ty + off / 2);
                            frame.setSize(tw - off, th - off);
                        } else {
                            frame.setLocation(tx, ty);
                            frame.setSize(tw, th);
                        }
                        frame.revalidate();
                        frame.repaint();
                    }
                } else {
                    frame.setOpacity(t2);
                    if (expanding) {
                        int off = (int)(tw * 0.04f * (1f - t2));
                        frame.setLocation(tx + off / 2, ty + off / 2);
                        frame.setSize(tw - off, th - off);
                    }
                }

                if (step[0] >= STEPS) {
                    stop();
                    frame.setOpacity(1f);
                    frame.setLocation(tx, ty);
                    frame.setSize(tw, th);
                    frame.revalidate();
                    frame.repaint();
                }
            });
            start();
        }};
    }

    //  SECTION 14B — RESIZE SUPPORT FOR UNDECORATED WINDOWS
    // Lets the user drag any edge or corner of the window to resize it,
    // since setUndecorated(true) removes the OS resize handles.
    private static final int RESIZE_MARGIN = 6;

    private void addResizeSupport(JFrame frame, JPanel panel) {
        MouseAdapter resizer = new MouseAdapter() {
            private int startX, startY, startW, startH, startFX, startFY;
            private int zone = 0; // bitmask: 1=left 2=right 4=top 8=bottom

            private int getZone(MouseEvent e) {
                int x = e.getX(), y = e.getY(), w = panel.getWidth(), h = panel.getHeight();
                int z = 0;
                if (x <= RESIZE_MARGIN)          z |= 1;
                if (x >= w - RESIZE_MARGIN)      z |= 2;
                if (y <= RESIZE_MARGIN)          z |= 4;
                if (y >= h - RESIZE_MARGIN)      z |= 8;
                return z;
            }

            private Cursor getCursor(int z) {
                switch (z) {
                    case 1:  return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                    case 2:  return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                    case 4:  return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                    case 8:  return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                    case 5:  return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                    case 6:  return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                    case 9:  return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                    case 10: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                    default: return Cursor.getDefaultCursor();
                }
            }

            @Override public void mouseMoved(MouseEvent e) {
                panel.setCursor(getCursor(getZone(e)));
            }

            @Override public void mousePressed(MouseEvent e) {
                zone   = getZone(e);
                startX = e.getXOnScreen(); startY = e.getYOnScreen();
                startW = frame.getWidth(); startH = frame.getHeight();
                startFX = frame.getX();   startFY = frame.getY();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (zone == 0) return;
                int dx = e.getXOnScreen() - startX;
                int dy = e.getYOnScreen() - startY;
                int nx = startFX, ny = startFY, nw = startW, nh = startH;
                if ((zone & 2) != 0) nw = Math.max(startW + dx, frame.getMinimumSize().width);
                if ((zone & 1) != 0) { nw = Math.max(startW - dx, frame.getMinimumSize().width); nx = startFX + startW - nw; }
                if ((zone & 8) != 0) nh = Math.max(startH + dy, frame.getMinimumSize().height);
                if ((zone & 4) != 0) { nh = Math.max(startH - dy, frame.getMinimumSize().height); ny = startFY + startH - nh; }
                frame.setBounds(nx, ny, nw, nh);
            }

            @Override public void mouseExited(MouseEvent e) {
                panel.setCursor(Cursor.getDefaultCursor());
            }
        };
        panel.addMouseListener(resizer);
        panel.addMouseMotionListener(resizer);
    }

    //  SECTION 15 — EXIT METHOD
    void exitApp() {
        System.exit(0);
    }

    // SECTION 16 - RETURNS TO CHOSEN GUI WINDOW.
    public void returnToJoinScreen() {
        SwingUtilities.invokeLater(() -> {
            if (MainChatWindow != null) {
                MainChatWindow.dispose();
                MainChatWindow = null;
            }
            onlineUsersModel = null;
            bubbleContainer = null;
            chatScroll = null;
            pendingUserList = null;
            userInfoMap.clear();
            messageHistory.clear();
            coordinatorUsername = "";

            boolean proceed = showConnectScreen(1);
            if (proceed) showChatWindow();
        });
    }


    public void returnToCreateScreen() {
        SwingUtilities.invokeLater(() -> {
            if (serverWindow != null) {
                serverWindow.dispose();
                serverWindow = null;
            }
            if (MainChatWindow != null) {
                MainChatWindow.dispose();
                MainChatWindow = null;
            }
            userInfoMap.clear();
            messageHistory.clear();
            coordinatorUsername = "";

            // Go straight back to the create screen (mode 0 = create)
            boolean proceed = showConnectScreen(0);
            if (!proceed) exitApp();
            else if (isHostMode) showServerWindow();
            else showChatWindow();
        });
    }

    public void returnToMainMenu() {
        SwingUtilities.invokeLater(() -> {
            //If the connect screen is currently open, set the flag and let showConnectScreen handle the navigation when it unblocks
            if (connectDialog != null && connectDialog.isShowing()) {
                goToMainMenuAfterClose = true;
                connectDialog.dispose();
                connectDialog = null;
                return;
            }

            // Otherwise handle navigation directly (e.g. called from chat window)
            if (serverWindow != null) { serverWindow.dispose(); serverWindow = null; }
            if (MainChatWindow != null) { MainChatWindow.dispose(); MainChatWindow = null; }

            onlineUsersModel = null;
            bubbleContainer = null;
            chatScroll = null;
            userInfoMap.clear();
            messageHistory.clear();
            coordinatorUsername = "";
            username = "TypedUsername";
            isHostMode = false;
            sessionPort = "7000";
            sessionIp = "127.0.0.1";

            int mode = showLaunchScreen();
            if (mode == -1) { exitApp(); return; }
            boolean proceed = showConnectScreen(mode);
            if (proceed) {
                if (isHostMode) showServerWindow();
                else showChatWindow();
            }
        });
    }

    public void displayRecievedMessage(String recievedMessage, String senderName, String timestamp) {
        // Displays received messages from the controller into the users GUI.
        addMessage(recievedMessage, false, senderName, timestamp);
    }

    //  SECTION 16 - Main method
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        applyTheme();

        Gui app = new Gui();

        int mode = app.showLaunchScreen();
        if (mode == -1) System.exit(0);

        boolean proceed = app.showConnectScreen(mode);
        //If !proceed, showConnectScreen() already handled gui navigation internally
        if (proceed) {
            if (app.isHostMode) SwingUtilities.invokeLater(app::showServerWindow);
            else SwingUtilities.invokeLater(app::showChatWindow);
        }
    }
}