/** Clase que simula las caches de instrucciones de los nucleos. */
class CacheI {
    public int[][] valores;
    public boolean[] reservado;
    public MyReentrantLock[] locks;

    /** Constructor de la clase. */
    CacheI() {
        valores = new int[4][18];
        locks = new MyReentrantLock[4];
        reservado = new boolean[4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 18; j++) {
                valores[i][j] = 0;
            }
            locks[i] = new MyReentrantLock();
            reservado[i] = false;
        }
    }
}
