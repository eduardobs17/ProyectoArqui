import java.util.concurrent.Phaser;

public class Hilillo extends Thread {
    private Procesador procesador;
    private int nucleo;
    private int estadoHilillo;
    public int ciclosRelojHilillo;
    private Phaser barreraInicio;
    private int[] IR;
    private boolean hililloNuevo;

    public int[] registro;
    public int pc;
    public int idHilillo;

    /**
     * Constructor del hilillo.
     * @param pNucleo Nucleo del hilillo.
     * @param bi Barrera de inicio para que los hilillos inicien a la vez.
     */
    Hilillo(int pNucleo, Phaser bi, int id, int[] contextoHilillo, boolean nuevo) {
        procesador = Procesador.getInstancia(1,1);
        nucleo = pNucleo;
        barreraInicio = bi;
        idHilillo = id;

        barreraInicio.register();

        registro = new int[32];
        System.arraycopy(contextoHilillo, 0, registro, 0, 32);
        pc = contextoHilillo[32];

        estadoHilillo = 1;
        ciclosRelojHilillo = 0;
        IR = new int[4];
        hililloNuevo = nuevo;
    }

    /**
     * Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta.
     * Y asi hasta llegar al final del archivo o hasta que haya un fallo de cache.
     */
    @Override
    public void run() {
        int bloque, posCacheI, numPalabra;

        while (estadoHilillo != 0) {
            bloque = (pc - 384) / 16;
            numPalabra = (pc - 384) % 16;
            posCacheI = bloque % 4;

            if (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] != bloque
                    || (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] == bloque
                    && procesador.cacheInstrucciones[nucleo].valores[posCacheI][17] == 0)) {
                int res = -1;
                while (res == -1) {
                    res = procesador.loadI(nucleo, posCacheI, pc, this);
                    if (res == -1) {
                        barreraInicio.arriveAndAwaitAdvance();
                        ciclosRelojHilillo++;
                    }
                }
            }

            for (int x = 0; x < 4; x++) { //Cada instruccion la coloca en el IR y la ejecuta con el metodo ALU.
                IR[x] = procesador.cacheInstrucciones[nucleo].valores[posCacheI][numPalabra];
                pc++;
                numPalabra++;
            }
            estadoHilillo = procesador.ALU(IR, this);
            if (estadoHilillo == -1) {
                pc -= 4;
            }

            ciclosRelojHilillo++;
            if (estadoHilillo != 0) {
                barreraInicio.arriveAndAwaitAdvance();
            } else {
                System.arraycopy(registro, 0, procesador.contexto[idHilillo], 0, 32);
                barreraInicio.arriveAndDeregister();
            }
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

    public Phaser getBarreraI() { return barreraInicio; }

    public void desregistrarHilillo(CacheI[] cacheI, CacheD[] cacheD, Hilillo hilo) {
        barreraInicio.arriveAndDeregister();

        for (int x = 0; x < 4; x++) {
            if (cacheI[0].locks[x].owner() == hilo.getId()) {
                cacheI[0].reservado[x] = false;
            }
            if (cacheI[1].locks[x].owner() == hilo.getId()) {
                cacheI[1].reservado[x] = false;
            }

            if (cacheD[0].locks[x].owner() == hilo.getId()) {
                cacheD[0].reservado[x] = false;
            }
            if (cacheD[1].locks[x].owner() == hilo.getId()) {
                cacheD[1].reservado[x] = false;
            }
        }
    }
}