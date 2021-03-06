import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * NSSA-290 Networking Essentials For Development
 * ChatServer.java
 * Purpose: connect to the server and have feed back to the client
 *
 * @author Mitchell Sweet, Caitlyn Daly and Yang Jin
 * @version 1.0 12/5/2017
 */
public class UDPChatServer extends JFrame {

    /**
     * The socket to accept client connections
     */
    private DatagramSocket datagramSocket;

    /**
     * the size of the buf for datagram packets
     */
    private final int bufSize = 1024;
    private JPanel jpNorth = new JPanel();
    private TitledBorder tbServer = new TitledBorder("Chat Server :");
    private JTextArea jtaMain = new JTextArea();

    private ServerSocket servSock = null;
    private BufferedReader rdr = null;
    private PrintWriter wrt = null;

    private Socket sock = null;
    public static final String SERVER_NAME = "";
    public static final int SERVER_PORT = 16734;

    private Vector<ServerRunnable> serverList = new Vector<ServerRunnable>();
    private ArrayList<String> nameHolder = new ArrayList<String>();
    private Vector<Thread> threadHolder = new Vector<Thread>();

    private int clientCount = 0;

    private JLabel ipAddress = new JLabel();
    private JLabel jlConnected = new JLabel("Connected Clients: " + clientCount);
    private final Lock rLock = new ReentrantLock(true);

    /**
     * The constructor of the chat server that initiate the server with the layout of the panel
     */
    public UDPChatServer() {
        //Layout of the panel
        this.setTitle("Chat Server Message Console");
        this.setLocation(1180, 200);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(400, 440);
        this.setLayout(new BorderLayout());

        jpNorth.setBorder(tbServer);
        jpNorth.setPreferredSize(new Dimension(400, 100));

        JPanel jpLabels = new JPanel(new GridLayout(0, 1));
        jpLabels.add(ipAddress);
        jpLabels.add(jlConnected);
        jpNorth.add(jpLabels);

        this.add(jpNorth, BorderLayout.NORTH);


        //Scroll Panel
        jtaMain.setEditable(false);
        this.add(new JScrollPane(jtaMain), BorderLayout.CENTER);

        setVisible(true);

        try {
            servSock = new ServerSocket(SERVER_PORT);
            InetAddress ip = InetAddress.getLocalHost();
            ipAddress.setText("IP Address: " + ip.getHostAddress());
            datagramSocket = new DatagramSocket(SERVER_PORT);

            System.out.println("getLocalHost: " + InetAddress.getLocalHost());
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
                    "IO Exception" + ioe, "IO Exception",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        //create the thread and start the new thread
        while (true) {
            try {

                Socket sock = servSock.accept();
                ServerRunnable cr = new ServerRunnable(sock, serverList);
                Thread t = new Thread(cr);
                t.start();

                serverList.add(cr);
                threadHolder.add(t);

                clientCount++;
                jlConnected.setText("Connected Clients: " + clientCount);

                System.out.println("serverList: " + serverList.size());

            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null,
                        "IO Exception" + ioe, "IO Exception",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

    } // ChatServer constructor end


    class ServerRunnable implements Runnable {

        private Socket sock;
        private Vector<ServerRunnable> serverList = new Vector<ServerRunnable>();

        private PrintStream os = null;
        private BufferedReader is = null;

        /**
         * the constructor of ServerRunnable
         */
        public ServerRunnable(Socket _sock, Vector<ServerRunnable> _serverList) {
            serverList = _serverList;
            sock = _sock;
        } // ServerRunnable constructor end

        /**
         * the method to open up the chat, and print out the information with the
         * client count and client name.
         *
         * @return none
         */
        public void run() {

            //new stuff//
            //wait for clients to connect to the server and start a new thread,
            //then continue waiting.
            //create the packet to accept a message
            
            
            while (true) {
                byte[] buf = new byte[bufSize];
                try {
                    //receive a message from the client
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    datagramSocket.receive(packet);
                } catch (IOException e) {
                    System.err.println("Error receiving datagram packet");
                    JOptionPane.showMessageDialog(null,
                            "Error: There is an issue with your connection. Please restart the program.", "IO Exception",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Error: There is an issue with your connection. Please restart the program.", "IO Exception",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
                //Start a new thread for this client
                ServerRunnable cr = new ServerRunnable(sock, serverList);
                Thread t = new Thread(cr);
                t.start();

                serverList.add(cr);
                threadHolder.add(t);

                clientCount++;
                jlConnected.setText("Connected Clients: " + clientCount);

                System.out.println("serverList: " + serverList.size());
            }
            //end new stuff//
        }

        /**
         * This is the method to write out the information. It will be called in the run function
         *
         * @param s the string of information that wants to print out
         * @return none
         */
        public void writeString(String s) {
            rLock.lock();
            for (int i = 0; i < serverList.size(); i++) {
                if (serverList.get(i) != null) {
                    serverList.get(i).os.println(s);
                }
            }
            rLock.unlock();
        } // writeString end
    } // ServerRunnable end
}


