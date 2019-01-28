/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.zip.CRC32;

/**
 */
abstract class Idea13KeyGen {
    private static BigInteger MOD = new BigInteger("86d1ee98953ca32058c98ef923bd23ea316e2a45e48de0913154253eac682e05ffdc75106fcf8c6eecc701dbe0309abd83bcd837be11af584457de0217d056a1e0580e9003d3bd5ee0300f0650fc4b6bb9be44b3091c70fdbb047e7889c873c9c6c30a2ab2e17727e58e92303456b0e7cff0909138a025c9fc3977d80c80d96d", 16);
    private static BigInteger PRIVATE_EXP = new BigInteger("1ea4b8c695a522aec77f88afb81fd579f5a244997e4981317f34d1025dcd90286cef7eec2d7dcf3da733d488557cb6af3cb23fb2ebd0b93a26ce91f787d222de737b289945f808bfd45910d9a7d5ad204fd1087dcfbdaf7c25009065689912ba3a34fad3e000c65eb0c8c8265455c2702521e1a3c03b5f067ce34949c6f64f41", 16);

    /**
     * @param s
     * @param i
     * @param bytes
     * @return
     */
    public static short getCRC32(String s, int i, byte bytes[]) {
        CRC32 crc32 = new CRC32();
        if (s != null) {
            for (int j = 0; j < s.length(); j++) {
                char c = s.charAt(j);
                crc32.update(c);
            }
        }
        crc32.update(i);
        crc32.update(i >> 8);
        crc32.update(i >> 16);
        crc32.update(i >> 24);
        for (int k = 0; k < bytes.length - 2; k++) {
            byte byte0 = bytes[k];
            crc32.update(byte0);
        }
        return (short) (int) crc32.getValue();
    }

    /**
     * @param biginteger
     * @return String
     */
    public static String encodeGroups(BigInteger biginteger) {
        BigInteger beginner1 = BigInteger.valueOf(0x39aa400L);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; biginteger.compareTo(BigInteger.ZERO) != 0; i++) {
            int j = biginteger.mod(beginner1).intValue();
            String s1 = encodeGroup(j);
            if (i > 0) {
                sb.append("-");
            }
            sb.append(s1);
            biginteger = biginteger.divide(beginner1);
        }
        return sb.toString();
    }

    /**
     * @param i
     * @return
     */
    public static String encodeGroup(int i) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 5; j++) {
            int k = i % 36;
            char c;
            if (k < 10) {
                c = (char) (48 + k);
            } else {
                c = (char) ((65 + k) - 10);
            }
            sb.append(c);
            i /= 36;
        }
        return sb.toString();
    }

    /**
     * @param name
     * @param days
     * @param id
     * @return
     */
    public static String generatorKey(String name, int days, int id) {
        id %= 100000;
        byte bkey[] = new byte[12];
        bkey[0] = (byte) 1; // Product type: IntelliJ IDEA is 1
        bkey[1] = 13; // version

        Date d = new Date();

        // 时间
        long ld = (d.getTime() >> 16);  // 取出高 48 bit
        // 取出中间32位
        bkey[2] = (byte) (ld & 255);    // 8 bit
        bkey[3] = (byte) ((ld >> 8) & 0xFF); // 8bit
        bkey[4] = (byte) ((ld >> 16) & 0xFF);    // 8bit
        bkey[5] = (byte) ((ld >> 24) & 0xFF);    // 8 bit

        days &= 0xFFFF; // 65535

        bkey[6] = (byte) (days & 0xFF);
        bkey[7] = (byte) ((days >> 8) & 0xFF);

        bkey[8] = 105;
        bkey[9] = -59;
        bkey[10] = 0;
        bkey[11] = 0;
        int w = getCRC32(name, id % 100000, bkey);
        bkey[10] = (byte) (w & 0xFF);
        bkey[11] = (byte) ((w >> 8) & 0xFF);
        BigInteger k0 = new BigInteger(bkey);

        BigInteger exp = new BigInteger("89126272330128007543578052027888001981", 10);
        BigInteger mod = new BigInteger("86f71688cdd2612ca117d1f54bdae029", 16);
        System.out.println(k0);
        BigInteger k1 = k0.modPow(exp, mod);
//        BigInteger k1 = k0.modPow(PRIVATE_EXP, MOD);
        String customerId = Integer.toString(id);
        String sz = "0";
        while (customerId.length() != 5) {
            customerId = sz.concat(customerId);
        }
        customerId = customerId.concat("-");

        String s1 = encodeGroups(k1);

        customerId = customerId.concat(s1);
        return customerId;
    }

    private Idea13KeyGen() {
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        args = new String[]{"*"};
        if (args.length == 0) {
            System.err.printf("*** Usage: %s name%n", Idea13KeyGen.class.getCanonicalName());
            System.exit(1);
        }
        Random r = new Random();
        // 325515161966965809798784783
        // 325515161966965809798784783
        System.out.println(generatorKey(args[0], 0, r.nextInt(100000)));



        BigInteger raw = new BigInteger("123456");
        BigInteger privateExp = new BigInteger("75812bde1f3725f1b6a228405c6ae2107e5697040fc59a7352bf8b3096cc65848b6ea53540453c788e74b4f48b745d8f00f22c182e36b1962004a9f4e3825eb9", 16);
        BigInteger publicExp = RSAKeyGenParameterSpec.F4;
        BigInteger mod = new BigInteger("8a85d574eb2fe69f2cedebaf826e2206fdf9671ed55a3e40d54fdf23dbb7cbf4e2968618eef83a995d5dec55f47a935b4cb8329c3b8c56b92eb73188d730ad8b", 16);
        BigInteger mod2 = new BigInteger("8a85d574eb2fe69f2cedebaf826e2206fdf9671ed55a3e40d54fdf23dbb7cbf4e2968618eef83a995d5dec55f47a935b4cb8329c3b8c56b92eb73188d730ad8b", 16);
        BigInteger encoded = raw.modPow(privateExp, mod);

        BigInteger decoded = encoded.modPow(publicExp, mod2);
        byte[] bytes = decoded.toByteArray();
        System.out.println("length:" + bytes.length);
        System.out.println("raw: " + raw);
        System.out.println("decoded: " + decoded);
        System.out.println("compareTo: " + decoded.compareTo(raw));
        System.out.println("equals: " + decoded.equals(raw));
        System.out.println(decoded.hashCode() + ":" + raw.hashCode());
        System.out.println("array equals:" + Arrays.equals(raw.toByteArray(), decoded.toByteArray()));
        System.out.println("int value ==:" + (raw.intValue() == decoded.intValue()));
    }
}