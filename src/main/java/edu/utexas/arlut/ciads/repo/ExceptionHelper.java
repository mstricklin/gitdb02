// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;

public class ExceptionHelper {
    public static class DataStoreCreateAccessException extends Exception {
        DataStoreCreateAccessException(String name, Throwable cause) {
            super("Error accessing "+name, cause);
        }
    }
    public static RefAlreadyExistsException createBranchAlreadyExistsException(String name) {
        return new RefAlreadyExistsException("branch '"+name+"' already exists");
    }
    public static RefNotFoundException createRefNotFoundException(String name) {
        return new RefNotFoundException(name + "not found");
    }
    public static DataStoreCreateAccessException createDataStoreCreateAccessException(String name, Throwable cause) {
        return new DataStoreCreateAccessException(name, cause);
    }
}

