spark-3.2.1-bin-hadoop3.2/bin/spark-shell \
    --conf spark.es.index.auto.create=true \
    --conf spark.es.nodes.wan.only=true \
    --conf spark.es.net.http.auth.user=$ES_USER \
    --conf spark.es.net.http.auth.pass=$ES_PASSWORD 
    # \
    # --conf spark.es.net.ssl=true \
    # --conf spark.es.net.ssl.cert.allow.self.signed=true #\
    # --conf spark.es.net.ssl.keystore.location=/home/alberto/Projects/es/Elasticsearch-8.x-Cookbook./elasticsearch/config/certs/http_ca.crt
