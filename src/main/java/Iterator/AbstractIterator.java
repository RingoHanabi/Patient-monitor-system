package Iterator;

public interface AbstractIterator {
    boolean hasNext();

    Object getNext();

    void reset();
}
