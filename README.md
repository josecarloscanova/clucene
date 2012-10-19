# clucene
=======

Elements to make lucene work on a blobStore like Windows Azure BlobStorage

## Usage

all you have to do is put your infos in a config.json file at the root of the project here is what a config file should look like:

{
    "azure": {
        "DefaultEndpointsProtocol":"the protocol you want to use",
        "AccountName":"the account name of your blobstore",
        "AccountKey":"The key linked to your account"
    },
    "container": "the name of the folder where you will store the index",
    "downloadDir": "where you want to put your index when you download it to the node's (this is for debugging"
}



------------------------

We use Travis for continuous integration! 

[![Build Status](https://secure.travis-ci.org/lahabana/clucene.png)](http://travis-ci.org/lahabana/clucene)
