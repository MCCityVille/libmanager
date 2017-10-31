package de.mccityville.libmanager.bungeecordexample;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.logging.Logger;

public class ExampleClass {

    private ExampleClass() {
    }

    public static void execute(Logger logger) {
        MutableIntList demo = new IntArrayList();
        demo.addAll(4, 8, 15, 16, 23, 42);

        logger.info("The numbers are: " + demo.makeString(", "));
        logger.info("The sum of the numbers are: " + demo.sum());
        logger.info("Don't play lotto with these numbers.");
    }
}
