Rollarcus
===

This is Rollarcus: an __experimental fork of [Apache Roller](https://github.com/apache/roller)__.
So far, there are two experiments here, each in its own branch:

1) [shiro_not_spring](https://github.com/snoopdave/rollarcus/tree/shiro_not_spring): is a branch of Roller's trunk that has been modified to use Apache Shiro for security instead of Spring. Also, all Spring depdenencies have been removed.

2) [jaxrs_not_struts](https://github.com/snoopdave/rollarcus/tree/jaxrs_not_struts): is a branch of Roller's trunk that has been modified to add a REST API, powered by Apache CXF, Apache Shiro and includes Arquillian-powered tests for the REST API. Someday this REST API could power a new JavaScript based web interface for Roller and allow the project to move on from Struts.

2) [bootstrap-ui](https://github.com/snoopdave/rollarcus/tree/bootstrap-ui): the idea is to modernize the Roller editor/admin UI and make it more consistent by using Bootstrap instead. This should be much less labor intensive than designing and implementing a new REST API and building a UI baesed on that.



