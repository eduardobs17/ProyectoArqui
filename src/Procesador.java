public class Procesador {
    //Nucleo 0
    Hilillo hilo0_1 = new Hilillo();
    Hilillo hilo0_2 = new Hilillo();
    int cacheD0[][] = new int[6][8];
    int cacheI0[][] = new int[6][8];


    //Nucleo 1
    Hilillo hilo1 = new Hilillo();
    int cacheD1[][] = new int[6][4];
    int cacheI1[][] = new int[6][4];


    int ciclos_Reloj = 0;
    int contexto[][] = new int[3][32];

    public Procesador () { //Constructor
        //Se inicializa en cero el contexto
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 32; j++) {
                contexto[i][j] = 0;
            }
        }
        //Se inicializa en cero las caches
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                cacheD0[i][j] = 0;
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                cacheI0[i][j] = 0;
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                cacheD1[i][j] = 0;
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                cacheI1[i][j] = 0;
            }
        }
    }

    public void ALU (int x) { //Este método manejará las instrucciones y su codificación
        switch (x) {
            case 8:
                //es DADDI
                break;

        }
    }
}
