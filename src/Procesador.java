public class Procesador {
    //Nucleo 0
    Hilillo hilo0_1 = new Hilillo();
    Hilillo hilo0_2 = new Hilillo();
    int cacheD0[][] = new int[6][8];
    int cacheI0[][] = new int[16][8];


    //Nucleo 1
    Hilillo hilo1 = new Hilillo();
    int cacheD1[][] = new int[6][4];
    int cacheI1[][] = new int[16][4];


    int ciclos_Reloj = 0;
    int contexto[][] = new int[3][32];

    /**
     * Constructor
     * Se inicializa en ceros el contexto y las caches.
     */
    public Procesador () {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 32; j++) {
                contexto[i][j] = 0;
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                cacheD0[i][j] = 0;
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                cacheI0[i][j] = 0;
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                cacheD1[i][j] = 0;
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 4; j++) {
                cacheI1[i][j] = 0;
            }
        }
    }

    /**
     * Este método manejará las instrucciones y su codificación
     * @param instruccion
     */
    public void ALU (int instruccion[], Hilillo h) {
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
                if (instruccion[1] == 0) {
                    h.pc = h.pc + (4 * instruccion[3]);
                }
                break;
            case 5: //BNEZ
                if (instruccion[1] != 0) {
                    h.pc = h.pc + (4 * instruccion[3]);
                }
                break;
            case 8: //DADDI
                h.registro[instruccion[1]] = h.registro[instruccion[2]] + instruccion[3];
                break;
            case 12: //DMUL
                h.registro[instruccion[1]] = h.registro[instruccion[2]] * h.registro[instruccion[3]];
                break;
            case 14: //DDIV
                h.registro[instruccion[1]] = h.registro[instruccion[2]] / h.registro[instruccion[3]];
                break;
            case 32: //DADD
                h.registro[instruccion[1]] = h.registro[instruccion[2]] + h.registro[instruccion[3]];
                break;
            case 34: //DSUB
                h.registro[instruccion[1]] = h.registro[instruccion[2]] - h.registro[instruccion[3]];
                break;
            case 35: //LW
                load(instruccion, h);
                break;
            case 43: //SW
                store(instruccion, h);
                break;
            case 63: //FIN

                break;
        }
    }

    /**
     * Metodo para guardar de memoria a registro
     * @param instruccion
     * @param h
     */
    public void load (int instruccion[], Hilillo h) {
        
    }

    /**
     * Metodo para guardar de registro a memoria
     * @param instruccion
     * @param h
     */
    public void store (int instruccion[], Hilillo h) {

    }
}
