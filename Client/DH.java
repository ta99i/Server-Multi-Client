package Client;

import java.util.Random;

public class DH {
    int _generator;
    int _prime;
    int _privateKey;
    int _publicKey;
    int _sharedKey = 0;
    static int n = 16;

    DH() {
        Random rand = new Random();
        _generator = rand.nextInt(n) + 3;
        _prime = calcPrime();
        _privateKey = rand.nextInt(_generator - 1) + 1;
        _publicKey = publicKey();
    }

    DH(int generator, int prime, int opk) {
        Random rand = new Random();
        _generator = generator;
        _prime = prime;
        _privateKey = rand.nextInt(_generator - 1) + 1;
        _publicKey = publicKey();
        calcSharedKey(opk);
    }

    public int publicKey() {
        int publicKey;
        publicKey = powmod(_generator, _privateKey, _prime);
        // System.out.println(null);
        return publicKey;
    }

    private int calcPrime() {
        int num = 0;
        Random rand = new Random();
        num = rand.nextInt(n - 1) + 1;
        while (!isPrime(num))
            num = rand.nextInt(n - 1) + 1;
        return num;
    }

    private boolean isPrime(int num) {
        if (num <= 3 || num % 2 == 0)
            return false;
        int divisor = 3;
        while ((divisor <= Math.sqrt(num)) && (num % divisor != 0))
            divisor += 2;
        return num % divisor != 0;
    }

    public int powmod(int a, int b, int m) {
        int res = a;
        for (int i = 0; i < b - 1; i++)
            res = (res * a) % m;
        return res;
    }

    public void calcSharedKey(int otherPublicKey) {
        int sk = powmod(otherPublicKey, _privateKey, _prime);
        _sharedKey = sk;
    }
}
