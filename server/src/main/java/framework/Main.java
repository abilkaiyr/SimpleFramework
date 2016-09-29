package framework;

import framework.api.HeaderParam;
import framework.api.MyEnum;
import framework.api.Path;
import framework.api.QueryParam;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import io.undertow.util.Headers;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.scene.input.KeyCode.T;

public class Main {

    public String pageNotFound() {
        return "Page not found\n";
    }


    //i am testin git

    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler((HttpServerExchange exchange) -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");


                    try {
                        Consumer println = System.out::println;

                        // For this to work, first do gradle build of application,
                        // so that application.jar is created
                        File file = new File("application/build/libs/application.jar");

                        ClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});

                        JarFile jarFile = new JarFile(file);

                        Stream<Class<?>> classes = jarFile.stream()
                                .map(entry -> entry.getName())
                                .filter(name -> name.endsWith(".class"))
                                .map(name -> name.replace('/', '.'))
                                .map(name -> name.substring(0, name.length() - ".class".length()))
                                .map(name -> {
                                    try {
                                        return classLoader.loadClass(name);
                                    } catch (ClassNotFoundException e) {
                                        throw new RuntimeException(e);
                                    }
                                });


                        Stream<Method> methods = classes.flatMap(c -> Stream.of(c.getDeclaredMethods()));

                        Stream<Method> methodsWithPath = methods.filter(m -> m.getAnnotation(Path.class) != null);

                        Map<String, List<Method>> methodByPath = methodsWithPath.collect(Collectors.groupingBy(m -> m.getAnnotation(Path.class).value()));


                        List<Method> pathMethods = methodByPath.getOrDefault(exchange.getRequestPath(), Arrays.asList(Main.class.getDeclaredMethod("pageNotFound")));

                        Method method = null;
                        if (pathMethods.size() > 1) {
                            throw new RuntimeException("These methods have the same value in @Path: " + pathMethods.toString());
                        } else {
                            method = pathMethods.get(0);
                        }

                        Stream<Parameter> paramsStream = Stream.of(method.getParameters());

                        Object[] arguments = paramsStream.map(p -> {

                            String paramName="", paramValue="";

                            if (p.getAnnotations()[0].annotationType().getName().equals(QueryParam.class.getName())) {
                                // this takes parameters from URL
                                QueryParam annotation = p.getAnnotation(QueryParam.class);
                                paramName = annotation.value();
                                paramValue = exchange.getQueryParameters().get(paramName).getFirst();
                            } else  {
                                //this takes others --> headers, cookies etc.
                                HeaderParam annotation2 = p.getAnnotation(HeaderParam.class);
                                paramName = annotation2.value();
                                paramValue = exchange.getRequestHeaders().get(paramName).getFirst();
                            }


                            Class<?> paramType = p.getType();

                            if (paramType.equals(Double.class)) {
                                return Double.valueOf(paramValue);
                            } else if (paramType.equals(Integer.class)) {
                                return Integer.valueOf(paramValue);
                            } else if (paramType.equals(Float.class)) {
                                return Float.valueOf(paramValue);
                            } else if (paramType.equals(Boolean.class)) {
                                return Boolean.valueOf(paramValue);
                            } else if (paramType.equals(MyEnum.class)) {
                                MyEnum g = MyEnum.values()[Integer.valueOf(paramValue)];
                                return g;
                            } else {
                                System.out.println("TYPE HERE --> " + paramType);
                                return paramValue;
                            }

                        }).toArray();

                        Object o = method.invoke(method.getDeclaringClass().newInstance(), arguments);
                        String result = String.valueOf(o);
                        exchange.getResponseSender().send(result);

                    } catch (Exception e) {
                        e.printStackTrace();
                        StringWriter writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        String errorMessage = writer.toString();
                        exchange.getResponseSender().send(errorMessage);
                    }

                }).build();
        server.start();
    }
}
