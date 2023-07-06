import java.io.File;
import java.util.*;

public class Manager {

//    static final String DIR_PATH = "inputFiles/";
    static final String DIR_PATH = "new input files/medium/";

    private LinkedList<Node> nodes;
    private LinkedList<Thread> node_threads;
    String ret;
    String path;


    private int numNodes, maxDegree;

    public Manager() {
        this.nodes = new LinkedList<>();
        this.node_threads = new LinkedList<>();
        this.numNodes = 0;
        this.maxDegree = 0;
        this.ret = "";
    }

    public static int[][] getNodes(String str) {
        int[][] ret;
        String innerContents = str.substring(2, str.length() - 2);
        String[] sub_str = innerContents.split("], \\s*\\[");

        ret = new int[sub_str.length][3];

        for (int i = 0; i < sub_str.length; i++) {
            String[] node_info = sub_str[i].split(",\\s*");
            ret[i] = Arrays.stream(node_info).mapToInt(Integer::parseInt).toArray();
        }

        return ret;
    }

    public void readInput(String path) {
//        System.out.println("> Read Input");
        this.path = path;
        try {
            Scanner scanner = new Scanner(new File(DIR_PATH + path));

            this.numNodes = Integer.parseInt(scanner.nextLine());
            this.maxDegree = Integer.parseInt(scanner.nextLine());

//            System.out.println("> numNodes: " + this.numNodes);
//            System.out.println("> maxDegree: " + this.maxDegree);

            while (scanner.hasNextLine()) {
                String node = scanner.nextLine();
//                System.out.println("> node: " + node);
                String[] node_split = node.split(" ", 2);
                int Node_id = Integer.parseInt(node_split[0]);
                int[][] neighbors = getNodes(node_split[1]);
                // remove this.path \/
                this.nodes.add(new Node(Node_id, this.numNodes, this.maxDegree, neighbors));
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
//        System.out.println("> there's " + this.nodes.size() + " nodes");
//        System.out.println("> End Read Input");

    }

    public String start() {

        // open nodes ports for connections
        for (Node node: this.nodes)
            while(!node.open_ports()) {}
        for (Node node: this.nodes)
            node.connect_to_neighbors();

        // start exchanging messages
        for (Node node: this.nodes)
            node.start();

        // wait for all nodes to finish
        // might change later
        for (Node node: this.nodes) {
            try {
                node.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.ret = "";
        for (Node node: this.nodes) {
            this.ret = this.ret + node.id + "," + node.color + "\n";
            node.interrupt();
        }

        Thread.currentThread().interrupt();

        return this.ret;
//        return this.path;
    }

    public String terminate() {

        for (Node node: this.nodes) {
            node.interrupt();
            node.end();
        }

        Thread.currentThread().interrupt();
        return this.ret;
//        return this.path;

    }
}
