# clucene
=======

A lucene based cloud search engine which stores its index in a Windows Azure Blob Storage.

The name should be changed as clucene is already the name of the C port of lucene. clucene is supposed to be the contraction of cloud and Lucene. 

This project started (and is still) a university project the end of it is December 2012 so sorry for the mess I may leave in the repo before then (promise I'll clean ;) )
Please feel free to comment I'm pretty new to Lucene and I'm sure there's many wrong things!

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

    {
          "port": 7050,
          "azure": {
              "defaultEndpointsProtocol":"http",
              "accountName":"clucene2",
              "accountKey":"azure key"
          },
          "indexer": {
              "crawler": {
                  "directory": "/mnt/resource/pages",
                  "queueSize": 10,
                  "nbThreads": 1
              },
              "indexer": {
                  "commitFrequency": 5000,
                  "folder": "/mnt/resource/index",
                  "regular": false,
                  "container": "index",
                  "queueSize": 50,
                  "nbThreads": 3,
                  "bufferSize": 512
              }, 
              "stats": {
                   "file": "stats/commit10s3th512mbSearch.csv",
                   "frequency": 1000,
             }
         }
    }

For a searcher:

    {
        "port": 7051,
        "azure": {
            "defaultEndpointsProtocol":"http",
            "accountName":"clucene2",
            "accountKey":"azure key"
        },
        "searcher": {
            "folder": "/mnt/resource/index",
            "container": "index"
        }
    }

Compulsory parameters in the JSON:

*   TODO

Once your configuration file is written you can either put it next to your executable jar or specify it to the executable:

    java -jar target/server-1.0-jar-with-dependencies.jar target/config.json 

## Using the indexer

TODO

## Using the searcher

TODO

## TODOs

This is a list of things that can be improved:
*		add much more unit tests
*		refactor rename and clean code
*		add compression on the nodes
*		Improve the error handling (currently most of it is just dumping the stacktrace)
*		get some sub projects (like utils) out of clucene
*		Make abstraction of the cloud service to make usable with S3 for example

------------------------

We use Travis for continuous integration! 

[![Build Status](https://secure.travis-ci.org/lahabana/clucene.png)](http://travis-ci.org/lahabana/clucene)
