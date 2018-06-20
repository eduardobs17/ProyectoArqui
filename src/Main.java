import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Main {

    /**
     * Metodo principal que inicia el programa.
     * @param argv
     */
    public static void main(String[] argv) {
        System.out.println("SIMULACION PROCESADOR MULTINUCLEO\n");

        MemoriaPrincipal memoria = MemoriaPrincipal.getInstancia();
        Scanner reader = new Scanner(System.in);
        int cantHilos, quantum, pc;

        System.out.println("Digite la cantidad de hilos que desea que maneje el procesador:");
        cantHilos = reader.nextInt();
        System.out.println("Digite el tamaño de quantum que desea:");
        quantum = reader.nextInt();

        Procesador procesador = Procesador.getInstancia(cantHilos, quantum);
        Queue<String> colaHilos = new ArrayDeque<String>(cantHilos+1);
        Queue<Integer> colaPCs = new ArrayDeque<Integer>(cantHilos+1);

        // Doble barrera
        CyclicBarrier barreraI = new CyclicBarrier(cantHilos);
        CyclicBarrier barreraF = new CyclicBarrier(cantHilos);

        for (int i = 0; i < cantHilos; i++) {
            String rutaHilo = i + ".txt";
            String inst = leerArchivo(rutaHilo);
            pc = memoria.agregarInst(inst);
            procesador.llenarContextopc(i, pc);
            colaHilos.add(inst);
            colaPCs.add(pc);
        }
        procesador.run(colaHilos, colaPCs, barreraI, barreraF);

        /*try {
            System.out.println("Levanto barrera");
            barreraI.await();
            barreraF.await();
            System.out.println("Terminado: todos llegan aqui");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Apertura del fichero y creacion de BufferedReader para poder
     * hacer una lectura comoda (disponer del metodo readLine()).
     * @param rutaArchivo
     */
    private static String leerArchivo(String rutaArchivo) {
        String instrucciones = "";
        File archivo;
        FileReader fr = null;
        BufferedReader br;

        try {
            archivo = new File (rutaArchivo);
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            String linea;
            while((linea = br.readLine()) != null) {
                instrucciones += linea + "\n"; //Concatena cada instruccion y agrega un \n al final.
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if ( null != fr ) {
                    fr.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return instrucciones;
    }
}
