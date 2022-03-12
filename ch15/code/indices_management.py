import elasticsearch
import os
import ssl

es = elasticsearch.Elasticsearch(
    hosts=os.environ.get("ES_HOST", "https://localhost:9200"),
    basic_auth=(
        os.environ.get("ES_USER", "elastic"),
        os.environ.get("ES_PASSWORD", "password"),
    ),
    ssl_version=ssl.TLSVersion.TLSv1_2,
    verify_certs=False,
)

index_name = "my_index"

if es.indices.exists(index=index_name):
    es.indices.delete(index=index_name)

es.indices.create(index=index_name)

es.cluster.health(wait_for_status="yellow")

es.indices.close(index=index_name)

es.indices.open(index=index_name)

es.cluster.health(wait_for_status="yellow")

es.indices.forcemerge(index=index_name)

es.indices.delete(index=index_name)
