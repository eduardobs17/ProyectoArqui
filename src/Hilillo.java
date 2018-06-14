public class Hilillo extends Thread {
    private Procesador procesador;
    public int[] registro = new int[32];
    public int[] IR = new int[4];
    public int[] estado = new int[4];
    public int pc = -1;
    private int nucleo;
    public int ciclosReloj;

    public int quantum = 0;
    Hilillo(String inst, int pcHilillo, int pNucleo, Procesador p) {
        pc = pcHilillo;
        nucleo = pNucleo;
        procesador = p;
        ciclosReloj = 0;
    }

    //Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta
    public void run() {
        int bloque = pc / 16;
        int posCacheI;
        if (nucleo == 0) {
            posCacheI = bloque % 8;
            if (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] == bloque) {

            } else { procesador.loadI(0, posCacheI, pc, this); }
        } else {
            posCacheI = bloque % 4;
            if (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] == bloque) {

            } else { procesador.loadI(1, posCacheI, pc, this); }
        }
    }
}
