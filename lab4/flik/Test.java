package flik;

public class Test {
    public static void main(String[] args) {
        if (Flik.isSameNumber(127, 127)) {
            System.out.println("127 is same");
        }
        if (!Flik.isSameNumber(127, 127)) {
            System.out.println("128 is not same");
        }

        if (Flik.isSameNumber(128, 128)) {
            System.out.println("128 is same");
        }
        if (!Flik.isSameNumber(128, 128)) {
            System.out.println("128 is not same");
        }
    }
}
