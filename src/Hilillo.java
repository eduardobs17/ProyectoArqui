import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Hilillo extends Thread {
    private Procesador procesador;
    private int nucleo;
    private String instrucciones;
    private int estadoHilillo;

    public int[] registro = new int[32];
    private int[] IR = new int[4];
    //public int[] estado = new int[4];

    public int pc;
    public int ciclosReloj;
    public int quantum;

    private CyclicBarrier barreraI;

    /**
     * Constructor del hilillo.
     * @param inst String con las instrucciones del hilillo.
     * @param pcHilillo PC del hilillo.
     * @param pNucleo Nucleo del hilillo.
     * @param bai Barrera de inicio para que los hilillos inicien a la vez.
     */
    Hilillo(String inst, int pcHilillo, int pNucleo, CyclicBarrier bai) {
        pc = pcHilillo;
        nucleo = pNucleo;
        procesador = Procesador.getInstancia(1,1);
        ciclosReloj = 0;
        estadoHilillo = 1;
        barreraI = bai;
    }

    /**
     * Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta.
     * Y asi hasta llegar al final del archivo o hasta que haya un fallo de cache.
     */
    @Override
    public void run() {
        int bloque, posCacheI, numPalabra;

        while (estadoHilillo != 0) {
            bloque = pc / 16;
            numPalabra = pc - (bloque*16);
            posCacheI = bloque % 4;

            if (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] != bloque
                    || (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] == bloque
                    && procesador.cacheInstrucciones[nucleo].valores[posCacheI][17] == 0)) {
                procesador.loadI(nucleo, posCacheI, pc, this);
            }

            for (int x = 0; x < 4; x++) { //Cada instruccion la coloca en el IR y la ejecuta con el metodo ALU.
                IR[x] = procesador.cacheInstrucciones[nucleo].valores[posCacheI][numPalabra];
                pc++;
                numPalabra++;
            }
            estadoHilillo = procesador.ALU(IR, this);
            System.out.println("Hilillo " + nucleo + ", estado " + estadoHilillo);
            try {
                barreraI.await(); //Se queda bloqueado hasta que todos los hilos hagan este llamado.
                System.out.println("Hilos cambian  ciclo de reloj");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            ciclosReloj++;
        }
    }

    /**
     * Retorna el nucleo.
     * @return Devuelve el nucleo del hilillo.
     */
    public int getNucleo() {
        return nucleo;
    }

    public int getEstadoHilillo() { return estadoHilillo; }

    public void cambiarBarrera(CyclicBarrier newBarreraI) {
        barreraI = newBarreraI;
    }
}
