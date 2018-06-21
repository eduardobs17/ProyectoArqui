public class MemoriaPrincipal {

    public BloqueD memDatos[] = new BloqueD[24];
    public BloqueI memInstrucciones[] = new BloqueI[40];

    private int iteradorBloqueInstrucciones = 0;
    private int iteradorPalabraInstrucciones = 0;

    private static MemoriaPrincipal memoria;

    /**
     * Constructor de Memoria principal.
     * Se inicializa en unos la memoria de datos e instrucciones
     */
    private MemoriaPrincipal () {
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
     * Metodo Singleton para controlar que solo se cree un objeto memoriaPrincipal.
     * Controla que solo se maneje una instancia de memoria en el programa.
     * @return Devuelve la memoria si esta ya existe.
     */
    public static MemoriaPrincipal getInstancia() {
        if (memoria == null) {
            memoria = new MemoriaPrincipal();
        }
        return memoria;
    }

    /**
     *
     * @return
     */
    @Override
    public MemoriaPrincipal clone() {
        try {
            throw new CloneNotSupportedException();
        } catch (CloneNotSupportedException ignored) { }
        return null;
    }

    /**
     * Una vez leido el archivo  y colocado las instrucciones en una string, este metodo es llamado
     * para agregar las instrucciones en la memoria principal de instrucciones.
     * @param instrucciones String con las instrucciones de todos los hilillos.
     */
    public int agregarInst(String instrucciones) {
        int valorRetorno = memInstrucciones[iteradorBloqueInstrucciones].direccion[0];
        String[] inst = instrucciones.split("\n"); // Llena el vector inst con la cantidad de instrucciones
        for (int x = 0; x < inst.length; x++) {
            String[] comandos = inst[x].split(" "); // Llena el vector comandos con la cantidad de comandos que tiene la instruccion.
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
}
