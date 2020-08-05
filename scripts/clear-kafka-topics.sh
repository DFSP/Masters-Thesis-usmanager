topics=$($(dirname $0)/kafka-run-class.sh kafka.admin.TopicCommand --list --bootstrap-server localhost:9092)
for topic in $topics
do
	if [[ ! $topic =~ "__" ]]
	then
		$(dirname $0)/kafka-run-class.sh kafka.admin.TopicCommand --zookeeper localhost:2181 --delete --topic $topic
	fi
done