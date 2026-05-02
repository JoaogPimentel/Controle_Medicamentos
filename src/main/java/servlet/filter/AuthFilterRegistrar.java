package servlet.filter;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Registra o AuthFilter programaticamente via a API padrão do Jakarta Servlet.
 * É invocado automaticamente pelo Tomcat durante a inicialização do contexto.
 */
public class AuthFilterRegistrar implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        FilterRegistration.Dynamic reg =
            sce.getServletContext().addFilter("authFilter", new AuthFilter());

        if (reg != null) {
            // Intercepta TODAS as requisições da aplicação
            reg.addMappingForUrlPatterns(null, false, "/*");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
