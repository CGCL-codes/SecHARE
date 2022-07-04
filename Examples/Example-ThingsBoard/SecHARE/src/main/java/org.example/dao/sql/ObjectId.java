package org.example.dao.sql;

import org.example.dao.sql.util.ClassLoaderUtil;

import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectId {

    /** Next random number for thread safety */
    private static final AtomicInteger NEXT_INC = new AtomicInteger(ThreadLocalRandom.current().nextInt());
    /** machine info */
    private static final int MACHINE = getMachinePiece() | getProcessPiece();

    /**
     * is valid ObjectId
     *
     * @param s str
     * @return boolean
     */
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        s = removeAllBySeparator(s);
        final int len = s.length();
        if (len != 24) {
            return false;
        }

        char c;
        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * objectId bytes
     *
     * @return objectId
     */
    public static byte[] nextBytes() {
        final ByteBuffer bb = ByteBuffer.wrap(new byte[12]);
        // 4 bit
        bb.putInt((int) (System.currentTimeMillis() / 1000L));
        // 4 bit
        bb.putInt(MACHINE);
        // 4 bit
        bb.putInt(NEXT_INC.getAndIncrement());
        return bb.array();
    }

    /**
     * objectId divided with no underscore
     *
     * @return objectId
     */
    public static String next() {
        return next(false);
    }

    /**
     * get objectId
     *
     * @param withHyphen is include Hyphen
     * @return objectId
     */
    public static String next(boolean withHyphen) {
        byte[] array = nextBytes();
        final StringBuilder buf = new StringBuilder(withHyphen ? 26 : 24);
        int t;
        for (int i = 0; i < array.length; i++) {
            if (withHyphen && i % 4 == 0 && i != 0) {
                buf.append("-");
            }
            t = array[i] & 0xff;
            if (t < 16) {
                buf.append('0');
            }
            buf.append(Integer.toHexString(t));

        }
        return buf.toString();
    }


    private static int getMachinePiece() {
        int machinePiece;
        try {
            StringBuilder netSb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                netSb.append(ni.toString());
            }
            machinePiece = netSb.toString().hashCode() << 16;
        } catch (Throwable e) {
            machinePiece = (ThreadLocalRandom.current().nextInt()) << 16;
        }
        return machinePiece;
    }

    private static int getProcessPiece() {
        final int processPiece;
        int processId;
        try {
            final String processName = ManagementFactory.getRuntimeMXBean().getName();
            final int atIndex = processName.indexOf('@');
            if (atIndex > 0) {
                processId = Integer.parseInt(processName.substring(0, atIndex));
            } else {
                processId = processName.hashCode();
            }
        } catch (Throwable t) {
            processId = ThreadLocalRandom.current().nextInt();
        }

        final ClassLoader loader = ClassLoaderUtil.getClassLoader();
        int loaderId = (loader != null) ? System.identityHashCode(loader) : 0;

        final String processSb = Integer.toHexString(processId) + Integer.toHexString(loaderId);
        processPiece = processSb.hashCode() & 0xFFFF;

        return processPiece;
    }


    private static String removeAllBySeparator(CharSequence str) {
        return (str == null || str.length() == 0) ?
                (null == str ? null : str.toString()) :
                str.toString().replace("-", "");
    }

}
