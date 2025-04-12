package com.wyhCat.engin.mapping;

import java.util.regex.Pattern;

//这里实现Comparable接口，能对其进行方便的自定义排序
//这个主要是对servlet组件映射服务，一个组件每一个URL都会通过ServletMapping继承这个类
public class AbstractMapping implements Comparable<AbstractMapping> {

    //一个url的对应的正则表达式
    final Pattern pattern;
    //完整的映射url
    final String url;

    public AbstractMapping(String urlPattern) {
        this.url = urlPattern;
        this.pattern = buildPattern(urlPattern);
    }

    //判断是否符合，用正则表达式来判断传入的URL是否符合该映射
    public boolean matches(String uri) {
        return pattern.matcher(uri).matches();
    }


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

    //比较两个对象哪个优先级更高
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
    //假如有如下映射:
    //  /
    //  */error
    //  /user
    //  /user/profile
    //  优先级从上到下递减，对于servlet组件可能没有用，但对于过滤器来说需要从上到下匹配
}