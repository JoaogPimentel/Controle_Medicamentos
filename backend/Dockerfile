# Build multi-stage: compila com o JDK e roda com um JRE enxuto.

# --- Stage 1: compilação ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
# Dependências e fontes (lib/backup é excluído pelo .dockerignore).
COPY lib ./lib
COPY src ./src
# Compila todos os .java no classpath das libs (igual ao build manual do README).
RUN find src/main/java -name '*.java' > sources.txt \
 && javac -encoding UTF-8 -cp "lib/*" -d out/classes @sources.txt

# --- Stage 2: runtime ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY lib ./lib
COPY --from=build /app/out/classes ./out/classes
# Main.java resolve `new File("src/main/webapp")` relativo ao WORKDIR.
COPY src/main/webapp ./src/main/webapp
EXPOSE 8080
# Classpath usa ':' (separador Linux). A config vem do ambiente (DotEnv lê env
# de processo com precedência — não é preciso .env dentro do container).
CMD ["java", "-cp", "out/classes:lib/*", "Main"]
