package com.wyhCat.engin;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


import com.wyhCat.engin.mapping.FilterMapping;
import com.wyhCat.engin.mapping.ServletMapping;
import com.wyhCat.utils.AnnoUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;

//一个wyhcat只有一个ServletContextImpl，存储了组件的注册信息与mapping映射
//一个组件对应着一个注册信息，多条mapping映射
public class ServletContextImpl implements ServletContext {

    final Logger logger = LoggerFactory.getLogger(getClass());
    //日志工厂

    private Map<String, ServletRegistrationImpl> servletRegistrations = new HashMap<>();
    //管理所有servlet组件的注册信息，用servlet的名字作为key

    private Map<String, FilterRegistrationImpl> filterRegistrations = new HashMap<>();
    //名字对应的 FilterRegistrationImpl，一个组件对应一个

    final Map<String, Servlet> nameToServlets = new HashMap<>();
    //用name映射到对应的servlet，方便

    final Map<String, Filter> nameToFilters = new HashMap<>();
    //name对应的Filter

    final List<ServletMapping> servletMappings = new ArrayList<>();
    //servlet映射表

    final List<FilterMapping> filterMappings = new ArrayList<>();
    //filter URL映射表

     SessionManager sessionManager = new SessionManager(this);
     //session管理器

    List<HttpSessionListener> httpSessionListeners;
    //session监听器

    List<HttpSessionAttributeListener> httpSessionAttributeListeners;
    //session属性监听器

    //处理请求：把请求转给servlet处理
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getRequestURI();
        // 通过请求URL来找对应的servlet（通过正则表达式匹配来找）
        Servlet servlet = null;
        for (ServletMapping mapping : this.servletMappings) {
            if (mapping.matches(path)) {
                servlet = mapping.servlet;
                break;
            }
        }

        //没找到servlet，写回失败信息
        if (servlet == null) {
            // 404 Not Found:
            PrintWriter pw = response.getWriter();
            pw.write("<h1>404 Not Found</h1><p>No mapping for URL: " + path + "</p>");
            pw.close();
            return;
        }

        List<Filter> enabledFilters = new ArrayList<>();
        //找到所有匹配上的过滤器，加入enabledFilters中
        for(FilterMapping mapping : this.filterMappings) {
            if (mapping.matches(path)) {
                enabledFilters.add(mapping.filter);
            }
        }
        Filter[] filters = enabledFilters.toArray(Filter[]::new);
        logger.info(" 由filter {} 处理, servlet: {}", Arrays.toString(filters), servlet);
        FilterChain chain = new FilterChainImpl(filters, servlet);
        try {
            chain.doFilter(request, response);
        } catch (ServletException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        //FilterChain会在最后调用servlet，所以不用再加上了
        //servlet.service(request, response);
    }

    //用来给servlet初始化
    //每个servlet在创建时具有一个mapping和register，mapping不需要初始化，register需要初始化
    public void initServlet(List<Class<?>> servletClasses) {
        //遍历每一个servlet
        for (Class<?> c : servletClasses) {
            //获取注解对象
            //servlet就是通过注解来标注servlet的各种信息
            //注解由servlet产生
            WebServlet ws = c.getAnnotation(WebServlet.class);
            if (ws != null) {
                logger.info("自动注册Servlet: {}", c.getName());
                Class<? extends Servlet> clazz = (Class<? extends Servlet>) c;
                //转成Servlet类
                ServletRegistration.Dynamic registration = this.addServlet(AnnoUtils.getServletName(clazz), clazz);
                //调用addServlet将映射URL加到servletRegistrations中
                registration.addMapping(AnnoUtils.getServletUrlPatterns(clazz));
                //把注解中的映射url添加到注册中心中
                registration.setInitParameters(AnnoUtils.getServletInitParams(clazz));
                //设置初始参数（注释中带有的）
            }
        }

        // 遍历servletRegistrations将其中每一个注册信息初始化，也就是吧servlet初始化
        for (String name : this.servletRegistrations.keySet()) {
            ServletRegistrationImpl registration = this.servletRegistrations.get(name);
            //通过名字获取到对应的注册对象
            try {
                registration.servlet.init(registration.getServletConfig());
                //为servlet获得初始配置（名字，上下文，初始参数）
                this.nameToServlets.put(name, registration.servlet);
                //把名字对应到容器
                for (String urlPattern : registration.getMappings()) {
                    this.servletMappings.add(new ServletMapping(urlPattern, registration.servlet));
                }
                //获取注册信息中的映射URL，一个servlet能有多条映射，分别会创建一个servletMappings
                registration.initialized = true;
                //将该类的注册信息设成已初始化
            } catch (ServletException e) {
                logger.error("初始化该组件失败: " + name + " / " + registration.servlet.getClass().getName(), e);
            }
        }
        // important: sort mappings:
        Collections.sort(this.servletMappings);
        //对映射进行排序，详见AbstractMapping
    }


    public void initFilters(List<Class<?>> filterClasses) {
        //和上文的初始化servlet组件一样，先是全部注册再初始化
        for(Class<?> c : filterClasses) {
            WebFilter wf = c.getAnnotation(WebFilter.class);
            if (wf != null) {
                logger.info("自动注册过滤器Filter: {}", c.getName());

                Class<? extends Filter> clazz = (Class<? extends Filter>) c;
                FilterRegistration.Dynamic registration = this.addFilter(AnnoUtils.getFilterName(clazz), clazz);
                registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),true,AnnoUtils.getFilterUrlPatterns(clazz));
                registration.setInitParameters(AnnoUtils.getFilterInitParams(clazz));
            }
        }

        for(String name : this.filterRegistrations.keySet()) {
            FilterRegistrationImpl registration = this.filterRegistrations.get(name);

            try {
                registration.filter.init(registration.getFilterConfig());
                this.nameToFilters.put(name, registration.filter);
                for(String urlPattern : registration.getUrlPatternMappings()){
                this.filterMappings.add(new FilterMapping(urlPattern, registration.filter));
            }
            registration.initialized = true;
            } catch (ServletException e) {
                logger.error("过滤器初始化失败:{}", name + " / " + registration.filter.getClass().getName(), e);
                throw new RuntimeException(e);
            }
        }
    }

    public void initListeners(List<Class<?>> listenerClasses) {
        for(Class<?> c : listenerClasses) {
            this.addListener((Class<? extends EventListener>) c);
            logger.info("加载Listener: {}", c.getName());
        }
    }

    @Override
    public String getContextPath() {
        // 只支持部署在根目录上
        return "";
    }

    @Override
    public ServletContext getContext(String uripath) {
        if ("".equals(uripath)) {
            return this;
        }
        // 同上
        return null;
    }

    @Override
    public String getMimeType(String file) {
        //获取文件类型
        String defaultMime = "application/octet-stream";
        Map<String, String> mimes = Map.of(".html", "text/html", ".txt", "text/plain", ".png", "image/png", ".jpg", "image/jpeg");
        int n = file.lastIndexOf('.');
        if (n == -1) {
            return defaultMime;
        }
        String ext = file.substring(n);
        return mimes.getOrDefault(ext, defaultMime);
    }

    @Override
    public String getInitParameter(String name) {
        // 不支持初始参数
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        // 同上
        return Collections.emptyEnumeration();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        //同上
        throw new UnsupportedOperationException("setInitParameter");
    }

    @Override
    //传入servlet和名字（可选），会创建一个对应的未初始化的注册对象
    public ServletRegistration.Dynamic addServlet(String name, String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("class name is null or empty.");
        }
        Servlet servlet = null;
        try {
            Class<? extends Servlet> clazz = createInstance(className);
            servlet = createInstance(clazz);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addServlet(name, servlet);
    }

    @Override
    //传入servlet和名字（可选），会创建一个对应的未初始化的注册对象
    public ServletRegistration.Dynamic addServlet(String name, Class<? extends Servlet> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null.");
        }
        Servlet servlet = null;
        try {
            servlet = createInstance(clazz);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addServlet(name, servlet);
    }

    @Override
    //传入servlet和名字（可选），会创建一个对应的未初始化的注册对象
    public ServletRegistration.Dynamic addServlet(String name, Servlet servlet) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }
        if (servlet == null) {
            throw new IllegalArgumentException("servlet is null.");
        }
        var registration = new ServletRegistrationImpl(this, name, servlet);
        this.servletRegistrations.put(name, registration);
        return registration;
    }

    @Override
    //创建一个servlet
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return createInstance(clazz);
    }

    @Override
    //获取注册信息
    public ServletRegistration getServletRegistration(String name) {
        return this.servletRegistrations.get(name);
    }

    @Override
    //获取全部注册信息
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Map.copyOf(this.servletRegistrations);
    }

    // Servlet API version: 6.0.0

    @Override
    public int getMajorVersion() {
        return 6;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 6;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }
//createInstance会尝试调用指定类的无参构造方法创建一个类返回
    private <T> T createInstance(String className) throws ServletException {
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found.", e);
        }
        return createInstance(clazz);
    }

    private <T> T createInstance(Class<T> clazz) throws ServletException {
        try {
            //TODO  这里不用强转而是直接创建一个新的是为什么?
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ServletException("Cannot instantiate class " + clazz.getName(), e);
        }
    }

    // TODO ///////////////////////////////////////////////////////////////////

    @Override
    public Set<String> getResourcePaths(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void log(String msg) {
        // TODO Auto-generated method stub
    }

    @Override
    public void log(String message, Throwable throwable) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getRealPath(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletContextName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dynamic addFilter(String filterName, String className) {
        if(className == null || className.isEmpty()) {
            throw new IllegalArgumentException("class name is null or empty.");
        }
        Filter filter = null;
        try {
            Class<? extends Filter> filterClass = createInstance(className);
            filter = createInstance(filterClass);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addFilter(filterName, filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
      if (filter == null) {
          throw new IllegalArgumentException("filter is null.");
      }
      if(filterName == null || filterName.isEmpty()) {
          throw new IllegalArgumentException("filter name is null or empty.");
      }
      FilterRegistrationImpl registration = new FilterRegistrationImpl(this,filterName,filter);
      this.filterRegistrations.put(filterName, registration);
      return (FilterRegistration.Dynamic) registration;
    }

    @Override
    public Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
       if (filterClass == null) {
           throw new IllegalArgumentException("filterClass is null.");
       }
       Filter filter = null;
        try {
            filter = createInstance(filterClass);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addFilter(filterName, filter);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return createInstance(clazz);
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return this.filterRegistrations.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Map.copyOf(this.filterRegistrations);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        // TODO Auto-generated method stub
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addListener(String className) {
        try {
            addListener((Class<? extends EventListener>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        if(t instanceof HttpSessionListener){
            if(this.httpSessionListeners == null){
                this.httpSessionListeners = new ArrayList<>();
            }
            this.httpSessionListeners.add((HttpSessionListener) t);
        }else
        if(t instanceof HttpSessionAttributeListener){
            if(this.httpSessionAttributeListeners == null){
                this.httpSessionAttributeListeners = new ArrayList<>();
            }
            this.httpSessionAttributeListeners.add((HttpSessionAttributeListener) t);
        }else
            throw new UnsupportedOperationException("尚不支持此监听器");
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        try {
            addListener(listenerClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void invokeSessionCreated(HttpSession session) {
        HttpSessionEvent event = new HttpSessionEvent(session);
        for(HttpSessionListener listener : this.httpSessionListeners){
            listener.sessionCreated(event);
        }
    }

    public void invokeSessionDestroyed(HttpSession session) {
        HttpSessionEvent event = new HttpSessionEvent(session);
        for(HttpSessionListener listener : this.httpSessionListeners){
            listener.sessionDestroyed(event);
        }
    }

    public void invokeSessionAttributeAdded(HttpSessionBindingEvent event) {
        for(HttpSessionAttributeListener listener : this.httpSessionAttributeListeners){
            listener.attributeAdded(event);
        }
    }

    public void invokeSessionAttributeRemoved(HttpSessionBindingEvent event) {
        for(HttpSessionAttributeListener listener : this.httpSessionAttributeListeners){
            listener.attributeRemoved(event);
        }
    }

    public void invokeSessionAttributeReplaced(HttpSessionBindingEvent event) {
        for(HttpSessionAttributeListener listener : this.httpSessionAttributeListeners){
            listener.attributeReplaced(event);
        }
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void declareRoles(String... roleNames) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getVirtualServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSessionTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getRequestCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getResponseCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        // TODO Auto-generated method stub
    }

    @Override
    public Object getAttribute(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(String name, Object object) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeAttribute(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public jakarta.servlet.ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        // TODO Auto-generated method stub
        return null;
    }
}