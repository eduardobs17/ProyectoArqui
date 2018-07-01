import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Phaser;

/**
 * 0 = invalido
 * 1 = compartido
 * 2 = modificado
 */
public class Procesador {
    private static Procesador procesador;

    private MemoriaPrincipal memoria;

    private Hilillo hilo0 = null;
    private Hilillo hilo1 = null;

    private final int quantum;
    private int ciclosReloj;

    //contexto[i][0] = pc; contexto[i][1-32] = registros
    public int contexto[][];
    public CacheD[] cacheDatos = new CacheD[2];
    public CacheI[] cacheInstrucciones = new CacheI[2];
    private MyReentrantLock busI = new MyReentrantLock();
    private MyReentrantLock busD = new MyReentrantLock();

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     * @param cant Cantidad de hilillos que se van a ejecutar.
     * @param tamQuantum Tamaño del quantum del hilillo.
     */
    private Procesador(int cant, int tamQuantum) {
        memoria = MemoriaPrincipal.getInstancia();
        quantum = tamQuantum;
        ciclosReloj = 0;

        cacheDatos[0] = new CacheD();
        cacheDatos[1] = new CacheD();

        cacheInstrucciones[0] = new CacheI();
        cacheInstrucciones[1] = new CacheI();

        contexto = new int[cant][33];
        for (int i = 0; i < cant; i++) {
            for (int j = 0; j < 33; j++) {
                contexto[i][j] = 0;
            }
        }
    }

    /**
     * Metodo Singleton para controlar que solo se cree un objeto procesador.
     * @param cantH Cantidad de hilillos que se van a ejecutar.
     * @param tamQ Tamaño de quantum del hilillo.
     * @return Devuelve el procesador si este ya existe.
     */
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

    /**
     * Metodo que inicia el programa y manda a ejecutar los Hilillos.
     * @param colaIDs cola con el PC de los hilos que se van a ejecutar.
     * @param barreraI barreraInicio para manejar ciclo de reloj para que los hilillos inicien a la vez.
     * @param barreraF barreraFinal para manejar ciclo de reloj para que los hilillos finalicen a la vez.
     * @param modalidad Modalidad de ejecución, si es 0 el programa corre con normalidad,
     *                  si es 1 se agregan delay de 1 segundo para que el programa se ejecute despacio.
     */
    public void run(Queue<Integer> colaIDs, Phaser barreraI, Phaser barreraF, int modalidad) {
        while (true) {
            if (!colaIDs.isEmpty() && hilo0 == null) {
                int id = colaIDs.poll();
                hilo0 = new Hilillo(0, barreraI, barreraF, id, contexto[id], true);
                hilo0.setName("Hilo0");
                hilo0.start();
            }
            if (!colaIDs.isEmpty() && hilo1 == null) {
                int id = colaIDs.poll();
                hilo1 = new Hilillo(1, barreraI, barreraF, id, contexto[id], true);
                hilo1.setName("Hilo1");
                hilo1.start();
            }

            barreraI.arriveAndAwaitAdvance();
            ciclosReloj++;
            System.out.println("Ciclo de reloj: " + ciclosReloj);
            if (hilo0 != null) {
                System.out.println("Nucleo 0: Hilillo " + hilo0.idHilillo + ", Estado " + hilo0.getEstadoHilillo());
            }
            if (hilo1 != null) {
                System.out.println("Nucleo 1: Hilillo " + hilo1.idHilillo + ", Estado " + hilo1.getEstadoHilillo());
            }
            System.out.println("\n");

            if (modalidad == 1) {
                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            if (hilo0 != null && hilo0.getEstadoHilillo() == 0) {
                try {
                    hilo0.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hilo0 = null;
            }
            if (hilo1 != null && hilo1.getEstadoHilillo() == 0) {
                try {
                    hilo1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hilo1 = null;
            }

            if (hilo0 != null && hilo0.ciclosRelojHilillo > quantum) {
                if (!colaIDs.isEmpty()) {
                    hilo0.suspend();
                    hilo0.desregistrarHilillo(cacheInstrucciones, cacheDatos, hilo0);
                    hilo0.stop();
                    System.arraycopy(hilo0.registro, 0, contexto[hilo0.idHilillo], 0, 32);
                    contexto[hilo0.idHilillo][32] = hilo0.pc;
                    colaIDs.add(hilo0.idHilillo);
                    hilo0 = null;

                    int id = colaIDs.poll();
                    hilo0 = new Hilillo(0, barreraI, barreraF, id, contexto[id], false);
                    hilo0.setName("Hilo0");
                    hilo0.start();
                }
            }

            if (hilo1 != null && hilo1.ciclosRelojHilillo > quantum) {
                if (!colaIDs.isEmpty()) {
                    /*hilo1.stop();
                    hilo1.desregistrarHilillo(busI, busD, cacheInstrucciones, cacheDatos);
                    System.arraycopy(hilo1.registro, 0, contexto[hilo1.idHilillo], 0, 32);
                    contexto[hilo1.idHilillo][32] = hilo1.pc;
                    colaIDs.add(hilo1.idHilillo);

                    hilo1 = null;*/
                }
            }

            if (hilo0 == null && hilo1 == null) {
                if (colaIDs.isEmpty()) {
                    return;
                }
            }
            barreraF.arriveAndAwaitAdvance();
        }
    }

    /**
     * Metodo que contiene la logica de las instrucciones.
     * @param instruccion Es el IR (instruccion actual) del hilillo.
     * @param h Es el hilillo.
     * @return Devuelve el PC del hilillo.
     */
    public int ALU (int instruccion[], Hilillo h) {
        int y = instruccion[0];
        switch (y) {
            case 2: //JR
                h.pc = h.registro[instruccion[1]];
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
                int res = loadD(instruccion[2], h.registro[instruccion[1]] + instruccion[3], h.getNucleo(), h.registro, h);
                if (res == -1) {
                    return -1;
                }
                break;
            case 43: //SW
                storeD(instruccion[2], h.registro[instruccion[1]] + instruccion[3], h.getNucleo(), h.registro, h);
                break;
            case 63: //FIN
                return 0;
        }
        return 1;
    }

    /**
     * Metodo para LoadD que carga datos de memoria a registro, toma en cuenta los fallos de cache.
     * @param registro El numero de registro en el cual debe escribir.
     * @param posMemoria Posicion de memoria del bloque.
     * @param nucleo Nucleo del hilillo.
     * @param registros Los registros del hilillo.
     */
    private int loadD (int registro, int posMemoria, int nucleo, int[] registros, Hilillo h) {
        int bloque = posMemoria / 16;
        int palabra = (posMemoria % 16) / 4;

        int posCache = bloque % 4;
        int otroNucleo = (nucleo + 1) % 2;

        CacheD copiaCache = cacheDatos[nucleo];
        CacheD copiaOtraCache = cacheDatos[otroNucleo];

        if (!copiaCache.reservado[posCache]) {                                                          //Se revisa si la posicion de cache está reservada
            copiaCache.reservado[posCache] = true;

            if (copiaCache.valores[posCache][4] == bloque) {                                            //Se busca el bloque en la cache
                if ((copiaCache.valores[posCache][5] == 1) || (copiaCache.valores[posCache][5] == 2)) { //Se revisa el estado del bloque
                                                                                                        //si estado = 1 (C) o estado = 2 (M), se escribe el registro
                    copiaCache.locks[posCache].tryLock();
                    try {
                        registros[registro] = copiaCache.valores[posCache][palabra];
                    } finally {
                        copiaCache.locks[posCache].unlock();
                        copiaCache.reservado[posCache] = false;
                    }
                } else {                                                                                //Si el bloque es invalido
                    if (!busD.isLocked()) {                                                             //Se revisa estado del bus
                        busD.tryLock();
                        try {
                            if (!copiaOtraCache.reservado[posCache]) {
                                copiaOtraCache.reservado[posCache] = true;

                                if (copiaOtraCache.valores[posCache][4] == bloque) {
                                    if (copiaOtraCache.valores[posCache][5] == 2) {
                                        copiaOtraCache.locks[posCache].tryLock();
                                        copiaCache.locks[posCache].tryLock();
                                        try {
                                            for (int i = 0; i < 40; i++) {
                                                h.getBarreraI().arriveAndAwaitAdvance();
                                                h.ciclosRelojHilillo++;
                                                h.getBarreraF().arriveAndAwaitAdvance();
                                            }
                                            guardarBloqueEnMemoriaD(copiaOtraCache.valores[posCache]);
                                            guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache, copiaCache, posCache);
                                        } finally {
                                            copiaOtraCache.locks[posCache].unlock();
                                            copiaCache.locks[posCache].unlock();
                                        }
                                    } else  {
                                        copiaCache.locks[posCache].tryLock();
                                        try {
                                            for (int i = 0; i < 40; i++) {
                                                h.getBarreraI().arriveAndAwaitAdvance();
                                                h.ciclosRelojHilillo++;
                                                h.getBarreraF().arriveAndAwaitAdvance();
                                            }
                                            guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                        } finally {
                                            copiaCache.locks[posCache].unlock();
                                        }
                                    }
                                } else {
                                    copiaCache.locks[posCache].tryLock();
                                    try {
                                        for (int i = 0; i < 40; i++) {
                                            h.getBarreraI().arriveAndAwaitAdvance();
                                            h.ciclosRelojHilillo++;
                                            h.getBarreraF().arriveAndAwaitAdvance();
                                        }
                                        guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                    } finally {
                                        copiaCache.locks[posCache].unlock();
                                    }
                                }
                                registros[registro] = copiaCache.valores[posCache][palabra];
                                copiaOtraCache.reservado[posCache] = false;
                            }
                        } finally {
                            if (busD.isHeldByCurrentThread()) {
                                busD.unlock();
                            }
                        }
                    } else { //Bus no disponible
                        copiaCache.reservado[posCache] = false;
                        return -1;
                    }
                }
            } else {
                if (!busD.isLocked()) {                                                                 //Se revisa estado del bus
                    busD.tryLock();
                    try {
                        if (copiaCache.valores[posCache][5] == 2) {
                            for (int i = 0; i < 40; i++) {
                                h.getBarreraI().arriveAndAwaitAdvance();
                                h.ciclosRelojHilillo++;
                                h.getBarreraF().arriveAndAwaitAdvance();
                            }
                            guardarBloqueEnMemoriaD(copiaCache.valores[posCache]);
                            copiaCache.valores[posCache][5] = 1;
                        }

                        if (!copiaOtraCache.reservado[posCache]) {
                            copiaOtraCache.reservado[posCache] = true;

                            if (copiaOtraCache.valores[posCache][4] == bloque) {
                                if (copiaOtraCache.valores[posCache][5] == 2) {
                                    copiaOtraCache.locks[posCache].tryLock();
                                    copiaCache.locks[posCache].tryLock();
                                    try {
                                        for (int i = 0; i < 40; i++) {
                                            h.getBarreraI().arriveAndAwaitAdvance();
                                            h.ciclosRelojHilillo++;
                                            h.getBarreraF().arriveAndAwaitAdvance();
                                        }
                                        guardarBloqueEnMemoriaD(copiaOtraCache.valores[posCache]);
                                        guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache, copiaCache, posCache);
                                        copiaCache.valores[posCache][5] = 1;
                                        copiaOtraCache.valores[posCache][5] = 1;
                                    } finally {
                                        copiaOtraCache.locks[posCache].unlock();
                                        copiaCache.locks[posCache].unlock();
                                    }
                                } else {
                                    copiaCache.locks[posCache].tryLock();
                                    try {
                                        if (copiaOtraCache.valores[posCache][5] == 0) {
                                            for (int i = 0; i < 40; i++) {
                                                h.getBarreraI().arriveAndAwaitAdvance();
                                                h.ciclosRelojHilillo++;
                                                h.getBarreraF().arriveAndAwaitAdvance();
                                            }
                                            guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                        } else {
                                            guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache, copiaCache, posCache);
                                        }
                                        copiaCache.valores[posCache][5] = 1;
                                    } finally {
                                        copiaCache.locks[posCache].unlock();
                                    }
                                }
                            } else {
                                copiaCache.locks[posCache].tryLock();
                                try {
                                    for (int i = 0; i < 40; i++) {
                                        h.getBarreraI().arriveAndAwaitAdvance();
                                        h.ciclosRelojHilillo++;
                                        h.getBarreraF().arriveAndAwaitAdvance();
                                    }
                                    guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                    copiaCache.valores[posCache][5] = 1;
                                } finally {
                                    copiaCache.locks[posCache].unlock();
                                }
                            }
                            registros[registro] = copiaCache.valores[posCache][palabra];
                            copiaOtraCache.reservado[posCache] = false;
                        }
                    } finally {
                        if (busD.isHeldByCurrentThread()) {
                            busD.unlock();
                        }
                    }
                } else { //Bus no disponible
                    copiaCache.reservado[posCache] = false;
                    return -1;
                }
            }
            copiaCache.reservado[posCache] = false;
        } else {
            return -1;
        }
        return 0;
    }

    /**
     * Metodo StoreD para cargar datos de registro a memoria, toma en cuenta los fallos de cache.
     * @param registro Registro desde el cual se va a escribir en memoria.
     * @param posMemoria Posicion de memoria del bloque.
     * @param nucleo Nucleo del hilillo.
     * @param registros Los registros del hilillo.
     */
    private void storeD (int registro, int posMemoria, int nucleo, int[] registros, Hilillo h) {
        int bloque = posMemoria / 16;
        int palabra = (posMemoria % 16) / 4;

        int posCache = bloque % 4;
        int otroNucleo = (nucleo + 1) % 2;

        CacheD copiaCache = cacheDatos[nucleo];
        CacheD copiaOtraCache = cacheDatos[otroNucleo];

        if (!copiaCache.reservado[posCache]) {
            copiaCache.reservado[posCache] = true;

            if (copiaCache.valores[posCache][4] == bloque) {
                if (copiaCache.valores[posCache][5] == 2) {
                    copiaCache.locks[posCache].tryLock();

                    try {
                        copiaCache.valores[posCache][palabra] = registros[registro];
                    } finally {
                        copiaCache.locks[posCache].unlock();
                    }
                } else if (copiaCache.valores[posCache][5] == 1) {
                    if (!busD.isLocked()) {
                        busD.tryLock();

                        try {
                            if (!copiaOtraCache.reservado[posCache]) {
                                copiaOtraCache.reservado[posCache] = true;

                                if (copiaOtraCache.valores[posCache][4] == bloque) {
                                    if (copiaOtraCache.valores[posCache][5] == 0) {
                                        copiaCache.locks[posCache].tryLock();

                                        try {
                                            copiaCache.valores[posCache][palabra] = registros[registro];
                                        } finally {
                                            copiaCache.locks[posCache].unlock();
                                        }
                                    } else if (copiaOtraCache.valores[posCache][5] == 1) {
                                        copiaCache.locks[posCache].tryLock();
                                        copiaOtraCache.locks[posCache].tryLock();

                                        try {
                                            copiaOtraCache.valores[posCache][5] = 0;
                                            copiaCache.valores[posCache][palabra] = registros[registro];
                                            copiaCache.valores[posCache][5] = 2;
                                        } finally {
                                            copiaCache.locks[posCache].unlock();
                                            copiaOtraCache.locks[posCache].unlock();
                                        }
                                    }
                                }
                                copiaOtraCache.reservado[posCache] = false;
                            }
                        } finally {
                            if (busD.isHeldByCurrentThread()) {
                                busD.unlock();
                            }
                        }
                    }
                }
            } else { //Fallo de caché
                if (!busD.isLocked()) {
                    busD.tryLock();

                    try {
                        //Se revisa bloque victima
                        if (copiaCache.valores[posCache][5] == 2) {
                            for (int i = 0; i < 40; i++) {
                                h.getBarreraI().arriveAndAwaitAdvance();
                                h.ciclosRelojHilillo++;
                                h.getBarreraF().arriveAndAwaitAdvance();
                            }
                            guardarBloqueEnMemoriaD(copiaCache.valores[posCache]);
                        }

                        if (!copiaOtraCache.reservado[posCache]) {
                            copiaOtraCache.reservado[posCache] = true;

                            if (copiaOtraCache.valores[posCache][4] == bloque) {
                                if (copiaOtraCache.valores[posCache][5] == 2) {
                                    copiaOtraCache.locks[posCache].tryLock();
                                    copiaCache.locks[posCache].tryLock();

                                    try {
                                        for (int i = 0; i < 40; i++) {
                                            h.getBarreraI().arriveAndAwaitAdvance();
                                            h.ciclosRelojHilillo++;
                                            h.getBarreraF().arriveAndAwaitAdvance();
                                        }
                                        guardarBloqueEnMemoriaD(copiaOtraCache.valores[posCache]);
                                        guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache, copiaCache, posCache);
                                        copiaCache.valores[posCache][palabra] = registros[registro];
                                        copiaCache.valores[posCache][5] = 2;
                                        copiaOtraCache.valores[posCache][5] = 0;
                                    } finally {
                                        copiaOtraCache.locks[posCache].unlock();
                                        copiaCache.locks[posCache].unlock();
                                    }
                                } else {
                                    copiaCache.locks[posCache].tryLock();

                                    try {
                                        for (int i = 0; i < 40; i++) {
                                            h.getBarreraI().arriveAndAwaitAdvance();
                                            h.ciclosRelojHilillo++;
                                            h.getBarreraF().arriveAndAwaitAdvance();
                                        }
                                        guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                        copiaCache.valores[posCache][palabra] = registros[registro];
                                        copiaCache.valores[posCache][5] = 2;
                                        copiaOtraCache.valores[posCache][5] = 0;
                                    } finally {
                                        copiaCache.locks[posCache].unlock();
                                    }
                                }
                            } else {
                                copiaCache.locks[posCache].tryLock();

                                try {
                                    for (int i = 0; i < 40; i++) {
                                        h.getBarreraI().arriveAndAwaitAdvance();
                                        h.ciclosRelojHilillo++;
                                        h.getBarreraF().arriveAndAwaitAdvance();
                                    }
                                    guardarBloqueEnCacheDesdeMemoriaD(bloque, copiaCache, posCache);
                                    copiaCache.valores[posCache][palabra] = registros[registro];
                                    copiaCache.valores[posCache][5] = 2;
                                } finally {
                                    copiaCache.locks[posCache].unlock();
                                }
                            }
                            copiaOtraCache.reservado[posCache] = false;
                        }
                    } finally {
                        if (busD.isHeldByCurrentThread()) {
                            busD.unlock();
                        }
                    }
                }
            }
            copiaCache.reservado[posCache] = false;
        }
    }

    /**
     * Metodo LoadI para cargar las instrucciones de la memoria a la cache, toma en cuenta los fallos de cache.
     * @param nucleo Nucleo del hilillo.
     * @param posCache Posicion en la cache.
     * @param posMem PC del hilillo (posicion en memoria).
     * @param h Es el hilillo.
     */
    public int loadI (int nucleo, int posCache, int posMem, Hilillo h) {
        int bloqueMem = posMem / 16;
        int bloqueEnMemoria = bloqueMem - 24;

        if (!cacheInstrucciones[nucleo].reservado[posCache]) { //Se revisa si la posicion de cache ya está reservada
            cacheInstrucciones[nucleo].reservado[posCache] = true;

            if (cacheInstrucciones[nucleo].valores[posCache][17] == 2) { //Se revisa bloque victima
                if (!busI.isLocked()) {
                    busI.tryLock();
                    cacheInstrucciones[nucleo].locks[posCache].tryLock();

                    try { //Se guarda el bloque en memoria
                        for (int i = 0; i < 40; i++) {
                            h.getBarreraI().arriveAndAwaitAdvance();
                            h.ciclosRelojHilillo++;
                            h.getBarreraF().arriveAndAwaitAdvance();
                        }
                        System.arraycopy(cacheInstrucciones[nucleo].valores[posCache], 0, memoria.memInstrucciones[bloqueEnMemoria].palabra, 0, 16);
                        cacheInstrucciones[nucleo].valores[posCache][17] = 1;
                    } finally {
                        if (busI.isHeldByCurrentThread()) {
                            busI.unlock();
                        }
                        cacheInstrucciones[nucleo].locks[posCache].unlock();
                    }
                } else {
                    cacheInstrucciones[nucleo].reservado[posCache] = false;
                    return -1;
                }
            }

            if (!busI.isLocked()) {
                busI.tryLock();
                cacheInstrucciones[nucleo].locks[posCache].tryLock();

                try {
                    for (int i = 0; i < 40; i++) {
                        h.getBarreraI().arriveAndAwaitAdvance();
                        h.ciclosRelojHilillo++;
                        h.getBarreraF().arriveAndAwaitAdvance();
                    }
                    System.arraycopy(memoria.memInstrucciones[bloqueEnMemoria].palabra, 0, cacheInstrucciones[nucleo].valores[posCache], 0, 16);
                    cacheInstrucciones[nucleo].valores[posCache][16] = bloqueMem;
                    cacheInstrucciones[nucleo].valores[posCache][17] = 1;
                } finally {
                    if (busI.isHeldByCurrentThread()) {
                        busI.unlock();
                    }
                    cacheInstrucciones[nucleo].locks[posCache].unlock();
                }
            }  else {
                cacheInstrucciones[nucleo].reservado[posCache] = false;
                return -1;
            }
            cacheInstrucciones[nucleo].reservado[posCache] = false;
        } else {
            return -1;
        }
        return 0;
    }

    /**
     * Metodo para llenar el PC del hilillo en su propio contexto.
     * @param fila El numero del hilillo.
     * @param pc PC del hilillo.
     */
    public void llenarContextopc(int fila, int pc) {
        contexto[fila][32] = pc;
    }

    /**
     * Copia el bloque desde la cache a la memoria de datos.
     * @param array Es el bloque que esta en la cache.
     */
    private void guardarBloqueEnMemoriaD(int[] array) {
        int bloque = array[4];
        System.arraycopy(array, 0, memoria.memDatos[bloque].palabra, 0, 4);
    }

    /**
     * Método que guarda en cache un bloque proveniente desde la cache del otro nucleo.
     * @param cacheF La cache que contiene el bloque que se guardará.
     * @param posCacheF La posición del bloque en la cache fuente.
     * @param cacheD La cache en la que se guardara el bloque.
     * @param posCacheD La posición en la que se guardara el bloque.
     */
    private void guardarBloqueEnCacheDesdeCacheD(CacheD cacheF, int posCacheF, CacheD cacheD, int posCacheD) {
        System.arraycopy(cacheF.valores[posCacheF], 0, cacheD.valores[posCacheD], 0, 5);
        cacheD.valores[posCacheD][5] = 1;
        cacheF.valores[posCacheF][5] = 1;
    }

    /**
     * Método que guarda en cache un bloque proveniente de la memoria de datos.
     * @param numBloque Numero de bloque de la memoria que se pasara a la cache.
     * @param cache La cache en la que se guardara el bloque.
     * @param posCache Posicion de la cache en la que se guardara el bloque.
     * */
    private void guardarBloqueEnCacheDesdeMemoriaD(int numBloque, CacheD cache, int posCache) {
        System.arraycopy(memoria.memDatos[numBloque].palabra, 0, cache.valores[posCache], 0, 4);
        cache.valores[posCache][4] = numBloque;
        cache.valores[posCache][5] = 1;
    }

    public void imprimirRegistroHilo(int h) {
        String rh = "";
        for (int i = 0; i < 32; i++) {
            rh = rh + contexto[h][i] + "   ";
        }
        System.out.println(rh);
    }

    private boolean revisarQuantumHilillo(Hilillo h) {
        return h.ciclosRelojHilillo > quantum;
    }
}