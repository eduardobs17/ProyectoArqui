import java.util.concurrent.Phaser;

public class Hilillo extends Thread {
    private Procesador procesador;
    private int nucleo;
    private String instrucciones;
    private int estadoHilillo;
    private int ciclosRelojHilillo;
    private Phaser barreraInicio;
    private Phaser barreraFinal;
    private int[] IR;

    public int[] registro;
    public int pc;
    public int idHilillo;

    /**
     * Constructor del hilillo.
     * @param inst String con las instrucciones del hilillo.
     * @param pcHilillo PC del hilillo.
     * @param pNucleo Nucleo del hilillo.
     * @param bi Barrera de inicio para que los hilillos inicien a la vez.
     */
    Hilillo(String inst, int pcHilillo, int pNucleo, Phaser bi, Phaser bf, int id) {
        procesador = Procesador.getInstancia(1,1);
        nucleo = pNucleo;
        instrucciones = inst;
        estadoHilillo = 1;
        ciclosRelojHilillo = 0;
        barreraInicio = bi;
        barreraFinal = bf;
        IR = new int[4];

        registro = new int[32];
        pc = pcHilillo;
        idHilillo = id;
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

            //System.out.println("Nucleo 0, Hilillo: " + nucleo + ", Estado " + estadoHilillo + "\n");
            if (estadoHilillo != 0) {
                barreraInicio.arriveAndAwaitAdvance();
            } else {
                barreraInicio.arriveAndDeregister();
            }
            ciclosRelojHilillo++;
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

    public void cambiarBarreraI(Phaser newBarreraI) {
        barreraInicio = newBarreraI;
    }

    public int getCiclosRelojHilillo() { return ciclosRelojHilillo; }
}