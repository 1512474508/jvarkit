#
# libraries to be downloaded by maven:
#
lib.dir?=lib

avro.tools.version = 1.7.7
avro.libs = $(lib.dir)/org/apache/avro/avro-tools/${avro.tools.version}/avro-tools-${avro.tools.version}.jar

httpclient.libs  = \
	$(lib.dir)/org/apache/httpcomponents/httpcore/4.4.1/httpcore-4.4.1.jar \
	$(lib.dir)/org/apache/httpcomponents/httpclient/4.5/httpclient-4.5.jar \
	$(lib.dir)/commons-codec/commons-codec/1.10/commons-codec-1.10.jar \
	$(lib.dir)/commons-logging/commons-logging/1.2/commons-logging-1.2.jar

all_maven_jars = $(sort  ${httpclient.libs} ${avro.libs})

${all_maven_jars}  : 
	mkdir -p $(dir $@) && wget -O "$@" "http://central.maven.org/maven2/$(patsubst ${lib.dir}/%,%,$@)"

