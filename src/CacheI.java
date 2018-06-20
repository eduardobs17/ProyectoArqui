import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CacheI {
    public int[][] valores;
    public Lock[] locks;
    public boolean[] reservado;

    /**
     * Constructor de cache de instrucciones.
     * @param nucleo
     */
    CacheI(int nucleo) {
        if (nucleo == 0) {
            valores = new int[8][18];
            locks = new ReentrantLock[8];
            reservado = new boolean[8];

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 18; j++) {
                    valores[i][j] = 0;
                }
                locks[i] = new ReentrantLock();
                reservado[i] = false;
            }
        } else { //Si el nucle es 1.
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
}
