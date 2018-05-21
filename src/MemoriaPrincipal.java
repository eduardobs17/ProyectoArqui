public class MemoriaPrincipal {

    private Bloque memDatos[] = new Bloque[24];
    private Bloque memInstrucciones[] = new Bloque[40];
    int bus[] = new int[2]; //Posición 0 es el bus para datos y posición 2  bus para instrucciones
                            //Esto para manejar el acceso a memoria compartida

    /**
     * Constructor
     */
    public MemoriaPrincipal () {
        int x = 0;
        for (int i = 0; i < 24; i++) {
            memDatos[i] = new Bloque();
            for (int j = 0; j < 4; j++) {
                memDatos[i].palabra[j] = 1;
                memDatos[i].direccion[j] += x;
                x = x+4;
            }
        }

        for (int i = 0; i < 40; i++) {
            memInstrucciones[i] = new Bloque();
            for (int j = 0; j < 4; j++) {
                memInstrucciones[i].palabra[j] = 1;
                memInstrucciones[i].direccion[j] += x;
                x = x+1;
            }
        }
    }
}
