import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;

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

    private final int cantHilos, quantum;
    private int ciclosReloj;

    private int contexto[][];
    private CacheD[] cacheDatos = new CacheD[2];
    public CacheI[] cacheInstrucciones = new CacheI[2];
    private ReentrantLock busI = new ReentrantLock();
    private ReentrantLock busD = new ReentrantLock();

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     * @param cant Cantidad de hilillos que se van a ejecutar.
     * @param tamQuantum Tamaño del quantum del hilillo.
     */
    private Procesador(int cant, int tamQuantum) {
        memoria = MemoriaPrincipal.getInstancia();
        cantHilos = cant;
        quantum = tamQuantum;
        ciclosReloj = 0;

        cacheDatos[0] = new CacheD();
        cacheDatos[1] = new CacheD();

        cacheInstrucciones[0] = new CacheI();
        cacheInstrucciones[1] = new CacheI();

        contexto = new int[cantHilos][33];
        for (int i = 0; i < cantHilos; i++) {
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
     * Corre la modalidad rapida, ejecuta el programa corrido.
     * @param colaHilos cola de los hilos que se van a ejecutar.
     * @param colaPCs cola con el PC de los hilos que se van a ejecutar.
     * @param barreraI barreraInicio para manejar ciclo de reloj.
     * @param barreraF barreraFinal para manejar ciclo de reloj.
     */
    public void runRapida(Queue<String> colaHilos, Queue<Integer> colaPCs, Phaser barreraI, Phaser barreraF) {
        int iden = 0;
        while (true) {

            if (!colaHilos.isEmpty() && hilo0 == null) {
                hilo0 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 0, barreraI, barreraF, iden);
                iden++;
                barreraI.register();
                hilo0.start();
            }
            if (!colaHilos.isEmpty() && hilo1 == null) {
                hilo1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 1, barreraI, barreraF, iden);
                iden++;
                barreraI.register();
                hilo1.start();
            }

            if (hilo0 != null && hilo0.getEstadoHilillo() == 0) {
                /*for (int i = 0; i < 32; i++) {
                    contexto[iden][i] =  hilo0.registro[i];
                }*/
                hilo0.idHilillo = hilo0.idHilillo + 1;

                hilo0 = null;
            }
            if (hilo1 != null && hilo1.getEstadoHilillo() == 0) {
                /*for (int i = 0; i < 32; i++) {
                    contexto[iden][i] =  hilo1.registro[i];
                }*/
                hilo1.idHilillo = hilo1.idHilillo + 1;

                hilo1 = null;
            }
            if (hilo0 == null && hilo1 == null) {
                if (colaHilos.isEmpty()) {
                    return;
                }
            }

            System.out.println("Ciclo de reloj: " + ciclosReloj);
            if(hilo0 != null) {
                System.out.println("Nucleo 0: Hilillo " + hilo0.idHilillo + ", Estado " + hilo0.getEstadoHilillo());
            }
            if(hilo1 != null) {
                System.out.println("Nucleo 1: Hilillo " + hilo1.idHilillo + ", Estado " + hilo1.getEstadoHilillo());
            }
            System.out.println("\n");

            barreraI.arriveAndAwaitAdvance();
            ciclosReloj++;

        }
    }

    /**
     * Corre la modalidad lenta, ejecuta el programa con delay en los ciclos de reloj.
     * @param colaHilos cola de los hilos que se van a ejecutar.
     * @param colaPCs cola con el PC de los hilos que se van a ejecutar.
     * @param barreraI barreraInicio para manejar ciclo de reloj.
     * @param barreraF barreraFinal para manejar ciclo de reloj.
     */
    public void runLenta(Queue<String> colaHilos, Queue<Integer> colaPCs, Phaser barreraI, Phaser barreraF) {
        int iden = 0;
        while (true) {

            if (!colaHilos.isEmpty() && hilo0 == null) {
                hilo0 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 0, barreraI, barreraF, iden);
                iden++;
                barreraI.register();
                hilo0.start();
            }
            if (!colaHilos.isEmpty() && hilo1 == null) {
                hilo1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 1, barreraI, barreraF, iden);
                iden++;
                barreraI.register();
                hilo1.start();
            }

            if (hilo0 != null && hilo0.getEstadoHilillo() == 0) {
                hilo0.idHilillo = hilo0.idHilillo + 1;
                hilo0 = null;
            }
            if (hilo1 != null && hilo1.getEstadoHilillo() == 0) {
                hilo1.idHilillo = hilo1.idHilillo + 1;
                hilo1 = null;
            }
            if (hilo0 == null && hilo1 == null) {
                if (colaHilos.isEmpty()) {
                    return;
                }
            }

            System.out.println("Ciclo de reloj: " + ciclosReloj);
            if(hilo0 != null) {
                System.out.println("Nucleo 0: Hilillo " + hilo0.idHilillo + ", Estado " + hilo0.getEstadoHilillo());
            }
            if(hilo1 != null) {
                System.out.println("Nucleo 1: Hilillo " + hilo1.idHilillo + ", Estado " + hilo1.getEstadoHilillo());
            }
            System.out.println("\n");

            barreraI.arriveAndAwaitAdvance();
            ciclosReloj++;

            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Metodo para llenar el contexto del Hilillo.
     */
    public void llenarContextoHilillo() {

    }

    /**
     * Metodo que inicia el programa y manda a ejecutar los hilillos.
     * @param colaHilos Cola que contiene string con las instrucciones de todos los hilillos.
     * @param colaPCs Cola que contiene el PC de todos los hilillos.
     * @param barreraF Barrera de inicio para que los hilillos inicien a la vez.
     */
    public void run(Queue<String> colaHilos, Queue<Integer> colaPCs, Phaser barreraI, Phaser barreraF) {
        /*while (true) {

                if (!colaHilos.isEmpty() && hilo0 == null) {
                    hilo0 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 0, barreraI, barreraF);
                    barreraI.register();
                    hilo0.start();
                }
                if (!colaHilos.isEmpty() && hilo1 == null) {
                    hilo1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 1, barreraI, barreraF);
                    barreraI.register();
                    hilo1.start();
                }

                if (hilo0 != null && hilo0.getEstadoHilillo() == 0) {
                    hilo0 = null;
                }
                if (hilo1 != null && hilo1.getEstadoHilillo() == 0) {
                    hilo1 = null;
                }
                if (hilo0 == null && hilo1 == null) {
                    if (colaHilos.isEmpty()) {
                        return;
                    }
                }

                //System.out.println("Ciclo de reloj: " + ciclosReloj);
                barreraI.arriveAndAwaitAdvance();
                ciclosReloj++;

        }*/
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
                int res = loadD(instruccion[2], h.registro[instruccion[1]] + instruccion[3], h.getNucleo(), h.registro, h.getBarreraI());
                if (res == -1) {
                    return -1;
                }
                break;
            case 43: //SW
                storeD(instruccion[2], h.registro[instruccion[1]] + instruccion[3], h.getNucleo(), h.registro, h.getBarreraI());
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
    private int loadD (int registro, int posMemoria, int nucleo, int[] registros, Phaser barreraHilillo) {
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
                                                barreraHilillo.arriveAndAwaitAdvance();
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
                                                barreraHilillo.arriveAndAwaitAdvance();
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
                                            barreraHilillo.arriveAndAwaitAdvance();
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
                            copiaCache.reservado[posCache] = false;
                        }
                    } else { //Bus no disponible
                        return -1;
                    }
                }
            } else {                                                                                    //Bloque no está en cache
                //Se revisa bloque victima
                if (copiaCache.valores[posCache][5] == 2) {
                    for (int i = 0; i < 40; i++) {
                        barreraHilillo.arriveAndAwaitAdvance();
                    }
                    guardarBloqueEnMemoriaD(copiaCache.valores[posCache]);
                }

                if (!busD.isLocked()) {                                                                 //Se revisa estado del bus
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
                                            barreraHilillo.arriveAndAwaitAdvance();
                                        }
                                        guardarBloqueEnMemoriaD(copiaOtraCache.valores[posCache]);
                                        guardarBloqueEnCacheDesdeCacheD(copiaOtraCache, posCache, copiaCache, posCache);
                                    } finally {
                                        copiaOtraCache.locks[posCache].unlock();
                                        copiaCache.locks[posCache].unlock();
                                    }
                                }
                            } else {
                                copiaCache.locks[posCache].tryLock();
                                try {
                                    for (int i = 0; i < 40; i++) {
                                        barreraHilillo.arriveAndAwaitAdvance();
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
                        copiaCache.reservado[posCache] = false;
                    }
                } else { //Bus no disponible
                    return -1;
                }
            }
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
    private void storeD (int registro, int posMemoria, int nucleo, int[] registros, Phaser barreraHilillo) {
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
                //Se revisa bloque victima
                if (copiaCache.valores[posCache][5] == 2) {
                    //Se debe reservar el bus
                    for (int i = 0; i < 40; i++) {
                        barreraHilillo.arriveAndAwaitAdvance();
                    }
                    guardarBloqueEnMemoriaD(copiaCache.valores[posCache]);
                }

                if (!busD.isLocked()) {
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
                                            barreraHilillo.arriveAndAwaitAdvance();
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
                                            barreraHilillo.arriveAndAwaitAdvance();
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
                                        barreraHilillo.arriveAndAwaitAdvance();
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
                        for (int mf = 0; mf < 40; mf++) {
                            h.getBarreraI().arriveAndAwaitAdvance();
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
                    for (int mf = 0; mf < 40; mf++) {
                        h.getBarreraI().arriveAndAwaitAdvance();
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
}