import java.util.concurrent.locks.ReentrantLock;

/** Clase auxiliar del ReentrackLock. */
public class MyReentrantLock extends ReentrantLock {

    /** Metodo que devuelve al dueño de un lock. */
    long owner() {
        Thread t =  this.getOwner();
        if (t != null) {
            return t.getId();
        } else {
            return -1;
        }
    }
}
