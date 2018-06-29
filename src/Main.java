import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Phaser;
import javax.swing.JOptionPane;

/** Clase principal del programa. */
public class Main {

    /**
     * Metodo principal que inicia el programa.
     * @param argv Parametros de entrada.
     */
    public static void main(String[] argv) {
        int cantHilos, quantum, pc;

        String[] elecciones = {"Rapida", "Lenta"};
        Object eleccion = JOptionPane.showInputDialog(null,
                "Elija la modalidad de ejecucion", "SIMULACION PROCESADOR MULTINUCLEO",
                JOptionPane.QUESTION_MESSAGE, null,
                elecciones, elecciones[0]);

        cantHilos = Integer.parseInt(JOptionPane.showInputDialog("SIMULACION PROCESADOR MULTINUCLEO\n\n" + "Digite la cantidad de hilos que desea que maneje el procesador"));
        quantum = Integer.parseInt(JOptionPane.showInputDialog("SIMULACION PROCESADOR MULTINUCLEO\n\n" + "Digite el tamaño de quantum que desea:"));

        MemoriaPrincipal memoria = MemoriaPrincipal.getInstancia();
        Procesador procesador = Procesador.getInstancia(cantHilos, quantum);
        Queue<Integer> colaIDs = new ArrayDeque<>(cantHilos);

        Phaser barreraInicio = new Phaser(), barreraFinal = new Phaser();
        barreraInicio.register();
        barreraFinal.register();

        for (int i = 0; i < cantHilos; i++) {
            String rutaHilo = i + ".txt";
            String inst = leerArchivo(rutaHilo);
            pc = memoria.agregarInst(inst);
            procesador.llenarContextopc(i, pc);
            colaIDs.add(i);
        }

        if (eleccion == "Rapida") {
            procesador.run(colaIDs, barreraInicio, barreraFinal, 0);
        } else {
            procesador.run(colaIDs, barreraInicio, barreraFinal, 1);
        }

        //IMPRIMIR CACHES
        System.out.println("CACHE NUCLEO 0");
        procesador.cacheDatos[0].imprimirCache();
        System.out.println("CACHE NUCLEO 1");
        procesador.cacheDatos[1].imprimirCache();

        //IMPRIMIR MEMORIA
        System.out.println("MEMORIA DE DATOS");
        memoria.imprimirMemoria();

        //IMRPRIMIR REGISTROS
        for (int i = 0; i < cantHilos; i++) {
            System.out.println("\nREGISTROS HILILLO " + i);
            procesador.imprimirRegistroHilo(i);
        }

        System.out.println("\nSimulación finalizada.");
    }

    /**
     * Metodo que lee y guarda las instrucciones de los archivos de los hilillos.
     * @param rutaArchivo Ruta del archivo que contiene las instrucciones.
     */
    private static String leerArchivo(String rutaArchivo) {
        StringBuilder instrucciones = new StringBuilder();
        File archivo;
        FileReader fr = null;
        BufferedReader br;

        try {
            archivo = new File (rutaArchivo);
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            String linea;
            while((linea = br.readLine()) != null) {
                instrucciones.append(linea).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return instrucciones.toString();
    }
}