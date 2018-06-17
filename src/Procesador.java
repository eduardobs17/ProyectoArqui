import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class Procesador {
    private static Procesador procesador;

    private int ciclosReloj = 0;
    private final int cantHilos, quantum;
    private MemoriaPrincipal memoria;

    private int contexto[][];
    public CacheD[] cacheDatos = new CacheD[2];
    public CacheI[] cacheInstrucciones = new CacheI[2];
    private ReentrantLock lockI = new ReentrantLock();
    private ReentrantLock lockD = new ReentrantLock();

    private Thread hilo0_1 = null;
    private Thread hilo0_2 = null;
    private Thread hilo1 = null;

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
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

    public void run(Queue<String> colaHilos, Queue<Integer> colaPCs) {
        while (!colaHilos.isEmpty()) {
            if (!colaHilos.isEmpty()) {
                Hilillo hilillo01 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 0);
                hilo0_1 =  new Thread(hilillo01);
                hilo0_1.start();
            }

            if (!colaHilos.isEmpty()) {
                Hilillo hilillo1 = new Hilillo(colaHilos.poll(), colaPCs.poll(), 1);
                hilo1 =  new Thread(hilillo1);
                hilo1.start();
            }
        }
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
                if (instruccion[2] != 0) {
                    if (h.registro[instruccion[2]] == 0) { //Divisor es cero
                        h.registro[instruccion[3]] = 0;
                    }
                    else {
                        h.registro[instruccion[3]] = h.registro[instruccion[1]] / h.registro[instruccion[2]];
                    }

                }
                break;
            case 32: //DADD
                h.registro[instruccion[3]] = h.registro[instruccion[1]] + h.registro[instruccion[2]];
                break;
            case 34: //DSUB
                h.registro[instruccion[3]] = h.registro[instruccion[1]] - h.registro[instruccion[2]];
                break;
            case 35: //LW
                loadD(instruccion, h);
                break;
            case 43: //SW
                storeD(instruccion, h);
                break;
            case 63: //FIN
                return 0;
        }
        ciclosReloj++;
        return 1;
    }

    /**
     * Metodo para guardar de memoria a registro.
     * 35 Y X n = RX   <--   M[n + (RY)]
     * @param instruccion
     * @param h
     */
    public void loadD (int instruccion[], Hilillo h) {
        int bloque = h.pc / 16;
        int palabra = (h.pc % 16) / 4;
        int posCache = h.calcularPosCache(bloque, h.nucleo);

        if (!cacheDatos[h.nucleo].reservado[posCache]) { //Se reserva si no esta reservado
            cacheDatos[h.nucleo].reservado[posCache] = true;
            if (cacheDatos[h.nucleo].valores[posCache][4] == bloque) { //Revisa si esta en esa cache
                if (cacheDatos[h.nucleo].valores[posCache][5] == 2) { //Revisa si esta modificado
                    if (!lockD.isLocked()) {
                        try {
                            lockD.tryLock();
                            cacheDatos[h.nucleo].locks[posCache].tryLock();
                            for (int i = 0; i < 4; i++) { //Se escribe en memoria
                                memoria.memDatos[bloque].palabra[i] = cacheDatos[h.nucleo].valores[bloque][i];
                            }
                            cacheDatos[h.nucleo].valores[bloque][5] = 1; //Compartido
                            h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                            for  (int i = 0; i < 40; i++) {
                                //AUMENTAN CICLOS DE RELOJ
                            }
                        }
                        finally {
                            lockD.unlock();
                            cacheDatos[h.nucleo].locks[posCache].unlock();
                        }
                    }
                }
                else { //Si esta invalido o compartido
                    if (h.nucleo == 0) { //PARA SABER CUAL NUCLEO ES
                        posCache = h.calcularPosCache(bloque, 1);
                        if (cacheDatos[1].valores[posCache][4] == bloque) { //Revisa si esta en la otra cache
                            cacheDatos[1].locks[posCache].tryLock();
                            if (cacheDatos[1].valores[posCache][5] == 2) { //Revisa si esta modificado
                                if (!lockD.isLocked()) { //Bus disponible
                                    try {
                                        lockD.tryLock();
                                        for (int i = 0; i < 4; i++) { //Se escribe en memoria
                                            memoria.memDatos[bloque].palabra[i] = cacheDatos[1].valores[bloque][i];
                                        }
                                        cacheDatos[1].valores[bloque][5] = 1; //Compartido
                                        h.registro[instruccion[2]] = cacheDatos[1].valores[bloque][palabra]; //Se escribe en el registro
                                        for  (int i = 0; i < 40; i++) {
                                            //AUMENTAN CICLOS DE RELOJ
                                        }
                                    }
                                    finally {
                                        lockD.unlock();
                                        cacheDatos[1].locks[posCache].unlock();
                                    }
                                }
                                else { cacheDatos[1].locks[posCache].unlock(); } //Bus no disponible
                            }
                        }
                        else { //Si no esta en la otra cache
                            for (int i = 0; i < 4; i++) { //Se carga desde memoria en la cache
                                cacheDatos[1].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                            }
                            cacheDatos[1].valores[bloque][5] = 1; //Compartido
                            h.registro[instruccion[2]] = cacheDatos[1].valores[bloque][palabra]; //Se escribe en el registro
                            for  (int i = 0; i < 40; i++) {
                                //AUMENTAN CICLOS DE RELOJ
                            }
                        }
                    }
                    else { //PARA SABER CUAL NUCLEO ES
                        posCache = h.calcularPosCache(bloque, 0);
                        if (cacheDatos[0].valores[posCache][4] == bloque) { //Revisa si esta en la otra cache
                            cacheDatos[0].locks[posCache].tryLock();
                            if (cacheDatos[0].valores[posCache][5] == 2) { //Revisa si esta modificado
                                if (!lockD.isLocked()) {
                                    try {
                                        lockD.tryLock();
                                        for (int i = 0; i < 4; i++) { //Se escribe en memoria
                                            memoria.memDatos[bloque].palabra[i] = cacheDatos[0].valores[bloque][i];
                                        }
                                        cacheDatos[0].valores[bloque][5] = 1; //Compartido
                                        h.registro[instruccion[2]] = cacheDatos[0].valores[bloque][palabra]; //Se escribe en el registro
                                        for  (int i = 0; i < 40; i++) {
                                            //AUMENTAN CICLOS DE RELOJ
                                        }
                                    }
                                    finally {
                                        lockD.unlock();
                                        cacheDatos[0].locks[posCache].unlock();
                                    }
                                }
                                else { cacheDatos[0].locks[posCache].unlock(); } //Bus no disponible
                            }
                        }
                        else { //Si no esta en la otra cache
                            for (int i = 0; i < 4; i++) { //Se carga desde memoria en la cache
                                cacheDatos[0].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                            }
                            cacheDatos[0].valores[bloque][5] = 1; //Compartido
                            h.registro[instruccion[2]] = cacheDatos[0].valores[bloque][palabra]; //Se escribe en el registro
                            for  (int i = 0; i < 40; i++) {
                                //AUMENTAN CICLOS DE RELOJ
                            }
                        }
                    }
                }
            }
            else { //Si no esta en esa cache
                if (cacheDatos[h.nucleo].valores[posCache][5] == 2) { //Revisa estado de bloque victima, si es M
                    for (int i = 0; i < 4; i++) { //Se escribe en memoria
                        memoria.memDatos[bloque].palabra[i] = cacheDatos[h.nucleo].valores[bloque][i];
                    }
                    for  (int i = 0; i < 40; i++) {
                        //AUMENTAN CICLOS DE RELOJ
                    }
                }
                if (h.nucleo == 0) { //Revisa si esta en la otra cache
                    posCache = h.calcularPosCache(bloque, 1);
                    if (cacheDatos[1].valores[posCache][4] == bloque) {
                        cacheDatos[1].locks[posCache].tryLock();
                        if (cacheDatos[1].valores[posCache][5] == 2) { //Revisa si esta modificado
                            if (!lockD.isLocked()) {
                                try {
                                    lockD.tryLock();
                                    for (int i = 0; i < 4; i++) { //Se escribe en memoria
                                        memoria.memDatos[bloque].palabra[i] = cacheDatos[1].valores[bloque][i];
                                    }
                                    cacheDatos[1].valores[bloque][5] = 1; //Compartido
                                    for (int i = 0; i < 4; i++) { //Y se guarda en la cache original
                                        cacheDatos[h.nucleo].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                                    }
                                    h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                                    for  (int i = 0; i < 40; i++) {
                                        //AUMENTAN CICLOS DE RELOJ
                                    }
                                }
                                finally {
                                    lockD.unlock();
                                    cacheDatos[1].locks[posCache].unlock();
                                }
                            }
                            else { cacheDatos[1].locks[posCache].unlock(); }
                        }
                        else { //Si es invalido o compartido
                            for (int i = 0; i < 4; i++) { //Cargar desde memoria a la cache original
                                cacheDatos[h.nucleo].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                            }
                            h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                            for  (int i = 0; i < 40; i++) {
                                //AUMENTAN CICLOS DE RELOJ
                            }
                        }
                    }
                    else { //Si no esta en la otra cache
                        for (int i = 0; i < 4; i++) { //Cargar desde memoria a la cache original
                            cacheDatos[h.nucleo].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                        }
                        h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                        for  (int i = 0; i < 40; i++) {
                            //AUMENTAN CICLOS DE RELOJ
                        }
                    }
                }
                else {
                    posCache = h.calcularPosCache(bloque, 0); //Revisa si esta en la otra cache
                    if (cacheDatos[0].valores[posCache][4] == bloque) {
                        posCache = h.calcularPosCache(bloque, 0);
                        if (cacheDatos[0].valores[posCache][4] == bloque) {
                            cacheDatos[0].locks[posCache].tryLock();
                            if (cacheDatos[0].valores[posCache][5] == 2) { //Revisa si esta modificado
                                if (!lockD.isLocked()) {
                                    try {
                                        lockD.tryLock();
                                        for (int i = 0; i < 4; i++) { //Se escribe en memoria
                                            memoria.memDatos[bloque].palabra[i] = cacheDatos[0].valores[bloque][i];
                                        }
                                        cacheDatos[0].valores[bloque][5] = 1; //Compartido
                                        for (int i = 0; i < 4; i++) { //Y se guarda en la cache original
                                            cacheDatos[h.nucleo].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                                        }
                                        h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                                        for  (int i = 0; i < 40; i++) {
                                            //AUMENTAN CICLOS DE RELOJ
                                        }
                                    }
                                    finally {
                                        lockD.unlock();
                                        cacheDatos[0].locks[posCache].unlock();
                                    }
                                }
                                else { cacheDatos[0].locks[posCache].unlock(); }
                            }
                            else { //Si es invalido o compartido
                                for (int i = 0; i < 4; i++) { //Cargar desde memoria a la cache original
                                    cacheDatos[h.nucleo].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                                }
                                h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                                for  (int i = 0; i < 40; i++) {
                                    //AUMENTAN CICLOS DE RELOJ
                                }
                            }
                        }
                        else { //Si no esta en la otra cache
                            for (int i = 0; i < 4; i++) { //Cargar desde memoria a la cache original
                                cacheDatos[h.nucleo].valores[bloque][i] = memoria.memDatos[bloque].palabra[i];
                            }
                            h.registro[instruccion[2]] = cacheDatos[h.nucleo].valores[bloque][palabra]; //Se escribe en el registro
                            for  (int i = 0; i < 40; i++) {
                                //AUMENTAN CICLOS DE RELOJ
                            }
                        }
                    }
                }
            }
        }
        //CAMBIO DE CICLO DE RELOJ
    }

    /**
     * Metodo para guardar de registro a memoria
     * 43 Y X n = M[n + (RY)]   <--   RX
     * @param instruccion
     * @param h
     */
    public void storeD (int instruccion[], Hilillo h) {

    }

    /**
     * Metodo para guardar de memoria a cache
     */
    public void loadI (int nucleo, int posCache, int posMem) {
        int bloqueMem = posMem / 16;
        int bloqueEnMemoria = bloqueMem - 24;

        if (!cacheInstrucciones[nucleo].reservado[posCache]) { //Se revisa si la posicion de cache ya está reservada
            cacheInstrucciones[nucleo].reservado[posCache] = true;

            if (cacheInstrucciones[nucleo].valores[posCache][17] == 2) { //Se revisa bloque victima
                if (!lockI.isLocked()) {
                    lockI.tryLock();
                    cacheInstrucciones[nucleo].locks[posCache].tryLock();

                    try { //Se guarda el bloque en memoria
                        for (int mf = 0; mf < 16; mf++) {
                            memoria.memInstrucciones[bloqueEnMemoria].palabra[mf] = cacheInstrucciones[nucleo].valores[posCache][mf];
                        }
                        cacheInstrucciones[nucleo].valores[posCache][17] = 1;
                        for (int mf = 0; mf < 40; mf++) {
                            //h.ciclosReloj++;

                            //Se aumentan los 40 ciclos de reloj
                        }
                    } finally {
                        lockI.unlock();
                        cacheInstrucciones[nucleo].locks[posCache].unlock();
                    }
                }
            }

            if (!lockI.isLocked()) {
                lockI.lock();
                cacheInstrucciones[nucleo].locks[posCache].lock();

                try {
                    for (int mf = 0; mf < 16; mf++) {
                        cacheInstrucciones[nucleo].valores[posCache][mf] = memoria.memInstrucciones[bloqueEnMemoria].palabra[mf];
                    }
                    cacheInstrucciones[nucleo].valores[posCache][16] = bloqueMem;
                    for (int mf = 0; mf < 40; mf++) {
                        //h.ciclosReloj++;
                        //Se aumentan los 40 ciclos de reloj
                    }
                    cacheInstrucciones[nucleo].valores[posCache][17] = 1;
                } finally {
                    lockI.unlock();
                    cacheInstrucciones[nucleo].locks[posCache].unlock();
                }
            }
        }
    }

    public void llenarContextopc(int fila, int pc) {
        contexto[fila][32] = pc;
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