thread-dumper
=============

Super-simple thread-dump utility for watching JVM performance as you develop.

This is great if you're developing a Java webapp and want to see what's taking time. Thread-dumper
takes a thread dump every second and splits it by blank lines to identify individual thread stacks. 
This isn't revolutionary, but is remarkably useful. When something in your webapp takes a while to 
respond, a few clicks gives you a remarkably clear view of what's slowing you down. 
Simple, effective.

Requirements:

 You'll need "jstack" available on your path. This is a JVM tool.

Build:

    mvn assembly:assembly

Run:

    java -jar thread-dumper-1.0.0-jar-with-dependencies.jar &lt;pid&gt; [search terms]

Search terms:

 If you provide no search terms, thread-dumper will print out a full thread dump every second.
 Thread-dumper will show any "blocks of text" - typically thread stacks - that contain the search terms you specify.
 If you need to suppress something, use -&lt;search term&gt;.
 
Examples:

    java -jar thread-dumper-1.0.0-jar-with-dependencies.jar 6884 +com.mypackage -com.mypackage.BackgroundProcessor
  
is equivalent to:
   
    java -jar thread-dumper-1.0.0-jar-with-dependencies.jar 6884 com.mypackage -com.mypackage.BackgroundProcessor

I hope it's as useful for you as it is for me.

David Carboni
http://workdocx.com/
