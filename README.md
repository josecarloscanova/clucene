# clucene
=======

Elements to make lucene work on a blobStore like Windows Azure BlobStorage

## Usage

all you have to do is put your infos in a config.json file at the root of the project here is what a config file should look like:

    {
        "azure": {
            "DefaultEndpointsProtocol":"the protocol",
            "AccountName":"account name",
            "AccountKey":"key linked to your account"
        },
        "container": "folder to store the index",
        "crawlerFolder": "where crawl4j will put its data", 
        "downloadFolder": "where the index will be downloaded"
    }



------------------------

We use Travis for continuous integration! 

[![Build Status](https://secure.travis-ci.org/lahabana/clucene.png)](http://travis-ci.org/lahabana/clucene)
