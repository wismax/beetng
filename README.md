BeetNG
======

BeetNG is a fork of [beet](http://beet.sf.net) a sourceforge hosted Java/Spring-based application behavior and performance data recording tool.

As Beet seems no longer maintained by its original authors, this fork was started. The main objectives are:
* fix and add support for newer version of Spring (3.0 and 3.1)
* add new data persisters
* add tools to ease data analysis
* implement a VisualVM integration
* add JEE6 support
* replace the Easyant build tool by a more standard Maven integration

Original description
--------------------
> This is the original description of the tool, as extracted from the sourceforge website.

Beet records user behavior and performance data for your Spring-based Java application.  It can thus help you to analyze usage patterns and research production performance issues.  Beet requires Spring Framework 2.0 or 2.5.

Visit the Downloads page to grab a copy, take the tutorial, and/or read the Quick Start chapter of the User Guide to enable it in your application.

Beet is freely available to use under the terms of the Mozilla Public License, v1.1.  It was developed and is maintained by Mantis Technology Group, Inc.

Features
* Record Java method calls, SQL statements, and HTTP requests, or add your own events
* Simple configuration, zero code modification required
* Know immediately which user and session caused each event and when
* JMX performance tracking and administration
* Record data as XML, compressed binary XML, directly to an RDBMS, or write your own storage
* Flexible ETL and log manipulation tools for compressed binary XML
* Low resource overhead, appropriate for production systems
