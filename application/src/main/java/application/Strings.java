package application;

/**
 * Created by zafersuslu on 9/27/16.
 */
public enum Strings {

    STRING_ONE("ONE"),
    STRING_TWO("TWO")
    ;

    private final String text;

    private Strings(final String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return text;
    }
}
