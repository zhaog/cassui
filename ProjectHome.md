# Cassandra UI - a web based interface over Cassandra #

## Getting Started ##
  1. Download cassui.war from downloads section and drop it into any servlet container.
  1. Go to `http://host of tomcat server:port/cassui`
  1. Add Cassandra server's IP address (and port if not 9160 with the separator being a colon)
    * for example 127.0.0.1 or 192.168.1.1:9160 or mycassandraserver.com:9160
  1. Start browsing through the keyspaces and individual column family.

## Main Features/Functionalities ##
### Release 0.3 ###
  * Added support for Cassandra 1.0 and CQL support of simple column types. Download cassui-0.3-cassandra1.0.war under downloads or tags under svn.
### Release 0.2 ###
  * Added support for Cassandra 0.7 and secure login.
### Release 0.1 ###
  * By default first 10 records are shown. The drop down can be used to get more records, but should be used for caution for large number of records. **This listing, however, would work only for ordered partitioning for now**.
  * Use 'Count Only' button for getting the total count for the column family.
  * A single key lookup can be used to get a single row based on the column key

## FAQ ##
  * This has been tested on tomcat 6X in internal as well as in Cloud environment
  * Listing works for ordered partitioning only
  * This is build using Cassandra Thrift Api with grails as the web application framework
  * Tested for Cassandra 0.6.3 and 0.6.4. NOT TESTED ON 0.7.x- it can be tried by changing the cassandra and thrift jar libraries
  * The grails code is available in svn repository

## Roadmap ##
  * Add paging support
  * Add key range support
  * Provide support for random partitioning
  * Keep on adding support for CQL


## About Us ##
cassui is backed by Impetus Labs - iLabs. iLabs is a R&D consulting division of Impetus Technologies (http://www.impetus.com). iLabs focuses on innovations with next generation technologies and creates practice areas and new products around them. iLabs is actively involved working on High Performance computing technologies, ranging from distributed/parallel computing, Erlang, grid softwares, GPU based software, Hadoop, Hbase, Cassandra, CouchDB and related technologies. iLabs is also working on various other Open Source initiatives.

## Why to build/use CassUI ##
The need for this tool came up since we could not find a suitable java web based interface for browsing Cassandra in a Cloud environment. We also needed it to verify our [kundera](http://kundera.googlecode.com) solution which is the first JPA compliant ORM over Cassandra.