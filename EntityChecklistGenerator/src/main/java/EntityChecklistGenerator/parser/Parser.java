package EntityChecklistGenerator.parser;

import java.io.File;

/**
 * Generic parser interface for parsing files into objects of type T.
 */
public interface Parser<T> {
    /**
     * Parse the given file into an instance of T.
     * @param file the input file
     * @return the parsed object
     * @throws Exception on parsing errors
     */
    T parse(File file) throws Exception;
}
