package application;

import framework.api.MyEnum;
import framework.api.QueryParam;
import framework.api.Path;

public class Hello {
    @Path("/hi")
    public String sayHello() {
        return "Hi!";
    }

    @Path("/hello")
    public String sayHello(@QueryParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @Path("/sum")
    public String sum(@QueryParam("a") String a, @QueryParam("b") String b) {
        return "Sum!";
    }


    @Path("/typedSum")
    public Double sum(@QueryParam("a") Double a, @QueryParam("b") Double b) {
        return a + b;
    }


    private enum Alignment {LEFT, RIGHT};

    @Path("/passEnum")

    public String passEnum(@QueryParam("index") MyEnum a) {
        return "MyEnum." + a.name();
    }

}
