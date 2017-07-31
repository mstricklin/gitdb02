// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

public interface Proxied  {
    IKeyed<Integer> impl();
    IKeyed<Integer> mutable();

}
