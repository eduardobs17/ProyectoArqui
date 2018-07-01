/** Clase que simula los bloques de la memoria de instrucciones. */
public class BloqueI {
    public int palabra[];
    public int direccion[];

    /** Constructor de la clase. */
    BloqueI() {
        palabra = new int[16];
        direccion = new int[16];
    }
}