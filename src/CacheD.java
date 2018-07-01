class CacheD {
    public int[][] valores;
    public boolean[] reservado;
    public MyReentrantLock[] locks;

    /**
     * Constructor de cache de datos.
     */
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

    public void imprimirCache() {
        String cd = "";
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 6; j++) {
                cd = cd + valores[i][j] + "   ";
            }
            cd = cd + "\n";
        }
        System.out.println(cd);
    }
}
