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

    Hilillo(String inst, int pcHilillo, int pNucleo) {
        cantInst = inst.split("\n").length;
        pc = pcHilillo;
        nucleo = pNucleo;
        procesador = Procesador.getInstancia(1,5);
        ciclosReloj = 0;
    }

    @Override
    //Lo primero que hace cada hilillo es leer la primera instrucción. Una vez hecho, la ejecuta
    public void run() {
        int bloque, posCacheI, estadoHilillo = 1, numPalabra;

        while (estadoHilillo != 0) {
            System.out.println("Hilillo 1, estado " + estadoHilillo);
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
    }

    public int getNucleo() { return nucleo; }
}
