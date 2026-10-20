package com.jxc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单据编号生成器
 * 格式: 前缀 + yyyyMMddHHmmss + 4位序列
 *
 * @author jxc
 */
public class OrderNoGenerator {

    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final int MAX_SEQ = 9999;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

    private OrderNoGenerator() {
    }

    /**
     * 生成单号
     *
     * @param prefix 前缀(如 PO-采购 SO-销售 TR-调拨)
     * @return 单号
     */
    public static String next(String prefix) {
        int seq = SEQ.incrementAndGet();
        if (seq > MAX_SEQ) {
            synchronized (OrderNoGenerator.class) {
                if (SEQ.get() > MAX_SEQ) {
                    SEQ.set(0);
                }
                seq = SEQ.incrementAndGet();
            }
        }
        String datePart = SDF.format(new Date());
        return prefix + datePart + String.format("%04d", seq);
    }
}
