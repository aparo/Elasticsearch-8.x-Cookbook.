curl  --user $ES_USER:$ES_PASSWORD --insecure -XDELETE "https://localhost:9200/index-agg"

curl  --user $ES_USER:$ES_PASSWORD --insecure -XPUT "https://localhost:9200/index-agg" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "name": {
        "term_vector": "with_positions_offsets",
        "store": true,
        "type": "text"
      },
      "title": {
        "term_vector": "with_positions_offsets",
        "store": true,
        "type": "text"
      },
      "parsedtext": {
        "term_vector": "with_positions_offsets",
        "store": true,
        "type": "text"
      },
      "tag": {
        "type": "keyword",
        "store": true
      },
      "date": {
        "type": "date",
        "store": true
      },
      "position": {
        "type": "geo_point",
        "store": true
      },
      "uuid": {
        "store": true,
        "type": "keyword"
      }
    }
  },
  "settings": {
    "index.number_of_replicas": 1,
    "index.number_of_shards": 1
  }
}'

curl  --user $ES_USER:$ES_PASSWORD --insecure -XPOST "https://localhost:9200/_bulk" -H 'Content-Type: application/x-ndjson' --data-binary @bulk.txt