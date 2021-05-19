package com.github.bingosam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * <p>Title: Module Information  </p>
 * <p>Description: 非线程安全  </p>
 * <p>Copyright: Copyright (c) 2020     </p>
 * <p>Create Time: 2020/8/4          </p>
 *
 * @author zhang kunbin
 */
@RequiredArgsConstructor
public class Rebucket {

    /**
     * 函数到栈顶距离的重要度
     */
    private final double c;

    /**
     * 两个堆栈对应函数的位置差重要度
     */
    private final double o;

    /**
     * 最小的dist
     */
    private final double minDist;

    @Getter
    @Setter
    private double[][] buffer = new double[200][200];

    /**
     * 获取两个堆栈的相似度
     * <p>越小则相似度越高</p>
     *
     * @param request 请求
     * @return 相似度
     */
    private double getDist(DistRequest request) {
        for (int i = 1; i < request.getStack1().getBuffLength(); ++i) {
            for (int j = 1; j < request.getStack2().getBuffLength(); ++j) {
                double x = 0;

                if (request.getStack1().getStack()[i - 1].equals(request.getStack2().getStack()[j - 1])) {
                    x = Math.exp(-c * Math.min(i - 1, j - 1)) * Math.exp(-o * Math.abs(i - j));
                }
                buffer[i][j] = Math.max(
                        Math.max(buffer[i - 1][j - 1] + x, buffer[i - 1][j]),
                        buffer[i][j - 1]
                );
            }
        }

        double sig = 0;
        int minLen = Math.min(request.getStack1().getStack().length, request.getStack2().getStack().length);
        for (int i = 0; i < minLen; ++i) {
            sig += Math.exp(-c * i);
        }
        double sim = buffer[request.getStack1().getStack().length][request.getStack2().getStack().length] / sig;
        return 1.0 - sim;
    }

    /**
     * 获取两个堆栈的相似度
     * <p>越小则相似度越高</p>
     *
     * @param stack1 堆栈1
     * @param stack2 堆栈2
     * @return 相似度
     */
    public double getDist(String[] stack1, String[] stack2) {
        if (stack1.length == 1
                || stack2.length == 1) {
            return 1.0;
        }

        DistRequest request = buildDistRequest(stack1, stack2);
        return getDist(request);
    }

    /**
     * 查找最相似的bucket
     *
     * @param bucketIt bucket迭代器
     * @param filter   bucket过滤器
     * @param stack    堆栈
     * @return 返回null表示找不到相似bucket，否则返回相似的bucket
     */
    public Bucket findTheMostSimilarBucket(Iterator<Bucket> bucketIt, Predicate<Bucket> filter, Stack stack) {
        Bucket result = null;
        double min = 2;
        while (bucketIt.hasNext()) {
            Bucket item = bucketIt.next();
            if (!filter.test(item)) {
                continue;
            }
            for (Stack node : item.getStacks()) {
                double dist = getDist(stack.getFrames(), node.getFrames());
                if (dist == 0.0) {
                    return item;
                }
                if (dist < min) {
                    result = item;
                    min = dist;
                }
            }
        }

        if (min > minDist) {
            result = null;
        }
        return result;
    }

    /**
     * 确保缓冲区大小足够
     *
     * @param stack1BufferLen 堆栈1的缓冲区长度
     * @param stack2BufferLen 堆栈2的缓冲区长度
     */
    private void ensureBufferCapacity(int stack1BufferLen, int stack2BufferLen) {
        if (buffer.length < stack1BufferLen) {
            int maxLen = Math.max(buffer[0].length, stack2BufferLen);
            buffer = new double[stack1BufferLen][maxLen];
        } else if (buffer[0].length < stack2BufferLen) {
            buffer = new double[buffer.length][stack2BufferLen];
        }
    }

    /**
     * 构建请求
     *
     * @param stack1 堆栈1
     * @param stack2 堆栈2
     */
    private DistRequest buildDistRequest(String[] stack1, String[] stack2) {
        if (stack1.length > stack2.length) {
            String[] temp = stack1;
            stack1 = stack2;
            stack2 = temp;
        }

        int stack1BufferLen = stack1.length + 1;
        int stack2BufferLen = stack2.length + 1;
        ensureBufferCapacity(stack1BufferLen, stack2BufferLen);
        return new DistRequest(new StackInfo(stack1), new StackInfo(stack2));
    }

    @Getter
    @AllArgsConstructor
    private static class DistRequest {

        private final StackInfo stack1;

        private final StackInfo stack2;
    }

    @Getter
    private static class StackInfo {

        private final String[] stack;

        private final int buffLength;

        StackInfo(String[] stack) {
            this.stack = stack;
            buffLength = stack.length + 1;
        }
    }

}
