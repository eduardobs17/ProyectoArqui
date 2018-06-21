import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

/** Clase principal del programa. */
public class Main {

    /**
     * Metodo principal que inicia el programa.
     * @param argv Parametros de entrada.
     */
    public static void main(String[] argv) {
        System.out.println("SIMULACION PROCESADOR MULTINUCLEO\n");

        Scanner reader = new Scanner(System.in);
        int cantHilos, quantum, pc;

        System.out.println("Digite la cantidad de hilos que desea que maneje el procesador:");
        cantHilos = reader.nextInt();
        System.out.println("Digite el tamaño de quantum que desea:");
        quantum = reader.nextInt();

        MemoriaPrincipal memoria = MemoriaPrincipal.getInstancia();
        Procesador procesador = Procesador.getInstancia(cantHilos, quantum);
        Queue<String> colaHilos = new ArrayDeque<>(cantHilos);
        Queue<Integer> colaPCs = new ArrayDeque<>(cantHilos);

        CyclicBarrier barreraI, barreraF;
        //Maximo 4
        if (cantHilos < 3) {
            barreraI = new CyclicBarrier(cantHilos + 1);
            barreraF = new CyclicBarrier(cantHilos + 1);
        } else {
            barreraI = new CyclicBarrier(4);
            barreraF = new CyclicBarrier(4);
        }

        for (int i = 0; i < cantHilos; i++) {
            String rutaHilo = i + ".txt";
            String inst = leerArchivo(rutaHilo);
            pc = memoria.agregarInst(inst);
            procesador.llenarContextopc(i, pc);
            colaHilos.add(inst);
            colaPCs.add(pc);
        }
        procesador.run(colaHilos, colaPCs, barreraI, barreraF);
    }

    /**
     * Metodo que lee y guarda las instrucciones de los archivos de los hilillos.
     * @param rutaArchivo Ruta del archivo que contiene las instrucciones.
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