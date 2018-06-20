import java.util.concurrent.CyclicBarrier;

public class Hilillo extends Thread {
    private Procesador procesador;
    private int nucleo;
    private int cantInst;
    private String instrucciones;

    public int[] registro = new int[32];
    private int[] IR = new int[4];
    public int[] estado = new int[4];

    public int pc;
    public int ciclosReloj;
    public int quantum;

    CyclicBarrier barreraI;
    CyclicBarrier barreraF;

    /**
     * Constructor de Hilillo, inicializa variables y obtiene el procesador por medio de Singleton.
     * @param inst
     * @param pcHilillo
     * @param pNucleo
     * @param bai
     * @param baf
     * @param cantHilos
     * @param q
     */
    Hilillo(String inst, int pcHilillo, int pNucleo, CyclicBarrier bai, CyclicBarrier baf, int cantHilos, int q) {
        cantInst = inst.split("\n").length;
        pc = pcHilillo;
        nucleo = pNucleo;
        procesador = Procesador.getInstancia(cantHilos, q);
        ciclosReloj = 0;
        quantum = q;

        barreraI = bai;
        barreraF = baf;
    }

    /**
     * Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta.
     * Y asi hasta llegar al final del archivo o hasta que haya un fallo de cache.
     */
    @Override
    public void run() {
        try { //Sincroniza que los hilos inicien todos a la vez.
            barreraI.await(); //Se queda bloqueado hasta que todos los hilos hagan este llamado.
            System.out.println("Hilos se ejecutan");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int bloque, posCacheI, estadoHilillo = 1, numPalabra;

        while (estadoHilillo != 0) {
            System.out.println("Hilillo " + nucleo + ", estado " + estadoHilillo);
            bloque = pc / 16;
            numPalabra = pc - (bloque*16);
            posCacheI = procesador.calcularPosCache(bloque, nucleo);

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
