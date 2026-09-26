# IoC & Servlet Containers — Architecture, Implementation & Internals

A staff-engineer level master guide covering **Inversion of Control (IoC) Containers**, building a custom **IoC & Dependency Injection Engine from scratch in Java**, **Bean Lifecycles & Post-Processors**, and **Servlet Container Internals**.

---

## 📂 Chapter Index & Roadmap

| Chapter / Guide | Core Concepts | Location |
|---|---|---|
| **01. Custom IoC Container Implementation** | Building an IoC Container in Java, Reflection, `@Component`/`@Inject`, 3-Level Cache Circular Dependency Resolution | [`01_ioc_container_implementation_from_scratch.md`](./01_ioc_container_implementation_from_scratch.md) |
| **02. Bean Lifecycle & Scopes** | `BeanDefinition`, Instantiation Pipeline, `BeanPostProcessor`, `@PostConstruct`/`@PreDestroy`, Scopes | [`02_bean_lifecycle_scopes_and_post_processors.md`](./02_bean_lifecycle_scopes_and_post_processors.md) |
| **03. Servlet Container Internals** | Servlet Lifecycle (`init`/`service`/`destroy`), `FilterChain`, `ContextLoaderListener`, `WebApplicationContext` Bridge | [`03_servlet_container_internals_and_servlet_lifecycle.md`](./03_servlet_container_internals_and_servlet_lifecycle.md) |

---

## 🎨 Visual Overview: IoC & Servlet Container Integration Architecture

```mermaid
flowchart TD
    subgraph Servlet Container Layer (Tomcat / Jetty)
        Server["Web Server Socket Listener"] --> ServletEngine["Servlet Container Engine"]
        ServletEngine --> Listener["ContextLoaderListener (ServletContextListener)"]
        ServletEngine --> Dispatcher["DispatcherServlet (Front Controller)"]
        
        subgraph Filter Chain Processing
            Dispatcher --> Filter1["SecurityFilter"]
            Filter1 --> Filter2["LoggingFilter"]
            Filter2 --> ServletService["HttpServlet.service()"]
        end
    end

    subgraph IoC Container Layer (Spring / Custom IoC)
        Listener -- Bootstraps --> IoC["WebApplicationContext (IoC Container)"]
        IoC --> BeanRegistry["BeanDefinitionRegistry"]
        
        subgraph Bean Lifecycle Pipeline
            BeanRegistry --> Instantiate["Reflection Instantiation"]
            Instantiate --> Inject["Dependency Injection (@Inject / 3-Level Cache)"]
            Inject --> PostProcess["BeanPostProcessor (AOP Proxies & @PostConstruct)"]
            PostProcess --> SingletonMap["singletonObjects Map (Fully Initialized Beans)"]
        end
    end

    ServletService <== Resolves Controller Beans ==> SingletonMap
```
