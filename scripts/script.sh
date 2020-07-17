gnome-terminal --tab --title="replication-migration" -- \
sh -c "sudo java -Djava.security.egd=file:/dev/./urandom -jar \
'$HOME/Dissertação/Código/Microservices management main components/replication-migration/target/replication-migration-0.0.1.jar' \
--dockermaster=127.0.0.1; $SHELL"

gnome-terminal --tab --title="node_exporter" -- sh -c 'sudo node_exporter; $SHELL'
gnome-terminal --tab --title="nginx " -- sh -c 'sudo cd ~/temp3; ls; $SHELL'
