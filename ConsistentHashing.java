import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An Implementation of Consistent Hashing from https://www.youtube.com/watch?v=Nq_07Nhz2WM&ab_channel=ByteVigor
 */
public class ConsistentHashing {

    public final TreeMap<Long, String> ring;
    private final int numberOfReplicas;
    private final MessageDigest md;

    public ConsistentHashing(int numberOfReplicas) throws NoSuchAlgorithmException {
        this.ring = new TreeMap<>();
        this.numberOfReplicas = numberOfReplicas;
        this.md = MessageDigest.getInstance("SHA-256");
    }

    public void addServer(String server) {
        for(int i = 0; i < numberOfReplicas; i++) {
            long hash = computeHash(server + ":" + i);
            ring.put(hash, server);
        }
    }

    public void removeServer(String server) {
        for(int i = 0; i < numberOfReplicas; i++) {
            long hash = computeHash(server + ":" + i);
            ring.remove(hash, server);
        }
    }

    public String getServer(String key){
        if(ring.isEmpty()) return null;

        long hash = computeHash(key);
        if(!ring.containsKey(hash)){
            SortedMap<Long, String> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }

        return ring.get(hash);

    }

    private long computeHash(String s) {
        md.reset();
        md.update(s.getBytes());
        byte[] digest = md.digest();
        long hash = ((long) (digest[0] & 0xFF) << 56) |
                ((long) (digest[1] & 0xFF) << 48) |
                ((long) (digest[2] & 0xFF) << 40) |
                ((long) (digest[3] & 0xFF) << 32) |
                ((long) (digest[4] & 0xFF) << 24) |
                ((long) (digest[5] & 0xFF) << 16) |
                ((long) (digest[6] & 0xFF) << 8) |
                ((long) (digest[7] & 0xFF));
        return hash;
    }

    public static void main(String[] args) {
        try {
            ConsistentHashing ch = new ConsistentHashing(3); // 3 replicas for each server
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\nConsistent Hashing Demo");
                System.out.println("1. Add server");
                System.out.println("2. Remove server");
                System.out.println("3. Get server for key");
                System.out.println("4. Print all servers");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        System.out.print("Enter server name to add: ");
                        String serverToAdd = scanner.nextLine();
                        ch.addServer(serverToAdd);
                        System.out.println("Server added: " + serverToAdd);
                        break;
                    case 2:
                        System.out.print("Enter server name to remove: ");
                        String serverToRemove = scanner.nextLine();
                        ch.removeServer(serverToRemove);
                        System.out.println("Server removed: " + serverToRemove);
                        break;
                    case 3:
                        System.out.print("Enter key: ");
                        String key = scanner.nextLine();
                        String server = ch.getServer(key);
                        System.out.println("Server for key '" + key + "': " + server);
                        break;
                    case 4:
                        System.out.println("All servers in the ring:");
                        System.out.println(ch.ring.toString());
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


}
