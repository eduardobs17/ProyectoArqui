import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CacheI {
    public int[][] valores;
    public Lock[] locks;
    public boolean[] reservado;

    /**
     * Constructor de cache de instrucciones.
     * @param nucleo Es el nucleo al que pertenece la cache de instrucciones.
     */
    CacheI(int nucleo) {
        valores = new int[4][18];
        locks = new ReentrantLock[4];
        reservado = new boolean[4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 18; j++) {
                valores[i][j] = 0;
            }
            locks[i] = new ReentrantLock();
            reservado[i] = false;
        }
    }
}
