import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class Procesador {
    private int ciclosReloj = 0;
    private final int cantHilos, quantum;
    private MemoriaPrincipal memoria;

    private int contexto[][];
    public CacheD[] cacheDatos = new CacheD[2];
    public CacheI[] cacheInstrucciones = new CacheI[2];
    private ReentrantLock bus[] = new ReentrantLock[2]; //Posición 0 es el bus para datos
                                                        //Posición 1 bus para instrucciones

    private Thread hilo0_1 = null;
    private Thread hilo0_2 = null;
    private Thread hilo1 = null;

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     */
    Procesador(int cant, int tamQuantum, MemoriaPrincipal m) {
        memoria = m;
        cantHilos = cant;
        quantum = tamQuantum;

        bus[0] = new ReentrantLock();
        bus[1] = new ReentrantLock();

        cacheDatos[0] = new CacheD(0);
        cacheDatos[1] = new CacheD(1);

        cacheInstrucciones[0] = new CacheI(0);
        cacheInstrucciones[1] = new CacheI(1);

        contexto = new int[cantHilos][33];
        for (int i = 0; i < cantHilos; i++) {
            for (int j = 0; j < 33; j++) {
                contexto[i][j] = 0;
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
        ciclosReloj++;
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
    public void loadI (int nucleo, int posCache, int posMem, Hilillo h) {
        //Se revisa si la posicion de cache ya está reservada
        if (!cacheInstrucciones[nucleo].reservado[posCache]) {
            cacheInstrucciones[nucleo].reservado[posCache] = true;

            //Se revisa bloque victima
            if (cacheInstrucciones[nucleo].valores[posCache][17] == 2) {
                if (!bus[1].isLocked()) {
                    bus[1].tryLock();
                    cacheInstrucciones[nucleo].locks[posCache].tryLock();

                    try {
                        //Se guarda el bloque en memoria
                        int[] bloque = new int[16];
                        for (int mf = 0; mf < 16; mf++) {
                            bloque[mf] = cacheInstrucciones[nucleo].valores[posCache][mf];
                        }
                        memoria.guardarBloqueInst(bloque, posMem);
                        for (int mf = 0; mf < 40; mf++) {
                            h.ciclosReloj++;

                            //Se aumentan los 40 ciclos de reloj
                        }
                    } finally {
                        bus[1].unlock();
                        cacheInstrucciones[nucleo].locks[posCache].unlock();
                    }
                }
            }
        }
    }

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
            if (cacheInstrucciones[0].valores[posCache][16] == bloque) { // Revisa etiqueta
                if (cacheInstrucciones[0].valores[posCache][17] != 0) {
                    return true;
                }
            }
        } else {
            posCache = bloque % 4;
            if (cacheInstrucciones[1].valores[posCache][4] == bloque) { // Revisa etiqueta
                if (cacheInstrucciones[1].valores[posCache][5] != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}