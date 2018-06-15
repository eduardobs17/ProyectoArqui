public class MemoriaPrincipal {

    public BloqueD memDatos[] = new BloqueD[24];
    public BloqueI memInstrucciones[] = new BloqueI[40];

    private int iteradorBloqueInstrucciones = 0;
    private int iteradorPalabraInstrucciones = 0;

    /**
     * Constructor
     * Se inicializa en unos la memoria de datos e instrucciones
     */
    public MemoriaPrincipal () {
        int x = 0;
        for (int i = 0; i < 24; i++) {
            memDatos[i] = new BloqueD();
            for (int j = 0; j < 4; j++) {
                memDatos[i].palabra[j] = 1;
                memDatos[i].direccion[j] += x;
                x = x+4;
            }
        }

        for (int i = 0; i < 40; i++) {
            memInstrucciones[i] = new BloqueI();
            for (int j = 0; j < 16; j++) {
                memInstrucciones[i].palabra[j] = 1;
                memInstrucciones[i].direccion[j] += x;
                x++;
            }
        }
    }

    /**
     * Metodo para agregar las instrucciones en la memoria principal de instrucciones.
     * @param instrucciones
     */
    public int agregarInst(String instrucciones) {
        int valorRetorno = memInstrucciones[iteradorBloqueInstrucciones].direccion[0];
        String[] inst = instrucciones.split("\n");
        for (int x = 0; x < inst.length; x++) {
            String[] comandos = inst[x].split(" ");
            for (int y = 0; y < comandos.length; y++) {
                memInstrucciones[iteradorBloqueInstrucciones].palabra[iteradorPalabraInstrucciones] = Integer.parseInt(comandos[y]);
                iteradorPalabraInstrucciones++;
                if (iteradorPalabraInstrucciones == 16) {
                    iteradorPalabraInstrucciones = 0;
                    iteradorBloqueInstrucciones++;
                }
            }
        }
        return valorRetorno;
    }

    //Se escribe en memoria
    public void guardarBloqueInst(int[] bloque, int pos) {
        for (int mf = 0; mf < 16; mf++) {
            memInstrucciones[pos].palabra[mf] = bloque[mf];
        }
    }
}
