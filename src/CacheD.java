import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CacheD {
    public int[][] valores;
    public boolean[] reservado;
    public Lock[] locks;

    /**
     * Constructor de cache de datos.
     * @param nucleo
     */
    CacheD(int nucleo) {
        if (nucleo == 0) {
            valores = new int[8][6];
            locks = new ReentrantLock[8];
            reservado = new boolean[8];

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 6; j++) {
                    valores[i][j] = 0;
                }
                locks[i] = new ReentrantLock();
                reservado[i] = false;
            }
        } else { //Si el nucleo es 1.
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
}
