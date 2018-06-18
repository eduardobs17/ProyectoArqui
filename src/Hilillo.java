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
            bloque = pc / 16;
            numPalabra = pc - (bloque*16);
            posCacheI = calcularPosCache(bloque, nucleo);

            if (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] != bloque
                    || (procesador.cacheInstrucciones[nucleo].valores[posCacheI][16] == bloque
                    && procesador.cacheInstrucciones[nucleo].valores[posCacheI][17] == 0)) {
                procesador.loadI(nucleo, posCacheI, pc);
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

    private int calcularPosCache(int numeroBloque, int nucleo) {
        if (nucleo == 0) {
            return numeroBloque % 8;
        } else {
            return numeroBloque % 4;
        }
    }
}
