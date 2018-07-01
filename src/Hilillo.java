import java.util.concurrent.Phaser;

/** Clase que simula y ejecuta las instrucciones de los hilillos. */
public class Hilillo extends Thread {
    private Procesador procesador;
    private Phaser barreraInicio;
    private int nucleo;
    private int estadoHilillo;
    private int[] IR;

    public int[] registro;
    public int pc;
    public int idHilillo;
    public int ciclosRelojHilillo;

    /**
     * Constructor de la clase.
     * @param pNucleo Nucleo al que pertenece el hilillo.
     * @param bi Barrera usada para la sincronizacion.
     * @param id Identificado del hilillo.
     * @param contextoHilillo Contexto del hilillo; es decir, sus registros y pc.
     */
    Hilillo(int pNucleo, Phaser bi, int id, int[] contextoHilillo) {
        procesador = Procesador.getInstancia(1,1);

        barreraInicio = bi;
        barreraInicio.register();

        nucleo = pNucleo;
        estadoHilillo = 1;
        IR = new int[4];

        registro = new int[32];
        System.arraycopy(contextoHilillo, 0, registro, 0, 32);
        pc = contextoHilillo[32];
        idHilillo = id;
        ciclosRelojHilillo = 0;
    }

    /**
     * Método que ejecuta el hilillo. Carga instrucciones desde memoria, las lee y las ejecuta.
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

    /** Retorna el nucleo al que pertenece el hilillo. */
    public int getNucleo() {
        return nucleo;
    }

    /** Devuelve el estado del hilillo. */
    public int getEstadoHilillo() { return estadoHilillo; }

    /** Devuelve la instancia de la barrera usada. */
    public Phaser getBarreraI() { return barreraInicio; }

    /**
     * Metodo que se llama cuando se va a realizar un cambio de contexto.
     * Se elimina todos los campos que el hilillo reservó o registró.
     * @param cacheI Cache de instrucciones.
     * @param cacheD Cache de datos.
     * @param hilo Hilillo que va a realizar el cambio de contexto.
     */
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