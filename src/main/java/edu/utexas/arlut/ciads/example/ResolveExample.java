// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.example;

import java.io.IOException;

import edu.utexas.arlut.ciads.repo.GitRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
public class ResolveExample {

    public static final String BASELINE_TAG = "baseline";

    public static void main(String[] args) throws GitAPIException, IOException {
        GitRepository gr = GitRepository.init("t.git");

        RevCommit baseline = gr.getCommit(BASELINE_TAG+"^{}");
        RevCommit context = baseline;
        // TODO: check on baseline existence...
        log.info("Baseline {}", baseline.abbreviate(10).name());

        Repository r = gr.repo();
        log.info("01 {}", r.resolve(baseline.name() + ":01").name());
        log.info("01/02 {}", r.resolve(baseline.name() + ":01/02").name());
        log.info("01/02/03 {}", r.resolve(baseline.name() + ":01/02/03").name());
    }
}
