package gitlet;

    //id就是文件的sha1
public class Blobs {
//    private String id;
    private byte[] blob;

    public Blobs(byte[] blob) {
//        this.id = id;
        this.blob = blob;
    }
}
