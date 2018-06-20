import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 0 = invalido
 * 1 = compartido
 * 2 = modificado
 */
public class Procesador {
    private static Procesador procesador;

    private MemoriaPrincipal memoria;

    private Hilillo hilo0_1 = null;
    private Hilillo hilo0_2 = null;
    private Hilillo hilo1 = null;

    public final int cantHilos, quantum;

    private int contexto[][];
    public CacheD[] cacheDatos = new CacheD[2];
    public CacheI[] cacheInstrucciones = new CacheI[2];
    private ReentrantLock busI = new ReentrantLock();
    private ReentrantLock busD = new ReentrantLock();

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     * @param cant
     * @param tamQuantum
     */
    private Procesador(int cant, int tamQuantum) {
        memoria = MemoriaPrincipal.getInstancia();
        cantHilos = cant;
        quantum = tamQuantum;

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

    public static Procesador getInstancia(int cantH, int tamQ) {
        if (procesador == null) {
            procesador = new Procesador(cantH, tamQ);
        }
        return procesador;
    }

    @Override
    public Procesador clone() {
        try {
            throw new CloneNotSupportedException();
        } catch (CloneNotSupportedException ignored) { }
        return null;
    }

    public void run(Queue<String> colaHilos, Queue<Integer> colaPCs, CyclicBarrier bi, CyclicBarrier bf, int cantHilos, int quantum) {
        if (!colaHilos.isEmpty()) {
            hilo0_1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 0, bi, bf, cantHilos, quantum);
            hilo0_1.start();
        }

        if (!colaHilos.isEmpty()) {
            hilo1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 1, bi, bf, cantHilos, quantum);
            hilo1.start();
        }

        /*while (!colaHilos.isEmpty()) {

        }*/
    }

    /**
     * Este método manejará las instrucciones y su codificación
     * @param instruccion
     * @param h
     */
    public int ALU (int instruccion[], Hilillo h) {
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
                if (h.registro[instruccion[1]] == 0) {
                    h.pc = h.pc + (4 * instruccion[3]);
                }
                break;
            case 5: //BNEZ
                if (h.registro[instruccion[1]] != 0) {
                    h.pc = h.pc + (4 * instruccion[3]);
                }
                break;
            case 8: //DADDI
                h.registro[instruccion[2]] = h.registro[instruccion[1]] + instruccion[3];
                break;
            case 12: //DMUL
                h.registro[instruccion[3]] = h.registro[instruccion[1]] * h.registro[instruccion[2]];
                break;
            case 14: //DDIV
                if (h.registro[instruccion[2]] == 0) { //Divisor es cero
                    h.registro[instruccion[3]] = 0;
                } else {
                    h.registro[instruccion[3]] = h.registro[instruccion[1]] / h.registro[instruccion[2]];
                }
                break;
            case 32: //DADD
                h.registro[instruccion[3]] = h.registro[instruccion[1]] + h.registro[instruccion[2]];
                break;
            case 34: //DSUB
                h.registro[instruccion[3]] = h.registro[instruccion[1]] - h.registro[instruccion[2]];
                break;
            case 35: //LW
                loadD(instruccion[2], h.registro[instruccion[1]] + instruccion[3], h.getNucleo(), h.registro);
                break;
            case 43: //SW
                storeD(instruccion, h);
                break;
            case 63: //FIN
                return 0;
        }
        return 1;
    }

    /**
     * Metodo para LOADD: guardar de memoria a registro.
     * 35 Y X n = RX   <--   M[n + (RY)]
     * @param registro
     * @param posMemoria
     * @param nucleo
     * @param registros
     */
    public void loadD (int registro, int posMemoria, int nucleo, int[] registros) {
        int bloque = posMemoria / 16;
        int palabra = (posMemoria - 16 * bloque) / 4;

        int posCache = calcularPosCache(bloque, nucleo);
        int otroNucleo = (nucleo + 1) % 2;

        CacheD copiaCache = cacheDatos[nucleo];
        CacheD copiaOtraCache = cacheDatos[otroNucleo];

        if (!copiaCache.reservado[posCache]) {                                                          //Se revisa si la posicion de cache está reservada
            copiaCache.reservado[posCache] = true;

            if (copiaCache.valores[posCache][4] == bloque) {                                            //Se busca el bloque en la cache
                if ((copiaCache.valores[posCache][5] == 1) || (copiaCache.valores[posCache][5] == 2)) { //Se revisa el estado del bloque
                                                                                                        //si estado = 1 (C) o estado = 2 (M), se escribe el registro
                    registros[registro] = copiaCache.valores[posCache][palabra];
                    copiaCache.reservado[posCache] = false;
                } else {                                                                                //Si el bloque es invalido
                    if (!busD.isLocked()) {                                                             //Se revisa estado del bus
                        busD.tryLock();
                        try {
                            int posCache2 = calcularPosCache(bloque, otroNucleo);

                            if (!copiaOtraCache.reservado[posCache2]) {
                                copiaOtraCache.reservado[posCache2] = true;

                                if (copiaOtraCache.valores[posCache2][4] == bloque) {
                                    if (copiaOtraCache.valores[posCache][5] == 2) {
                                        copiaOtraCache.locks[posCache2].tryLock();
                                        copiaCache.locks[posCache].tryLock();
                                        try {
                                            guardarBloqueEnMemoriaD(copiaOtraCache.valores[posCache]);
                                            guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache2, copiaCache, posCache);
                                        } finally {
                                            copiaOtraCache.locks[posCache2].unlock();
                                            copiaCache.locks[posCache].unlock();
                                        }
                                    } else  {
                                        copiaCache.locks[posCache].tryLock();
                                        try {
                                            guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                        } finally {
                                            copiaCache.locks[posCache].unlock();
                                        }
                                    }
                                } else {
                                    copiaCache.locks[posCache].tryLock();
                                    try {
                                        guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                    } finally {
                                        copiaCache.locks[posCache].unlock();
                                    }
                                }
                                registros[registro] = copiaCache.valores[posCache][palabra];
                                copiaOtraCache.reservado[posCache2] = false;
                            }
                        } finally {
                            busD.unlock();
                            copiaCache.reservado[posCache] = false;
                        }
                    } else { //Bus no disponible
                        //LW vuelve a empezar
                    }
                }
            } else {                                                                                    //Bloque no está en cache
                //Se revisa bloque victima
                if (copiaCache.valores[posCache][5] == 2) {
                    guardarBloqueEnMemoriaD(copiaCache.valores[posCache]);
                }

                if (!busD.isLocked()) {                                                                 //Se revisa estado del bus
                    busD.tryLock();
                    try {
                        int posCache2 = calcularPosCache(bloque, otroNucleo);

                        if (!copiaOtraCache.reservado[posCache2]) {
                            copiaOtraCache.reservado[posCache2] = true;

                            if (copiaOtraCache.valores[posCache2][4] == bloque) {
                                if (copiaOtraCache.valores[posCache2][5] == 2) {
                                    copiaOtraCache.locks[posCache2].tryLock();
                                    copiaCache.locks[posCache].tryLock();
                                    try {
                                        guardarBloqueEnMemoriaD(copiaOtraCache.valores[posCache]);
                                        guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache2, copiaCache, posCache);
                                    } finally {
                                        copiaOtraCache.locks[posCache2].unlock();
                                        copiaCache.locks[posCache].unlock();
                                    }
                                }
                            } else {
                                copiaCache.locks[posCache].tryLock();
                                try {
                                    guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                } finally {
                                    copiaCache.locks[posCache].unlock();
                                }
                            }
                            registros[registro] = copiaCache.valores[posCache][palabra];
                            copiaOtraCache.reservado[posCache2] = false;
                        }
                    } finally {
                        busD.unlock();
                        copiaCache.reservado[posCache] = false;
                    }
                } else { //Bus no disponible
                    //LW vuelve a empezar
                }
            }
        }
    }

    /**
     * Metodo para STORE: guardar de registro a memoria
     * 43 Y X n = M[n + (RY)]   <--   RX
     * @param instruccion
     * @param h
     */
    public void storeD (int instruccion[], Hilillo h) {

    }

    /**
     * Metodo para LOADI: guardar de memoria a cache.
     * @param nucleo
     * @param posCache
     * @param posMem
     * @param h
     */
    public void loadI (int nucleo, int posCache, int posMem, Hilillo h) {
        int bloqueMem = posMem / 16;
        int bloqueEnMemoria = bloqueMem - 24;

        if (!cacheInstrucciones[nucleo].reservado[posCache]) { //Se revisa si la posicion de cache ya está reservada
            cacheInstrucciones[nucleo].reservado[posCache] = true;

            if (cacheInstrucciones[nucleo].valores[posCache][17] == 2) { //Se revisa bloque victima
                if (!busI.isLocked()) {
                    busI.tryLock();
                    cacheInstrucciones[nucleo].locks[posCache].tryLock();

                    try { //Se guarda el bloque en memoria
                        System.arraycopy(cacheInstrucciones[nucleo].valores[posCache], 0, memoria.memInstrucciones[bloqueEnMemoria].palabra, 0, 16);
                        cacheInstrucciones[nucleo].valores[posCache][17] = 1;
                        for (int mf = 0; mf < 40; mf++) {
                            h.ciclosReloj++;
                        }
                    } finally {
                        busI.unlock();
                        cacheInstrucciones[nucleo].locks[posCache].unlock();
                    }
                }
            }

            if (!busI.isLocked()) {
                busI.tryLock();
                cacheInstrucciones[nucleo].locks[posCache].tryLock();

                try {
                    System.arraycopy(memoria.memInstrucciones[bloqueEnMemoria].palabra, 0, cacheInstrucciones[nucleo].valores[posCache], 0, 16);
                    cacheInstrucciones[nucleo].valores[posCache][16] = bloqueMem;
                    for (int mf = 0; mf < 40; mf++) {
                        h.ciclosReloj++;
                    }
                    cacheInstrucciones[nucleo].valores[posCache][17] = 1;
                } finally {
                    busI.unlock();
                    cacheInstrucciones[nucleo].locks[posCache].unlock();
                }
            }
            cacheInstrucciones[nucleo].reservado[posCache] = false;
        }
    }

    /**
     * Metodo para llenar el contexto de los hilillos.
     * @param fila
     * @param pc
     */
    public void llenarContextopc(int fila, int pc) {
        contexto[fila][32] = pc;
    }

    public int calcularPosCache(int numeroBloque, int nucleo) {
        if (nucleo == 0) {
            return numeroBloque % 8;
        } else {
            return numeroBloque % 4;
        }
    }

    /**
     * Metodo para guardar un bloque en memoria de datos.
     * @param array
     */
    private void guardarBloqueEnMemoriaD(int[] array) {
        int bloque = array[4];
        System.arraycopy(array, 0, memoria.memDatos[bloque].palabra, 0, 4);
    }

    /**
     * Metodo para guardar bloque en cache desde cache de datos.
     * @param cacheF
     * @param posCacheF
     * @param cacheD
     * @param posCacheD
     */
    private void guardarBloqueEnCacheDesdeCacheD(CacheD cacheF, int posCacheF, CacheD cacheD, int posCacheD) {
        System.arraycopy(cacheF.valores[posCacheF], 0, cacheD.valores[posCacheD], 0, 5);
        cacheD.valores[posCacheD][5] = 1;
        cacheF.valores[posCacheF][5] = 1;
    }

    /**
     * Metodo para guardar bloque en cache desde la memoria de datos.
     * @param numBloque
     * @param cache
     * @param posCache
     */
    private void guardarBloqueEnCacheDesdeMemoriaD(int numBloque, CacheD cache, int posCache) {
        for (int x = 0; x < 4; x++) {
            cache.valores[posCache][x] = memoria.memDatos[numBloque].palabra[x];
        }
        cache.valores[posCache][4] = numBloque;
        cache.valores[posCache][5] = 1;
    }
}