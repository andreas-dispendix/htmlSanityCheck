package org.aim42.htmlsanitycheck

import org.aim42.htmlsanitycheck.check.BrokenCrossReferencesChecker
import org.aim42.htmlsanitycheck.check.Checker
import org.aim42.htmlsanitycheck.check.CheckerCreator
import org.aim42.htmlsanitycheck.collect.PerRunResults
import org.aim42.htmlsanitycheck.collect.SinglePageResults
import org.aim42.htmlsanitycheck.html.HtmlPage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Array

/**
 * runs one or several checks on HTML input
 */
class ChecksRunner {

    // we check a collection of files:
    private Collection<File> filesToCheck

    // where do we put our results
    private File checkingResultsDir

    // checker instances
    private Set<Checker> checkers

    // keep all results
    private PerRunResults resultsForAllPages


    private static Logger logger = LoggerFactory.getLogger(ChecksRunner.class);


    // convenience constructors, mainly  for tests
    // ------------------------------------------------
    public ChecksRunner(Set<Class> checkerCollection,
                        Set<File> filesToCheck,
                        File checkingResultsDir ) {
        this( checkerCollection, filesToCheck, checkingResultsDir, false)
    }

    // just ONE file to check and distinct directory
    public ChecksRunner(Set<Class> checkerCollection,
                        File fileToCheck,
                        File checkingResultsDir ) {
        this( checkerCollection, [fileToCheck], checkingResultsDir, false)
    }

    // with just ONE file to check
    public ChecksRunner(Set<Class> checkerCollection,
                        File fileToCheck ) {
        this( checkerCollection, [fileToCheck], fileToCheck.getParentFile(), false)
    }

    // with ONE checker and ONE file and target directory
    public ChecksRunner( Class checkerCollection,
                         File fileToCheck,
                         File checkingResultsDir ) {
        this( [checkerCollection], [fileToCheck], checkingResultsDir, false)
    }

    // with just ONE checker and ONE file...
    public ChecksRunner( Class checkerCollection,
                         File fileToCheck ) {
        this( [checkerCollection], [fileToCheck], fileToCheck.getParentFile(), false)
    }

    // with checkers and just ONE file...
    public ChecksRunner( Class checker,
                         Set<File> filesToCheck ) {
        this( [checker], filesToCheck, File.createTempDir(), false)
    }


    // standard constructor
    public ChecksRunner(
            Set<Class> checkerCollection,
            Set<File> filesToCheck,
            File checkingResultsDir,
            Boolean checkExternalResources
    ) {
        this.resultsForAllPages = new PerRunResults()

        this.filesToCheck = filesToCheck
        this.checkingResultsDir = checkingResultsDir

        this.checkers = CheckerCreator.createCheckerClassesFrom( checkerCollection )

        logger.debug("ChecksRunner created with ${checkerCollection.size()} checkers for ${filesToCheck.size()} files")
    }


    /**
     *  performs all configured checks on a single HTML file.
     *
     *  Creates a {@link org.aim42.htmlsanitycheck.collect.SinglePageResults} instance to keep checking results.
     */
    public SinglePageResults performChecksForOneFile(File thisFile) {

        // the currently processed (parsed) HTML page
        HtmlPage pageToCheck = HtmlPage.parseHtml(thisFile)

        // initialize results for this page
        SinglePageResults collectedResults =
                new SinglePageResults(
                        pageFilePath: thisFile.canonicalPath,
                        pageFileName: thisFile.name,
                        pageTitle: pageToCheck.getDocumentTitle(),
                        pageSize: pageToCheck.documentSize
                )

        // apply every checker to this page
        //checkers.each { checker ->
        //    def singleCheckResults = checker.performCheck( pageToCheck )

        //}

        def singleCheckResults = new BrokenCrossReferencesChecker().performCheck( pageToCheck )

        collectedResults.addResultsForSingleCheck( singleCheckResults )

        /*
        collectedResults.with {
            addResultsForSingleCheck(missingImageFilesCheck(baseDir))
            addResultsForSingleCheck(duplicateIdCheck())
            addResultsForSingleCheck(brokenCrossReferencesCheck())
            addResultsForSingleCheck(missingLocalResourcesCheck(baseDir))
            addResultsForSingleCheck(missingAltAttributesCheck())
        }
        */

        return collectedResults
    }

    /**
     * performs all configured checks on pageToCheck
     */
    public PerRunResults performChecks() {

        logger.debug "entered performChecks"

        filesToCheck.each { file ->
               resultsForAllPages.addPageResults(
                    performChecksForOneFile(file))
        }

        // after all checks, stop the timer...
        resultsForAllPages.stopTimer()

        // and then report the results
        reportCheckingResultsOnConsole()
        reportCheckingResultsAsHTML(checkingResultsDir.absolutePath)
    }

}

/************************************************************************
 * This is free software - without ANY guarantee!
 *
 *
 * Copyright Dr. Gernot Starke, arc42.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *********************************************************************** */
