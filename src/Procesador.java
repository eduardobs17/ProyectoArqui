import java.util.Queue;

public class Procesador {
    private int cantHilos;
    //Nucleo 0
    Hilillo hilo0_1;
    Hilillo hilo0_2;
    // Caches = palabra1, palabra2, palabra3, palabra4, etiqueta, estado
    int cacheD0[][] = new int[8][6];
    int cacheI0[][] = new int[8][18];

    //Nucleo 1
    Hilillo hilo1;
    int cacheD1[][] = new int[4][6];
    int cacheI1[][] = new int[4][18];

    int ciclos_Reloj = 0;
    int contexto[][];

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     */
    public Procesador (int cant) {
        cantHilos = cant;
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
     * @param instruccion
     * @param h
     */
    public void loadI (int instruccion[], Hilillo h) {

    }

    public void llenarContextopc(int fila, int valor) {
        contexto[fila][32] = valor;
    }

    public void run(Queue<String> colaHilos, Queue<Integer> colaPCs) {
        hilo0_1 = new Hilillo(colaHilos.poll(), colaPCs.poll());
        buscarInstruccionEnCache(hilo0_1.pc, 0);
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
