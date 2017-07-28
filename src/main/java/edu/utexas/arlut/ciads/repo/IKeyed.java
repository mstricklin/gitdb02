// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

public interface IKeyed<T> {
    T id();
    String getType();
}
