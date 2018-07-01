/** Clase que simula las caches de datos de los nucleos. */
class CacheD {
    public int[][] valores;
    public boolean[] reservado;
    public MyReentrantLock[] locks;

    /** Constructor de la clase. */
    CacheD() {
        valores = new int[4][6];
        locks = new MyReentrantLock[4];
        reservado = new boolean[4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                valores[i][j] = 0;
            }
            locks[i] = new MyReentrantLock();
            reservado[i] = false;
        }
    }

    /** Metodo que imprime los valores que hay en la cache. */
    public void imprimirCache() {
        StringBuilder cd = new StringBuilder();
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 6; j++) {
                cd.append(valores[i][j]).append("   ");
            }
            cd.append("\n");
        }
        System.out.println(cd);
    }
}
