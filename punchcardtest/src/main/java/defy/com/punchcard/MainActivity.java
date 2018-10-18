package defy.com.punchcard;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import defy.com.punchcard.listener.IToastStr;
import defy.com.punchcard.listener.ListenerUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "66666";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
//            PunchCardFragment fragment = new PunchCardFragment();

//            SeekBarFragment fragment = new SeekBarFragment();
//            LaunchModeFragment fragment = new LaunchModeFragment();
//            ViewFragment fragment = new ViewFragment();
//            Dagger2Fragment fragment = new Dagger2Fragment();
//            KotlinFragment fragment = new KotlinFragment();

            PunchCardKotlin fragment = new PunchCardKotlin();

//            LearnFragment fragment = new LearnFragment();
            getFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();
        }

//        Node<Integer> test = init(10);
//        printNode(test);
//        Log.e("66666", "---------------" + "\n");
//        printNode(revList(test));

//        testContentProvider();// 内容提供者
        IToastStr iToastStr = ListenerUtils.getListener(IToastStr.class);
        String toastStr = iToastStr.getToastStr();
//        Toast.makeText(this, toastStr, Toast.LENGTH_SHORT).show();

//        execCommand("pm","uninstall", "com.bbtree.cardreader");
        uninstallDataAPPBySilent("com.bbtree.cardreader");
    }

    /**
     * 反转单链表
     *
     * @param head
     * @return
     */
    public Node<Integer> revList(Node<Integer> head) {

        if (head == null)
            return null;
        Node<Integer> nodeResult = null;

        Node<Integer> nodePre = null;
        Node<Integer> current = head;
        while (current != null) {
            Node<Integer> nodeNext = current.next;
            if (nodeNext == null) {
                nodeResult = current;
            }

            current.next = nodePre;
            nodePre = current;
            current = nodeNext;
        }

        return nodeResult;
    }

    /**
     * 打印日志
     *
     * @param head
     */
    public void printNode(Node head) {
        Node nodeTemp = head;
        while (nodeTemp != null) {
            System.out.print(nodeTemp.item);
            Log.e("66666", nodeTemp.item + "");
            nodeTemp = nodeTemp.next;
        }
    }

    /**
     * 初始化 单链表
     *
     * @param num
     * @return
     */
    public Node init(int num) {
        Node node = new Node(0, null);
        Node currentNode = null;
        Node tempNode = null;
        for (int i = 1; i < num; i++) {
            tempNode = new Node(i, null);
            if (i == 1) {
                node.next = tempNode;
            } else {
                currentNode.next = tempNode;
            }
            currentNode = tempNode;
        }
        return node;
    }

    private LinkedList list = new LinkedList();

    public void dequeue() {
        list.removeFirst();
        list.addLast(null);
        list.offer(null);
        list.poll();
    }

    private Queue queue = new ArrayDeque();
    private Queue queueList = new LinkedList();
    private Queue queuePriority = new PriorityQueue();

    public static final String AUTHORITY_URI = "com.bbtree.cardreader.provider.CardRecordContentProvider";

    /**
     * 测试内容提供者
     */
    private void testContentProvider() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(Uri.parse("content://" + AUTHORITY_URI + "/" + "CARD_RECORD"), null, null, null, null);
            Log.e("66666", "cursor size :" + cursor.getCount());
            while (cursor.moveToNext()) {
                String columnName = cursor.getColumnName(1);
                String card_serial_number = cursor.getString(cursor.getColumnIndex(columnName));
                Log.e("66666", "card_serial_number : " + card_serial_number + "");
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //TODO pm命令可以通过adb在shell中执行，同样，我们可以通过代码来执行

    /**
     * 静默卸载apk到Data/app目录
     *
     * @param packageName
     * @return 卸载成功为true
     */
    public static boolean uninstallDataAPPBySilent(String packageName) {
        Log.d(TAG, "-------uninstallDataAPPBySilent------");

        // 参数检测
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Log.d(TAG, "packageName: " + packageName);

        StringBuilder cmd = new StringBuilder();
        cmd.append("pm uninstall " + packageName).append("\n");
        // 部分手机Root之后Library path 丢失，导入library path可解决该问题
        // cmd.append("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
        if (execRootCmd(cmd.toString()) == 0) {
            Log.e(TAG, "uninstall: " + packageName + "success");
            return true;
        }
        Log.e(TAG, "uninstall: " + packageName + " failed");

        return false;
    }

    /**
     * root权限下执行命令
     *
     * @param cmd
     *            多条命令需用换行分隔
     * @return 执行结果码 0代表成功
     */
    private static int execRootCmd(String cmd) {
        Log.d(TAG, "execRootCmd: " + cmd);

        Process process = null;
        DataOutputStream dos = null;
        try {
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            process.waitFor();
            Log.e(TAG, "process.exitValue(): " + process.exitValue());

            return process.exitValue();
        } catch (Exception e) {
            Log.e(TAG, "exception: " + e.getMessage());
            return -1;
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
