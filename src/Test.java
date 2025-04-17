import kakkoiichris.kotoba.Console;

public class Test {
    public static void main(String[] args) {
        var console = new Console();

        console.open();

        while (console.isOpen()) {
            var input = console.readLine();

            if (input.isEmpty()) {
                console.close();
            }
            else {
                console.write(input.get());
            }
        }

        console.close();
    }
}
