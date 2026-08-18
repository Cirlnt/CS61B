package gitlet;

import java.io.File;
import java.util.TreeMap;

import static gitlet.Utils.sha1;
import static gitlet.Utils.writeObject;

public class tree<K,V> extends TreeMap<K,V> {
    //tree文件里面放的是type[]，为对应HashMap的序列化，类里面还是HashMap


    private TreeMap<K, V> map;

    public tree() {
        this.map = new TreeMap<>();
    }

    public tree(TreeMap<K, V> map) {
        this.map = map;
    }





}
