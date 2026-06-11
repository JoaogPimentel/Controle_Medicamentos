package servlet.filter;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AuthFilterRegistrar implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        FilterRegistration.Dynamic reg =
            sce.getServletContext().addFilter("authFilter", new AuthFilter());

        if (reg != null) {
            reg.addMappingForUrlPatterns(null, false, "/*");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
