import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    static final String TERMINATED = "terminated by time";
    static final String FAILED = "termination failed";

//    static final String DIR_PATH = "inputFiles/";
//    static final String[] FILES = {"input1.txt", "input2.txt"};

    static final String DIR_PATH = "new input files/medium/";
    static final String[] FILES = {"input1.txt", "input2.txt", "input3.txt"};
//    static final String[] FILES = {"input3.txt"};



    public static void main(String[] args) {
        for (String path : FILES) {
            try {
                System.out.println(DIR_PATH + path);
                Scanner scannerInput = new Scanner(new File(DIR_PATH + path));

                int numNodes = Integer.parseInt(scannerInput.next());
                Manager m = new Manager();

                Thread startThread = new Thread(() -> startManager(m, path));
                Thread terminateThread = new Thread(() -> {
                    try {
                        Thread.sleep(numNodes * 5 * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(TERMINATED);
                    endManager(m, numNodes);
                });

                startThread.start();
                terminateThread.start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void startManager(Manager m, String path) {
//        System.out.println("> Start Manager");
        long startTime = System.currentTimeMillis();
        m.readInput(path);
        String coloringOutput = m.start();
        long endTime = System.currentTimeMillis();
        System.out.println(coloringOutput +"\nElapsed Time in milli seconds: "+ (endTime-startTime));
    }

    private static void endManager(Manager m, int numNodes) {
        Thread terminateThread = new Thread(() -> System.out.println(m.terminate()));
        Thread failedThread = new Thread(() -> {
            try {
                Thread.sleep(numNodes * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(FAILED);
        });

        terminateThread.start();
        failedThread.start();
    }



}

