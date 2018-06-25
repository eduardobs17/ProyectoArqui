import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CacheD {
    public int[][] valores;
    public boolean[] reservado;
    public Lock[] locks;

    /**
     * Constructor de cache de datos.
     */
    CacheD() {
        valores = new int[4][6];
        locks = new ReentrantLock[4];
        reservado = new boolean[4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                valores[i][j] = 0;
            }
            locks[i] = new ReentrantLock();
            reservado[i] = false;
        }
    }
}
