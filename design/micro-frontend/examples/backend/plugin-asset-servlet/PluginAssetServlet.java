package com.qlangtech.tis.runtime.module.action;

import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 插件前端资源服务Servlet
 *
 * <p>提供插件包中前端资源（JavaScript、CSS等）的HTTP访问。
 *
 * <h3>URL格式</h3>
 * <pre>
 * /plugin-assets/{pluginName}/{resourcePath}
 *
 * 示例:
 * /plugin-assets/tis-ontology-plugin/ontology-res-inference.bundle.js
 * /plugin-assets/tis-jdbc-plugin/assets/jdbc-type-selector.css
 * </pre>
 *
 * <h3>资源加载机制</h3>
 * <ol>
 *   <li>从URL中提取pluginName和resourcePath</li>
 *   <li>通过PluginManager获取插件的ClassLoader</li>
 *   <li>从ClassLoader加载META-INF/webapp/plugin-assets/下的资源</li>
 *   <li>设置缓存头并返回资源内容</li>
 * </ol>
 *
 * <h3>缓存策略</h3>
 * <ul>
 *   <li>强缓存：Cache-Control: public, max-age=31536000 (1年)</li>
 *   <li>协商缓存：ETag (基于pluginName + version + resourcePath)</li>
 *   <li>支持304 Not Modified响应</li>
 * </ul>
 *
 * <h3>安全性</h3>
 * <ul>
 *   <li>路径校验：禁止".."和绝对路径，防止目录遍历攻击</li>
 *   <li>资源隔离：只能访问META-INF/webapp/plugin-assets/目录</li>
 *   <li>插件隔离：通过ClassLoader隔离不同插件的资源</li>
 * </ul>
 *
 * <h3>Web.xml配置</h3>
 * <pre>{@code
 * <servlet>
 *     <servlet-name>PluginAssetServlet</servlet-name>
 *     <servlet-class>com.qlangtech.tis.runtime.module.action.PluginAssetServlet</servlet-class>
 * </servlet>
 *
 * <servlet-mapping>
 *     <servlet-name>PluginAssetServlet</servlet-name>
 *     <url-pattern>/plugin-assets/*</url-pattern>
 * </servlet-mapping>
 * }</pre>
 *
 * @author TIS Team
 * @date 2026-06-11
 * @see PluginManager
 */
public class PluginAssetServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 资源在插件包中的基础路径 */
    private static final String RESOURCE_BASE_PATH = "META-INF/webapp/plugin-assets/";

    /** ETag缓存，避免重复计算 */
    private final ConcurrentMap<String, String> etagCache = new ConcurrentHashMap<>();

    /**
     * 处理GET请求
     *
     * @param req HTTP请求
     * @param resp HTTP响应
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (StringUtils.isEmpty(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing resource path");
            return;
        }

        // 解析路径: /pluginName/resourcePath
        String[] parts = pathInfo.substring(1).split("/", 2);
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid resource path format. Expected: /plugin-assets/{pluginName}/{resourcePath}");
            return;
        }

        String pluginName = parts[0];
        String resourcePath = parts[1];

        // 安全检查：防止目录遍历攻击
        if (resourcePath.contains("..") || resourcePath.startsWith("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid resource path: directory traversal not allowed");
            return;
        }

        serveResource(pluginName, resourcePath, req, resp);
    }

    /**
     * 加载并返回资源
     *
     * @param pluginName 插件名称
     * @param resourcePath 资源路径（相对于plugin-assets目录）
     * @param req HTTP请求
     * @param resp HTTP响应
     * @throws IOException IO异常
     */
    private void serveResource(String pluginName, String resourcePath,
                               HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String fullResourcePath = RESOURCE_BASE_PATH + resourcePath;

        // 获取插件ClassLoader
        ClassLoader pluginClassLoader = getPluginClassLoader(pluginName);
        if (pluginClassLoader == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Plugin not found: " + pluginName);
            return;
        }

        // 加载资源
        InputStream resourceStream = pluginClassLoader.getResourceAsStream(fullResourcePath);
        if (resourceStream == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Resource not found: " + resourcePath + " in plugin " + pluginName);
            return;
        }

        try {
            // 检查If-None-Match（ETag协商缓存）
            String etag = generateETag(pluginName, resourcePath);
            String ifNoneMatch = req.getHeader("If-None-Match");
            if (etag.equals(ifNoneMatch)) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            // 设置响应头
            String contentType = getContentType(resourcePath);
            resp.setContentType(contentType);
            resp.setCharacterEncoding(TisUTF8.getName());

            // 设置缓存头
            resp.setHeader("ETag", etag);
            resp.setHeader("Cache-Control", "public, max-age=31536000, immutable"); // 1年强缓存
            resp.setDateHeader("Expires", System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // 1年后过期

            // 跨域支持（如果需要）
            // resp.setHeader("Access-Control-Allow-Origin", "*");

            // 输出资源内容
            IOUtils.copy(resourceStream, resp.getOutputStream());
            resp.flushBuffer();

        } finally {
            IOUtils.closeQuietly(resourceStream);
        }
    }

    /**
     * 获取插件的ClassLoader
     *
     * @param pluginName 插件名称
     * @return ClassLoader，如果插件不存在返回null
     */
    private ClassLoader getPluginClassLoader(String pluginName) {
        try {
            PluginManager pm = PluginManager.getInstance();
            // 这里需要根据实际的PluginManager API调整
            // 示例代码，具体实现取决于TIS的插件管理机制
            return pm.getPluginClassLoader(pluginName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据文件扩展名猜测Content-Type
     *
     * @param resourcePath 资源路径
     * @return Content-Type
     */
    private String getContentType(String resourcePath) {
        // 优先使用JDK的Content-Type猜测
        String contentType = URLConnection.guessContentTypeFromName(resourcePath);
        if (contentType != null) {
            return contentType;
        }

        // 手动处理常见的Web资源类型
        String lowerPath = resourcePath.toLowerCase();

        if (lowerPath.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (lowerPath.endsWith(".mjs")) {
            return "application/javascript; charset=utf-8";
        } else if (lowerPath.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (lowerPath.endsWith(".json")) {
            return "application/json; charset=utf-8";
        } else if (lowerPath.endsWith(".map")) {
            return "application/json; charset=utf-8";
        } else if (lowerPath.endsWith(".wasm")) {
            return "application/wasm";
        } else if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".ico")) {
            return "image/x-icon";
        } else if (lowerPath.endsWith(".woff")) {
            return "font/woff";
        } else if (lowerPath.endsWith(".woff2")) {
            return "font/woff2";
        } else if (lowerPath.endsWith(".ttf")) {
            return "font/ttf";
        } else if (lowerPath.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        }

        // 默认类型
        return "application/octet-stream";
    }

    /**
     * 生成ETag
     *
     * <p>ETag格式: "pluginName-version-hash"
     * <p>用于协商缓存，当资源未变化时返回304 Not Modified
     *
     * @param pluginName 插件名称
     * @param resourcePath 资源路径
     * @return ETag值
     */
    private String generateETag(String pluginName, String resourcePath) {
        String cacheKey = pluginName + ":" + resourcePath;

        // 从缓存获取
        String cachedEtag = etagCache.get(cacheKey);
        if (cachedEtag != null) {
            return cachedEtag;
        }

        // 生成新的ETag
        String version = getPluginVersion(pluginName);
        int hash = (pluginName + "-" + version + "-" + resourcePath).hashCode();
        String etag = "\"" + pluginName + "-" + version + "-" + Integer.toHexString(hash) + "\"";

        // 缓存ETag
        etagCache.put(cacheKey, etag);

        return etag;
    }

    /**
     * 获取插件版本号
     *
     * @param pluginName 插件名称
     * @return 版本号，如果获取失败返回"unknown"
     */
    private String getPluginVersion(String pluginName) {
        try {
            PluginManager pm = PluginManager.getInstance();
            // 这里需要根据实际的PluginManager API调整
            return pm.getPluginVersion(pluginName);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 支持HEAD请求（用于检查资源是否存在）
     */
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
