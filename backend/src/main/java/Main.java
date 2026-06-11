import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import servlet.AlertaServlet;
import servlet.AuthServlet;
import servlet.CatalogoServlet;
import servlet.DashboardServlet;
import servlet.EstoqueServlet;
import servlet.HistoricoServlet;
import servlet.MedicamentoServlet;
import servlet.PosologiaServlet;
import servlet.VinculoServlet;
import servlet.filter.AuthFilterRegistrar;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector().setProperty("URIEncoding", "UTF-8");

        Context ctx = tomcat.addContext("",
            new File("src/main/webapp").getAbsolutePath());

        org.apache.catalina.Wrapper defaultServlet = ctx.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        ctx.addChild(defaultServlet);
        ctx.addServletMappingDecoded("/", "default");

        ctx.addApplicationListener(AuthFilterRegistrar.class.getName());

        Tomcat.addServlet(ctx, "dashboard",  new DashboardServlet());
        ctx.addServletMappingDecoded("/api/dashboard", "dashboard");

        Tomcat.addServlet(ctx, "auth",         new AuthServlet());
        ctx.addServletMappingDecoded("/api/auth/*",         "auth");

        Tomcat.addServlet(ctx, "medicamentos", new MedicamentoServlet());
        ctx.addServletMappingDecoded("/api/medicamentos/*", "medicamentos");

        Tomcat.addServlet(ctx, "alertas",      new AlertaServlet());
        ctx.addServletMappingDecoded("/api/alertas/*",      "alertas");

        Tomcat.addServlet(ctx, "vinculos",     new VinculoServlet());
        ctx.addServletMappingDecoded("/api/vinculos/*",     "vinculos");

        Tomcat.addServlet(ctx, "catalogo",     new CatalogoServlet());
        ctx.addServletMappingDecoded("/api/catalogo/*",     "catalogo");

        Tomcat.addServlet(ctx, "posologias",   new PosologiaServlet());
        ctx.addServletMappingDecoded("/api/posologias/*",   "posologias");

        Tomcat.addServlet(ctx, "historico",    new HistoricoServlet());
        ctx.addServletMappingDecoded("/api/historico/*",    "historico");

        Tomcat.addServlet(ctx, "estoque",      new EstoqueServlet());
        ctx.addServletMappingDecoded("/api/estoque/*",      "estoque");

        tomcat.start();
        System.out.println("API iniciada em http://localhost:8080");
        System.out.println("  Login:     POST http://localhost:8080/api/auth/login");
        System.out.println("  Dashboard: GET  http://localhost:8080/api/dashboard");
        tomcat.getServer().await();
    }
}
