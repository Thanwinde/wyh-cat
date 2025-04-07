package com.wyhCat.engin.mapping;

import java.util.regex.Pattern;

/**
 * AbstractMapping 类用于表示一个 URL 映射模式。
 * 它通过正则表达式匹配 URL，并且根据 URL 的优先级对映射进行排序。
 * 该类实现了 Comparable 接口，允许根据优先级对多个映射进行排序。
 *
 * @author nsh
 * @data 2025/4/7 19:19
 */
public class AbstractMapping implements Comparable<AbstractMapping>{

    // 正则表达式模式，用于匹配 URL
    final Pattern pattern;

    // 存储 URL 模式
    final String url;

    /**
     * 构造函数，初始化 URL，并生成对应的正则表达式模式
     *
     * @param url 用于初始化映射的 URL 模式
     */
    public AbstractMapping(final String url) {
        this.url = url;
        pattern = buildPattern(url);  // 根据 URL 生成正则表达式模式
    }

    /**
     * 匹配给定的 URL 是否符合当前映射的模式
     *
     * @param url 要匹配的 URL
     * @return 如果给定 URL 匹配当前模式，返回 true；否则返回 false
     */
    public boolean matches(final String url) {
        return pattern.matcher(url).matches();  // 使用正则表达式检查匹配
    }

    /**
     * 计算当前映射的优先级。优先级越高，值越大。
     * 优先级规则：
     * 1. 如果 URL 是根目录 ("/")，优先级最高。
     * 2. 如果 URL 以 "*" 开头，优先级次高。
     * 3. 否则，优先级根据 URL 长度确定，长度越短优先级越高。
     *
     * @return 当前映射的优先级值
     */
    int priority() {
        if (this.url.equals("/")) {
            return Integer.MAX_VALUE;  // 根目录映射优先级最大
        }
        if (this.url.startsWith("*")) {
            return Integer.MAX_VALUE - 1;  // 以 * 开头的 URL 次优先级
        }
        return 100000 - this.url.length();  // 根据 URL 长度确定优先级，越短优先级越高
    }

    /**
     * 构建一个正则表达式，用于匹配符合 URL 模式的字符串。
     * 该方法将 URL 中的 "*" 替换为 ".*"，使其成为一个正则表达式。
     *
     * @param urlPattern URL 模式字符串
     * @return 编译后的正则表达式模式
     */
    Pattern buildPattern(String urlPattern) {
        StringBuilder sb = new StringBuilder(urlPattern.length() + 16);  // 创建 StringBuilder 用于构建正则表达式
        sb.append('^');  // 正则表达式开始
        for (int i = 0; i < urlPattern.length(); i++) {
            char ch = urlPattern.charAt(i);  // 获取当前字符
            if (ch == '*') {
                sb.append(".*");  // 将 '*' 替换为正则中的 '.*'，表示任意字符
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                sb.append(ch);  // 字母和数字直接添加到正则中
            } else {
                sb.append('\\').append(ch);  // 对其他特殊字符进行转义
            }
        }
        sb.append('$');  // 正则表达式结束
        return Pattern.compile(sb.toString());  // 编译并返回正则表达式模式
    }

    /**
     * 比较当前映射与另一个映射的优先级。优先级高的排在前面。
     * 如果优先级相同，则根据 URL 字符串的字典顺序进行比较。
     *
     * @param o 另一个 AbstractMapping 对象
     * @return 如果当前对象的优先级高，则返回正数；如果优先级低，则返回负数；如果相同，则根据 URL 比较
     */
    @Override
    public int compareTo(AbstractMapping o) {
        int cmp = this.priority() - o.priority();  // 比较优先级
        if (cmp == 0) {
            cmp = this.url.compareTo(o.url);  // 如果优先级相同，根据 URL 字符串进行比较
        }
        return cmp;
    }
}
