#!/bin/sh

BLOG_CLASSPATH=""
for f in "lib/*.jar
do
  BLOG_CLASSPATH="BLOG_CLASSPATH":$f
done
BLOG_CLASSPATH="$JAVA_HOME/lib/tools.jar":"$BLOG_CLASSPATH":build/crawler.jar:"$CLASSPATH"

$JAVA_HOME/bin/java -classpath "$BLOG_CLASSPATH" edu.indiana.cs.webmining.blog.BlogProcessingSystem
