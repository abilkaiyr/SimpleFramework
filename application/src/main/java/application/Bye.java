package application;

import framework.api.Path;

public class Bye {

    @Path("/bye")
    public String sayBye() {
        return "Bye!";
    }


    @Path("/bye")
    public String gavBye() { return "Bye!"; }

}
