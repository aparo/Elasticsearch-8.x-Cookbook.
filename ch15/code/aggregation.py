from utils import create_and_add_mapping, populate
from pprint import pprint
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


create_and_add_mapping(es, index_name)
populate(es, index_name)

results = es.search(
    index=index_name,
    body={"size": 0, "aggs": {"pterms": {"terms": {"field": "name", "size": 10}}}},
)
pprint(results)

results = es.search(
    index=index_name,
    body={
        "size": 0,
        "aggs": {
            "date_histo": {"date_histogram": {"field": "date", "interval": "month"}}
        },
    },
)
pprint(results)

es.indices.delete(index=index_name)
