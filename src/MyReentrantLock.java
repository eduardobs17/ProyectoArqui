import java.util.concurrent.locks.ReentrantLock;

public class MyReentrantLock extends ReentrantLock {
    long owner() {
        Thread t =  this.getOwner();
        if (t != null) {
            return t.getId();
        } else {
            return -1;
        }
    }
}
