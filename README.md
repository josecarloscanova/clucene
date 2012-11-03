# clucene
=======

A lucene based cloud search engine which stores its index in a Microsoft Azure Blob Storage.

The name should be changed as clucene is already the name of the C++ port of lucene. clucene is supposed to be the contraction of cloud and Lucene. 

# Usage

## Setting up the nodes

The project is trying to be as configurable as possible from the inside to avoid recompiling for each node.
We use [JSON](http://json.org/) as a configuration format for its simplicity.

A configuration file look like:

    {
        "port": 7050,
        "azure": {
            "DefaultEndpointsProtocol":"the protocol",
            "AccountName":"account name",
            "AccountKey":"key linked to your account",
            "container": "folder to store the index",
        },
        ....
    }

A node can either be an indexer (will crawl and index document) or a searcher (will search queries sent to it). To set that just change the configuration file to right node.

For an indexer:

    "indexer": {
        "crawler": {
            "folder": "where crawler4j will put the data",
            "seed": "http://en.wikipedia.org",
            "domain": "http://en.wikipedia.org", // Sticks to the pages starting with
            "nbCrawler": 2
        },
        "downloadFolder": "../index/cloud",
        nbThread: 3,
        commitFreq: 100
    } 

For a searcher:

    "searcher": {
        // To be completed
    }

Compulsory parameters in the JSON:

*   port
*   azure (with DefaultEndpointProtocol, AccountName, AccountKey, container)
*   For an indexer:
    *   crawler (folder, seed, domain, nbCrawler)
    *   downloadFolder
*   For a searcher:
    * To be completed

Once your configuration file is written you can either put it next to your executable jar or specify it to the executable:

    java -jar target/server-1.0-jar-with-dependencies.jar target/config.json 

## Using the indexer

## Using the searcher

------------------------

We use Travis for continuous integration! 

[![Build Status](https://secure.travis-ci.org/lahabana/clucene.png)](http://travis-ci.org/lahabana/clucene)
