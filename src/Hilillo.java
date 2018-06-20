import java.util.concurrent.CyclicBarrier;

public class Hilillo extends Thread {
    private Procesador procesador;
    private int nucleo, cantInst;
    private String instrucciones;

    public int[] registro = new int[32];
    private int[] IR = new int[4];
    public int[] estado = new int[4];

    public int pc;
    public int ciclosReloj;
    public int quantum = 0;

    CyclicBarrier barreraI;
    CyclicBarrier barreraF;


    Hilillo(String inst, int pcHilillo, int pNucleo, CyclicBarrier bai, CyclicBarrier baf, int cantHilos, int quamtun) {
        cantInst = inst.split("\n").length;
        pc = pcHilillo;
        nucleo = pNucleo;
        procesador = Procesador.getInstancia(cantHilos, quamtun);
        ciclosReloj = 0;

        barreraI = bai;
        barreraF = baf;
    }

    /**
     * Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta
     */
    @Override
    public void run() {
        try { // Sincroniza que los hilos inicien todos a la vez
            barreraI.await(); // Se queda bloqueado hasta que todos los hilos hagan este llamado.
            System.out.println("Hilos se ejecutan");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int bloque, posCacheI, estadoHilillo = 1, numPalabra;

        while (estadoHilillo != 0) {
            //System.out.println("Hilillo 1, estado " + estadoHilillo);
            bloque = pc / 16;
            numPalabra = pc - (bloque*16);
            posCacheI = procesador.calcularPosCache(bloque, nucleo);

            if (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] != bloque
                    || (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] == bloque
                    && procesador.cacheInstrucciones[nucleo].valores[posCacheI][17] == 0)) {
                procesador.loadI(nucleo, posCacheI, pc, this);
            }

            for (int x = 0; x < 4; x++) {
                IR[x] = procesador.cacheInstrucciones[nucleo].valores[posCacheI][numPalabra];
                pc++;
                numPalabra++;
            }
            estadoHilillo = procesador.ALU(IR, this);
            ciclosReloj++;
        }

        try {
            barreraF.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna el nucleo.
     * @return
     */
    public int getNucleo() {
        return nucleo;
    }
}
