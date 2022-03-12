import urllib3
urllib3.disable_warnings()

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

es.indices.put_mapping(
    index=index_name,
    body={
        "properties": {
            "uuid": {"type": "keyword"},
            "title": {"type": "text", "term_vector": "with_positions_offsets"},
            "parsedtext": {"type": "text", "term_vector": "with_positions_offsets"},
            "nested": {
                "type": "nested",
                "properties": {
                    "num": {"type": "integer"},
                    "name": {"type": "keyword"},
                    "value": {"type": "keyword"},
                },
            },
            "date": {"type": "date"},
            "position": {"type": "integer"},
            "name": {"type": "text", "term_vector": "with_positions_offsets"},
        }
    },
)

mappings = es.indices.get_mapping(index=index_name)

print(mappings)

es.indices.delete(index=index_name)
