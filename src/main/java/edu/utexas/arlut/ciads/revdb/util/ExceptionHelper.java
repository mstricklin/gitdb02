// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.util;

import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.EntryExistsException;

public class ExceptionHelper {
    public static class DataStoreCreateAccessException extends Exception {
        DataStoreCreateAccessException(String name, Throwable cause) {
            super("Error accessing "+name, cause);
        }
        DataStoreCreateAccessException(String name) {
            super("Error accessing "+name);
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
    public static DataStoreCreateAccessException createDataStoreCreateAccessException(String name) {
        return new DataStoreCreateAccessException(name);
    }

    public static EntryExistsException createEntryExistsException(String name) {
        return new EntryExistsException("Branch "+name+" already exists");
    }
}

