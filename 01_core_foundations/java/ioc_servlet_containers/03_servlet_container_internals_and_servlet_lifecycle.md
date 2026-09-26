# Chapter 3: Servlet Container Internals, Lifecycle & Bridge Architecture

Explores Servlet Container mechanics (`init`/`service`/`destroy`), `FilterChain` processing, and the `ContextLoaderListener` bridge to IoC `WebApplicationContext`.

---

## 📌 1. Servlet Interface & Lifecycle

A **Servlet** is a Java component managed by a Servlet Container (Tomcat, Jetty) that processes incoming requests and generates responses.

```mermaid
stateDiagram-v2
    [*] --> Unloaded : Servlet Class Loaded by WebAppClassLoader
    Unloaded --> Initialized : Container invokes init(ServletConfig)
    Initialized --> Ready : Ready to process requests
    
    state Ready {
        [*] --> ProcessingRequest : Concurrent Threads invoke service(req, resp)
        ProcessingRequest --> DispatchMethod : service() routes to doGet() / doPost()
        DispatchMethod --> [*]
    }

    Ready --> Destroyed : Container Shutdown invokes destroy()
    Destroyed --> [*]
```

### Lifecycle Methods
1. **`init(ServletConfig config)`**: Executed **exactly once** when the container loads the Servlet.
2. **`service(ServletRequest req, ServletResponse resp)`**: Executed **concurrently by multiple worker threads** for every incoming HTTP request.
3. **`destroy()`**: Executed **exactly once** when the web application shuts down.

---

## 📌 2. `FilterChain` Execution Mechanism (Chain of Responsibility)

Servlet Filters inspect or modify requests/responses prior to reaching the target Servlet.

```mermaid
sequenceDiagram
    participant Container as Servlet Container Engine
    participant F1 as SecurityFilter
    participant F2 as LoggingFilter
    participant Servlet as HttpServlet (DispatcherServlet)

    Container->>F1: doFilter(req, resp, chain)
    F1->>F1: Check Auth Tokens
    F1->>F2: chain.doFilter(req, resp)
    F2->>F2: Start Log Timer
    F2->>Servlet: chain.doFilter(req, resp)
    Servlet->>Servlet: service(req, resp) -> Process Request
    Servlet-->>F2: Return Response
    F2-->>F1: Log Response Time & Return
    F1-->>Container: Return Response to Client
```

---

## 📌 3. `ContextLoaderListener` Bridge Architecture

How does an IoC container (like Spring) integrate seamlessly with a Servlet Container (like Tomcat)?

```mermaid
flowchart TD
    TomcatBoot["Tomcat Starts Web Application"] --> Event["Fires ServletContextEvent (Web App Deployed)"]
    Event --> Listener["ContextLoaderListener.contextInitialized(event)"]
    
    Listener --> Instantiate["Instantiate WebApplicationContext (IoC Container)"]
    Instantiate --> Scan["Scan Annotations & Initialize All Singleton Beans"]
    
    Scan --> Bind["Bind IoC Container to ServletContext Attribute:\nServletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, iocContext)"]
    
    Bind --> DispatcherBoot["DispatcherServlet Initialized"]
    DispatcherBoot --> Lookup["Lookup Controllers & Handlers from Root WebApplicationContext"]
```

### Java Code: Custom `ContextLoaderListener` Bridge Implementation
```java
package com.custom.servlet;

import com.custom.ioc.SimpleIoCContainer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextLoaderListener implements ServletContextListener {

    public static final String IOC_CONTAINER_ATTRIBUTE = "COM_CUSTOM_IOC_CONTAINER";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Web Container] Initializing Root IoC WebApplicationContext...");
        
        // 1. Instantiate Custom IoC Container
        SimpleIoCContainer iocContainer = new SimpleIoCContainer();
        
        // 2. Register & Initialize Beans
        iocContainer.register(UserDao.class);
        iocContainer.register(UserService.class);
        iocContainer.getBean("userService"); // Pre-instantiate singletons
        
        // 3. Bind IoC Container instance to ServletContext attribute!
        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute(IOC_CONTAINER_ATTRIBUTE, iocContainer);
        
        System.out.println("[Web Container] Root IoC Container successfully bound to ServletContext!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[Web Container] Shutting down IoC Container...");
    }
}
```
