VER=1.0.4

.PHONY: all clean doc release

all:
	javac -classpath src:lib/jep-2.3.0.jar:lib/djep-1.0.0.jar `find src -name "*.java"`
clean:
	rm -f `find src -name "*.class"`

doc:
	rm -rf doc/*
	javadoc -overview overview.html -classpath src:jep-2.3.0.jar:djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "Examples" "example.*" \
		peersim \
		peersim.cdsim \
		peersim.config \
		peersim.core \
		peersim.dynamics \
		peersim.edsim \
		peersim.graph \
		peersim.rangesim \
		peersim.reports \
		peersim.transport \
		peersim.util \
		peersim.vector \
		example.aggregation \
		example.loadbalance \
		example.edaggregation \
		example.hot \
		example.newscast 

docnew:
	rm -rf doc/*
	javadoc -overview overview.html -docletpath peersim-doclet.jar -doclet peersim.tools.doclets.standard.Standard -classpath src:jep-2.3.0.jar:djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "Examples" "example.*" \
		peersim \
		peersim.cdsim \
		peersim.config \
		peersim.core \
		peersim.dynamics \
		peersim.edsim \
		peersim.graph \
		peersim.rangesim \
		peersim.reports \
		peersim.transport \
		peersim.util \
		peersim.vector \
		example.aggregation \
		example.loadbalance \
		example.hot \
		example.edaggregation \
		example.newscast 


release: clean all docnew
	rm -fr peersim-$(VER)
	mkdir peersim-$(VER)
	cp -r doc peersim-$(VER)
	cp Makefile overview.html README CHANGELOG RELEASE-NOTES build.xml peersim-doclet.jar peersim-$(VER)
	mkdir peersim-$(VER)/example
	cp example/*.txt peersim-$(VER)/example
	mkdir peersim-$(VER)/src
	cp --parents `find src/peersim src/example -name "*.java"` peersim-$(VER)
	cd src ; jar cf ../peersim-$(VER).jar `find peersim example -name "*.class"`
	mv peersim-$(VER).jar peersim-$(VER)
	cp jep-2.3.0.jar peersim-$(VER)
	cp djep-1.0.0.jar peersim-$(VER)

tar:	clean
	mkdir danra-srinivk-nemanjla/
	mkdir -p danra-srinivk-nemanjla/scripts
	mkdir -p danra-srinivk-nemanjla/src
	mkdir -p danra-srinivk-nemanjla/plot
	cp src/example/gossip/* danra-srinivk-nemanjla/src/
	cp -r plot/* danra-srinivk-nemanjla/plot/
	cp README.md report.pdf collect* danra-srinivk-nemanjla/
	cp example/Shuffle* danra-srinivk-nemanjla/scripts
	tar -cvzf danra-srinivk-nemanjla.tar.gz danra-srinivk-nemanjla
	rm -rf danra-srinivk-nemanjla
