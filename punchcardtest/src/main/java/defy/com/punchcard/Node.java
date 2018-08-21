package defy.com.punchcard;

/**
 * Created by chenglei on 2017/9/26.
 */

public class Node<E> {
    E item;
    Node<E> next;

    public Node(E item, Node<E> next) {
        this.item = item;
        this.next = next;
    }
}
