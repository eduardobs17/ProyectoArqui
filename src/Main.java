import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

public class Main {

    /**
     * Apertura del fichero y creacion de BufferedReader para poder
     * hacer una lectura comoda (disponer del metodo readLine()).
     * @param rutaArchivo
     */
    public static String leerArchivo(String rutaArchivo) {
        String instrucciones = "";
        File archivo;
        FileReader fr = null;
        BufferedReader br;

        try {
            archivo = new File (rutaArchivo);
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea;
            while((linea = br.readLine()) != null)
                instrucciones += linea + "\n";
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            // En el finally cerramos el fichero, para asegurarnos
            // que se cierra tanto si todo va bien como si salta
            // una excepcion.
            try {
                if( null != fr ){ fr.close(); }
            } catch (Exception e2){ e2.printStackTrace(); }
        }
        return instrucciones;
    }

    public static void main(String[] argv) {
        System.out.println("SIMULACION PROCESADOR MULTINUCLEO\n");

        Scanner reader = new Scanner(System.in);
        int cantHilos;
        System.out.println("Digite la cantidad de hilos que desea que maneje el procesador: ");
        cantHilos = reader.nextInt();

        //System.out.println("CantHilos = " + cantHilos);
        Queue<String> colaHilos = new ArrayDeque<>(cantHilos);

        for (int i = 0; i < cantHilos; i++) {
            String hilo = i + ".txt";
            String inst = leerArchivo(hilo);
            colaHilos.add(inst);
        }
    }
}
