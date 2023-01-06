package grafo.io;

import java.util.Scanner;

public class IO_Handler {


    public static String requireInput(String message) {
        Scanner in = new Scanner(System.in);
        System.out.println(message);
        return in.nextLine();
    }
}
