# web
***
[TOC]

## 简介
	Web 是 servlet 常用操作的辅助工具, 提供了一些常用的 Servlet 操作工具, 包含 Cookie 操作, 文件下载, 文件上传, Servlet 操作

## Cookie
	Servlet API 中操作 Cookie 一般通过以下方式进行
```
// 获取 cookie
for (Cookie cookie : request.getCookies()) {
    if ("".equals(cookie.getName())) {
        // TODO something
    }
}
// 写入 cookie
Cookie cookie = new Cookie("cookieName", "cookieValue");
cookie.setPath("/");		// 某些容器中不设置 path 会造成无法获取
response.addCookie(cookie);

// 删除 cookie
cookie.setMaxAge(0);
response.addCookie(cookie);
```

PW 中对 Servlet Cookie 进行了简单的封装, PW 中 Cookie 由 ```freework.web.Cookie``` 定义, 接口操作与 Servlet API 完全兼容
且提供了以下方法来简化 cookie 操作

| 方法 | 说明 |
|--------|--------|
| readValue | 读取当前cookie值 |
| saveTo | 保存当前 cookie 值 |
| removeFrom | 移除 cookie |

使用 Cookie 操作 Cookie 如下:
```
// 读取 cookie
Cookie cookie = new Cookie("name");
String clientValue = cookie.readValue(request, response);

// 保存 cookie
cookie.setValue("deleteMe");
cookie.saveTo(request, response);

// 删除 cookie
cookie.removeFrom(request, response);
```
另外 Cookie 还提供了对 ``HttpOnly`` 的支持, 通过 setHttpOnly/getHttpOnly 来处理

## DownloadView - 文件下载
	PW 提供 DownloadView 来简化文件下载操作(文件名称, 下载文件头等设置), 对于文件下载，有两种方式
1. 复写 ``getResourceAsStream`` 来提供数据流
2. 复写 ``doDownloadInternal`` 来处理下载逻辑
典型的文件下载代码如下：
```
    new DownloadView("中文名称.xls", "application/vnd.ms-excel") {
        @Override
        protected InputStream getResourceAsStream() {
            return new FileInputStream(file);
        }
    }.sendTo(request, response);

    new DownloadView("中文名称.xls", "application/vnd.ms-excel") {
        @Override
        protected void doDownloadInternal(HttpServletRequest request, HttpServletResponse response) throws IOException {
            // ...
            InputStream in = ...;
            IOUtils.flow(is, response.getOutputStream(), true, false);
        }
    }.sendTo(request, response);
```

## ExcelView - Excel导出
对于数据的导出, DownloadView 提供一个子类 ExcelView 来对其进行支持, 方便进行导出 excel.
ExcelView 是一个抽象类, 在实际使用时需要复写其 buildData 方法来将每个数据对象转换为导出的数据行.
```
String[] headers = { "产品ID", "产品名称" };      // excel 标题
Iterable<Product> lazyPagingProducts =  ....;       // 要导出的数据
new ExcelView<Product>("products.xls", lazyPagingProducts, headers) {
    @Override
    protected Object[] buildData(Product entry) {
        return new Object[] {
                entry.getId(),
                entry.getTitle()
        };
    }
}.sendTo(request, response);
```
对于构建数据时只需要读取 property 值的, ExcelView 提供了更加便捷的操作方式
```
String[] headers = { "产品ID", "产品名称" };      // excel 标题
String[] props = { "id", "title" };               // 要导出的对象属性, 顺序与header一致
Iterable<Product> lazyPagingProducts =  ....;       // 要导出的数据
ExcelView.<Product>create(
        "product.xls",
        lazyPagingProducts,
        headers,
        props
).sendTo(request, response);
```

## Multipart 文件上传
PW 提供 `Multipart` 来处理 Web 上传功能, 其提供以下两个方法：

| 方法 | 说明 |
|-------|-------|
| isMultipartRequest | 是否是文件上传请求 |
| parse | 解析文件上传请求 |

```
// 如果是文件上传请求, 则解析
if (Multipart.isMultipartRequest(request)) {

	// 注意, 该方法是流式 API, 不可重复使用, 如果希望重复读取文件, 可自行写入储存设备
    Multipart.parse(request, new Multipart.Handler() {
        @Override
        public void onParameter(String name, String value, Map<String, String> multipartHeaders) {
            // 处理参数
        }

        @Override
        public void onPart(String name, String filename, InputStream multipartIn, Map<String, String> multipartHeaders) throws IOException {
            // 处理文件上传
        }
    });
}
```

## 字符编码转换过滤器 - CharacterEncodingFilter
字符编码过滤器已经很常见了, 因此不多做解释.
为什么又来一个字符编码过滤器?
在 Servlet API 中 `request.setCharacterEncoding` 只对 post 参数有效, 对于 GET 是无效的, 而大多数 Filter 也只是设置该参数, 对于 URL 中参数的解码, 一般依赖于容器配置, 例如 tomcat 的 `URIEncoding`, 而对于 IE 和 其他浏览器 GET 方法对参数的编码是不一样的, 因此通过容器设置也不能保证乱码问题的解决.
	***PW 的 `CharacterEncodingFilter` 增加了一个功能: 自动检测 GET 请求参数编码并进行解码.***
注: `当前解码只针对UTF-8/GBK进行检测, 且只对 GET 请求有效`, 对于非 GET 请求, URL 中参数不会被解析

使用:
```
<!-- S charset encoding filter -->
<filter>
    <filter-name>encoding-filter</filter-name>
    <filter-class>freework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
    	<!-- request.setCharaterEncoding -->
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>encoding-filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
<!-- E charset encoding filter -->
```

## WebRequestContext - 更加方便获取 request, response
Java web 应用不可避免需要频繁的获取 request, response, 但是通过参数传递, 注入等方式获取这些对象，某些时候感觉比较麻烦， PW 提供一个辅助类 -- `WebRequestContext` 来方便获取请求上下文中的相关对象.
通过 `WebRequestContext.getRequiredContext()` 来获取 `WebRequestContext` 对象, 其核心方法如下:

| 方法 | 说明 |
|--------|--------|
| getRequest | 获取当前请求的 HttpServletRequest |
| getResponse | 获取当前请求的 HttpServletResponse |
| getSession | 获取当前请求的 HttpSession |
| getAttribute | 获取当前请求给定作用域的属性 |
| resolve | 在当前请求上下文中解析给定的 EL 表达式 |

使用样例：
```
	WebRequestContext context = WebRequestContext.getRequiredContext();
    HttpServletRequest request = context.getRequest();
    HttpServletResponse response = context.getResponse();

    String contextPath = request.getContextPath();
    response.sendRedirect(contextPath + "/index.html");

    String param = context.resolve("${param.cat}");
```
使用配置:
	要使用 `WebRequestContext` 需要在 `web.xml` 添加如下配置来支持(建议配置在 encoding filter 之后, 其他 filter 之前)
```
<!-- S context filter -->
<filter>
    <filter-name>context-filter</filter-name>
    <filter-class>freework.web.WebRequestContextFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>context-filter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>FORWARD</dispatcher>
</filter-mapping>
<!-- E context filter -->
```

## 更多工具。。
	PW 还提供了一些其他常用的 Web 操作类

| 类 | 说明 |
|--------|--------|

## 其他
其实什么也没有, 只做了最常用的事情...