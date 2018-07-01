/** Clase que simula la memoria del programa. */
public class MemoriaPrincipal {

    public BloqueD memDatos[] = new BloqueD[24];
    public BloqueI memInstrucciones[] = new BloqueI[40];

    private int iteradorBloqueInstrucciones = 0;
    private int iteradorPalabraInstrucciones = 0;

    private static MemoriaPrincipal memoria;

    /** Constructor de la clase. Las memorias se inicializan en unos. */
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

    @Override
    public MemoriaPrincipal clone() {
        try {
            throw new CloneNotSupportedException();
        } catch (CloneNotSupportedException ignored) { }
        return null;
    }

    /**
     * Metodo que agrega las instrucciones a memoria.
     * @param instrucciones String con las instrucciones de todos los hilillos.
     */
    public int agregarInst(String instrucciones) {
        int valorRetorno = memInstrucciones[iteradorBloqueInstrucciones].direccion[iteradorPalabraInstrucciones];
        String[] inst = instrucciones.split("\n"); // Llena el vector inst con la cantidad de instrucciones
        for (String anInst : inst) {
            String[] comandos = anInst.split(" "); // Llena el vector comandos con la cantidad de comandos que tiene la instruccion.
            for (String comando : comandos) {
                memInstrucciones[iteradorBloqueInstrucciones].palabra[iteradorPalabraInstrucciones] = Integer.parseInt(comando);
                iteradorPalabraInstrucciones++;
                if (iteradorPalabraInstrucciones == 16) {
                    iteradorPalabraInstrucciones = 0;
                    iteradorBloqueInstrucciones++;
                }
            }
        }
        return valorRetorno;
    }

    /** Metodo que imprime los valores de la memoria. */
    public void imprimirMemoria() {
        String md = "";
        for (int i = 0; i < 24; i++) {
            for(int j = 0; j < 4; j++) {
                md = md + memoria.memDatos[i].palabra[j] + "   ";
            }
            md = md + "\n";
        }
        System.out.println(md);
    }
}
