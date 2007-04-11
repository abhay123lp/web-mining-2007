#!/bin/sh

BLOG_CLASSPATH=""
for f in lib/*.jar
do
  BLOG_CLASSPATH="$BLOG_CLASSPATH":$f
done
BLOG_CLASSPATH="$JAVA_HOME/lib/tools.jar":"$BLOG_CLASSPATH":build/crawler.jar:"$CLASSPATH"

echo $BLOG_CLASSPATH

$JAVA_HOME/bin/java -Xmx1024m -cp "$BLOG_CLASSPATH" edu.indiana.cs.webmining.blog.BlogProcessingSystem Blog
