import java.util.Queue;

public class Procesador {
    private final int cantHilos, quantum;
    //Nucleo 0
    private Thread hilo0_1 = null;
    Thread hilo0_2 = null;
    // Caches = palabra1, palabra2, palabra3, palabra4, etiqueta, estado
    public int cacheD0[][] = new int[8][6];
    public int cacheI0[][] = new int[8][18];

    //Nucleo 1
    private Thread hilo1 = null;
    public int cacheD1[][] = new int[4][6];
    public int cacheI1[][] = new int[4][18];

    private int ciclos_Reloj = 0;
    private int contexto[][];

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     */
    Procesador(int cant, int tamQuantum) {
        cantHilos = cant;
        quantum = tamQuantum;
        contexto = new int[cantHilos][33];

        for (int i = 0; i < cantHilos; i++) {
            for (int j = 0; j < 33; j++) {
                contexto[i][j] = 0;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                cacheD0[i][j] = 0;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 18; j++) {
                cacheI0[i][j] = 0;
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                cacheD1[i][j] = 0;
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 18; j++) {
                cacheI1[i][j] = 0;
            }
        }
    }

    /**
     * Este método manejará las instrucciones y su codificación
     * @param instruccion
     */
    public void ALU (int instruccion[], Hilillo h) {
        int y = instruccion[0];
        switch (y) {
            case 2: //JR
                h.pc = instruccion[1];
                break;
            case 3: //JAL
                h.registro[31] = h.pc;
                h.pc = h.pc + instruccion[3];
                break;
            case 4: //BEQZ
                if (instruccion[1] == 0) {
                    h.pc = h.pc + (4 * instruccion[3]);
                }
                break;
            case 5: //BNEZ
                if (instruccion[1] != 0) {
                    h.pc = h.pc + (4 * instruccion[3]);
                }
                break;
            case 8: //DADDI
                h.registro[instruccion[1]] = h.registro[instruccion[2]] + instruccion[3];
                break;
            case 12: //DMUL
                h.registro[instruccion[1]] = h.registro[instruccion[2]] * h.registro[instruccion[3]];
                break;
            case 14: //DDIV
                if (instruccion[3] != 0) {
                    h.registro[instruccion[1]] = h.registro[instruccion[2]] / h.registro[instruccion[3]];
                }
                break;
            case 32: //DADD
                h.registro[instruccion[1]] = h.registro[instruccion[2]] + h.registro[instruccion[3]];
                break;
            case 34: //DSUB
                h.registro[instruccion[1]] = h.registro[instruccion[2]] - h.registro[instruccion[3]];
                break;
            case 35: //LW
                loadD(instruccion, h);
                break;
            case 43: //SW
                storeD(instruccion, h);
                break;
            case 63: //FIN
                break;
        }
        ciclos_Reloj++;
    }

    /**
     * Metodo para guardar de memoria a registro
     * @param instruccion
     * @param h
     */
    public void loadD (int instruccion[], Hilillo h) {
        
    }

    /**
     * Metodo para guardar de registro a memoria
     * @param instruccion
     * @param h
     */
    public void storeD (int instruccion[], Hilillo h) {

    }

    /**
     * Metodo para guardar de memoria a registro
     */
    public void loadI () { }

    public void llenarContextopc(int fila, int valor) {
        contexto[fila][32] = valor;
    }

    public void run(Queue<String> colaHilos, Queue<Integer> colaPCs) {
        if (!colaHilos.isEmpty()) {
            Hilillo hilillo01 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 0, this);
            hilo0_1 =  new Thread(hilillo01);
        }
        if (!colaHilos.isEmpty()) {
            Hilillo hilillo1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 1, this);
            hilo1 =  new Thread(hilillo1);
        }
        if (hilo0_1 != null) { hilo0_1.run(); }
        if (hilo1 != null) { hilo1.run(); }
    }

    /**
     * 0 = invalido
     * 1 = compartido
     * 2 = modificado
     * @param pc
     * @param nucleo
     * @return
     */
    private boolean buscarInstruccionEnCache(int pc, int nucleo) {
        int bloque = pc / 16;
        int posCache;
        if (nucleo == 0) { //Busca en cache del nucleo 0
            posCache = bloque % 8;
            if (cacheI0[posCache][16] == bloque) { // Revisa etiqueta
                if (cacheI0[posCache][17] != 0) {
                    return true;
                }
            }
        } else {
            posCache = bloque % 4;
            if (cacheI1[posCache][4] == bloque) { // Revisa etiqueta
                if (cacheI1[posCache][5] != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
