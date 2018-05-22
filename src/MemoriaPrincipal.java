public class MemoriaPrincipal {

    private BloqueD memDatos[] = new BloqueD[24];
    private BloqueI memInstrucciones[] = new BloqueI[40];
    int bus[] = new int[2]; //Posición 0 es el bus para datos y posición 2  bus para instrucciones
                            //Esto para manejar el acceso a memoria compartida
    private int iteradorBloqueInstrucciones = 0;
    private int iteradorPalabraInstrucciones = 0;

    /**
     * Constructor
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

    public void agregarInst(String instrucciones) {
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
    }
}
