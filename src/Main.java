import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import javax.swing.*;

/** Clase principal del programa. */
public class Main {

    /**
     * Metodo principal que inicia el programa.
     * @param argv Parametros de entrada.
     */
    public static void main(String[] argv) {
        int cantHilos, quantum, pc;
        String verResultados = "";

        Object[] elecciones = { "Rapida", "Lenta"};
        Object eleccion = JOptionPane.showInputDialog(null,
                "Elija la modalidad de ejecucion", "SIMULACION PROCESADOR MULTINUCLEO",
                JOptionPane.QUESTION_MESSAGE, null,
                elecciones, elecciones[0]);

        if(eleccion == "Rapida") { //Ejecuta los hilos hasta que finalice el proceso.
            cantHilos = Integer.parseInt(JOptionPane.showInputDialog( "SIMULACION PROCESADOR MULTINUCLEO\n\n" +
                    "Digite la cantidad de hilos que desea que maneje el procesador"));
            quantum = Integer.parseInt(JOptionPane.showInputDialog( "SIMULACION PROCESADOR MULTINUCLEO\n\n" +
                    "Digite el tamaño de quantum que desea"));

            MemoriaPrincipal memoria = MemoriaPrincipal.getInstancia();
            Procesador procesador = Procesador.getInstancia(cantHilos, quantum);
            Queue<String> colaHilos = new ArrayDeque<>(cantHilos);
            Queue<Integer> colaPCs = new ArrayDeque<>(cantHilos);

            CyclicBarrier barreraI;
            if (cantHilos == 1) {
                barreraI = new CyclicBarrier(2);
            } else {
                barreraI = new CyclicBarrier(3);
            }

            for (int i = 0; i < cantHilos; i++) {
                String rutaHilo = i + ".txt";
                String inst = leerArchivo(rutaHilo);
                pc = memoria.agregarInst(inst);
                procesador.llenarContextopc(i, pc);
                colaHilos.add(inst);
                colaPCs.add(pc);
            }
            boolean r = procesador.run(colaHilos, colaPCs, barreraI);
        }
        else {
            JOptionPane.showMessageDialog(null, "Aqui llega hasta el ciclo de reloj 20");
        }
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