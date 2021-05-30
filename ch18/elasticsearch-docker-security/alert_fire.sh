export ES_PASSWORD="SaBvTzVTRC0SpPvQtz7j"

CURRR_DATE=`date +"%Y-%m-%dT%H:%M:%S%z"`

for a in `seq 1000`;
 do 
  curl -k -XPUT "https://elastic:$ES_PASSWORD@0.0.0.0:9200/mybooks/_doc/$a" -H 'Content-Type: application/json' -d'{"in_stock":false,"tag":[],"name":"Valkyrie","date":"'$CURRR_DATE'","position":{"lat":-17.9940459163244,"lon":-15.110538312702941},"age":49,"metadata":[],"price":19.0,"description":""}'

done

