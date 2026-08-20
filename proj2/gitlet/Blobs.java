package gitlet;

    //id就是文件的sha1，注意blob 是原始字节，不是序列化对象
public class Blobs {
//    private String id;
    private byte[] blob;

    public Blobs(byte[] blob) {
//        this.id = id;
        this.blob = blob;
    }
}
