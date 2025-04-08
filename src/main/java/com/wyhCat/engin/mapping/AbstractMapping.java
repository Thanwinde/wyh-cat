package com.wyhCat.engin.mapping;

import java.util.regex.Pattern;

public class AbstractMapping implements Comparable<AbstractMapping> {

    //url的对应的正则表达式
    final Pattern pattern;
    //完整的映射url
    final String url;

    public AbstractMapping(String urlPattern) {
        this.url = urlPattern;
        this.pattern = buildPattern(urlPattern);
    }

    public boolean matches(String uri) {
        return pattern.matcher(uri).matches();
    }
    //假如有如下映射:
    //  /
    //  *error
    //  /user
    //  /user/profile
    //  优先级重上到下递减，便于快速匹配

    //这里会生成一个正则表达式，*会替换成.*,特殊字符会加上/防止转意，其他字符会全部保留
    Pattern buildPattern(String urlPattern) {
        StringBuilder sb = new StringBuilder(urlPattern.length() + 16);
        sb.append('^');
        for (int i = 0; i < urlPattern.length(); i++) {
            char ch = urlPattern.charAt(i);
            if (ch == '*') {
                sb.append(".*");
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                sb.append(ch);
            } else {
                sb.append('\\').append(ch);
            }
        }
        //加上结束标记
        sb.append('$');
        return Pattern.compile(sb.toString());
    }

    //比较两个映射哪一个更合适
    @Override
    public int compareTo(AbstractMapping o) {
        int cmp = this.priority() - o.priority();
        if (cmp == 0) {
            cmp = this.url.compareTo(o.url);
        }
        return cmp;
    }

    int priority() {
        //如果url是根路径，返回最高优先
        if (this.url.equals("/")) {
            return Integer.MAX_VALUE;
        }
        if (this.url.startsWith("*")) {
            return Integer.MAX_VALUE - 1;
        }
        return 100000 - this.url.length();
    }
}