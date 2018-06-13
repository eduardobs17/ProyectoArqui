public class Hilillo extends Thread {
    private Procesador procesador;
    public int[] registro = new int[32];
    public int[] IR = new int[4];
    public int[] estado = new int[4];
    public int pc = -1;
    private int nucleo = -1;

    public int quantum = 0;
    Hilillo(String inst, int pcHilillo, int pNucleo, Procesador p) {
        pc = pcHilillo;
        nucleo = pNucleo;
        procesador = p;
    }

    //Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta
    public void run() {
        procesador.cacheI0[0][0] = -1;
        int bloque = pc / 16;
        int posCacheI;
        if (nucleo == 0) {
            posCacheI = bloque % 8;
            if (procesador.cacheI0[posCacheI][16] == bloque) {

            } else {
                procesador.loadI();
            }
        } else {
            posCacheI = bloque % 4;
            if (procesador.cacheI1[posCacheI][16] == bloque) {

            } else {
                procesador.loadI();
            }
        }




    }
}
