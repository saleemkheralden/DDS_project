import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.net.*;

public class Node extends Thread {
    int id;
    int color;
    private final int numNodes;
    private final int maxDeg;
    private final int[][] neighbors;
    private final HashMap<Integer, Integer> neighbors_colors;
    private final HashMap<Integer, ServerSocket> reading_sockets;
    private final HashMap<Integer, Socket> writing_sockets;
    private final HashMap<Integer, PrintWriter> out_sockets;
    private final HashMap<Integer, Thread> neighbor_threads;
    public boolean ready_for_accepting_msgs;

    boolean first_round;
    boolean stop;
    String path;

    // remove String path
    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.neighbors = neighbors;
        this.neighbors_colors = new HashMap<>();
        this.neighbor_threads = new HashMap<>();
        this.path = "";


        for (int[] neighbor : this.neighbors)
            this.neighbors_colors.put(neighbor[0], -1);


        this.reading_sockets = new HashMap<>();
        this.writing_sockets = new HashMap<>();
        this.out_sockets = new HashMap<>();
        this.ready_for_accepting_msgs = false;
        this.first_round = false;
        this.stop = false;
    }

    public boolean open_ports() {
        /*
         * open all listening ports
         * then update 'ready_for_accepting_msgs' flag to true if all ports are open
         */

        for (int[] neighbor : this.neighbors) {
            try {
                this.reading_sockets.put(neighbor[0],
                        new ServerSocket(neighbor[1]));
            } catch (Exception e) {
//                System.out.println(this.id + " Can't open port " + neighbor[1]);
                return false;
//                e.printStackTrace();
            }
        }

        this.ready_for_accepting_msgs = (this.reading_sockets.size() == this.neighbors.length);
        return true;
    }

    public void connect_to_neighbors() {
        for (int[] neighbor : this.neighbors) {
            try {
                this.writing_sockets.put(neighbor[0],
                        new Socket("127.0.0.1", neighbor[2]));
                this.out_sockets.put(neighbor[0],
                        new PrintWriter(this.writing_sockets.get(neighbor[0]).getOutputStream(), true));
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    public void send(String str) {
        for (Map.Entry<Integer, Socket> e : this.writing_sockets.entrySet())
            this.out_sockets.get(e.getKey()).println(str);
    }

    private Boolean check_neighbor_undecided() {
        for (Map.Entry<Integer, Integer> e : this.neighbors_colors.entrySet())
            if ((e.getKey() > this.id) && (e.getValue() == -1))
                return true;
        return false;
    }

    public int get_minimal_color() {
        int[] colors = new int[this.maxDeg + 1];
        for (Integer e : this.neighbors_colors.values())
            if (e > -1)
                colors[e] = 1;

        for (int i = 0; i < colors.length; i++)
            if (colors[i] == 0)
                return i;
        return -1;
    }

    public void recv() {
        for (int[] neighbor : this.neighbors) {
            this.neighbor_threads.put(neighbor[0],
                    new Thread(() -> {
                        try {
                            Socket socket = this.reading_sockets.get(neighbor[0]).accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            while (!this.stop) {
                                String cmd = in.readLine();
                                if (cmd == null)
                                    continue;
                                if (cmd.contains("<")) {
                                    int neighbor_id = Integer.parseInt(cmd.substring(1).split(",")[0]);
                                    this.neighbors_colors.put(neighbor_id, -1);
                                } else if (cmd.contains("undecided")) {
                                    continue;
                                } else {
                                    this.neighbors_colors.put(neighbor[0], Integer.parseInt(cmd));
                                }

//                                System.out.println(neighbor[0] + " still running on port " + socket.getPort());
                            }

//                            System.out.println("Socket " + neighbor[0] + " on port " + this.reading_sockets.get(neighbor[0]).getPort() + " is closed");

                            this.reading_sockets.get(neighbor[0]).close();
                            socket.close();
                            in.close();

//                            System.out.println(this.reading_sockets.get(neighbor[0]).isClosed() + " " + socket.isClosed() + " ");

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }));
            this.neighbor_threads.get(neighbor[0]).start();
        }
    }

    public void end() {
        for (int[] neighbor : this.neighbors) {
            try {
                this.writing_sockets.get(neighbor[0]).close();
                this.out_sockets.get(neighbor[0]).close();
                this.neighbor_threads.get(neighbor[0]).interrupt();

//                this.reading_sockets.get(neighbor[0]).close();
                Thread.currentThread().interrupt();
            } catch (IOException e) {
//                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println("NullPointer at " + this.path + " " + this.id + " "
                + this.writing_sockets.get(neighbor[0]) + " " +
                this.out_sockets.get(neighbor[0]) + " " +
                this.neighbor_threads.get(neighbor[0]) + " ");
            }
        }
    }

    @Override
    public void run() {
        super.run();

        Thread.currentThread().setName("Thread_" + this.maxDeg + "_" + this.id);

        // start listening to neighbors
        this.recv();

        while (!this.stop) {
            // start sending messages to neighbors
            if (this.first_round) {
                this.first_round = false;
                this.send("<" + this.id + ",undecided>");
                continue;
            }

            if (this.check_neighbor_undecided()) {
                this.send("undecided");
            } else {
                int color = this.get_minimal_color();
                this.color = color;
                this.send(String.valueOf(color));
                stop = true;
            }
        }

        this.end();
    }
}
