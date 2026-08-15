package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.writeObject;

public class tree<K,V> extends HashMap<K,V> {
    //tree文件里面放的是type[]，为对应HashMap的序列化，类里面还是HashMap


    private HashMap<K, V> map;

    public tree() {
        this.map = new HashMap<>();
    }

    // 提供方法往 Tree 里添加文件
    public void addFile(K filename, V blobHash) {
        map.put(filename, blobHash);
    }


}
