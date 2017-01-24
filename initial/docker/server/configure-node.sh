set -m

/entrypoint.sh couchbase-server &

sleep 15

curl -v -X POST http://127.0.0.1:8091/pools/default -d memoryQuota=300 -d indexMemoryQuota=300

curl -v http://127.0.0.1:8091/node/controller/setupServices -d services=kv%2cn1ql%2Cindex

curl -v http://127.0.0.1:8091/settings/web -d port=8091 -d username=Administrator -d password=password

curl -i -u Administrator:password -X POST http://127.0.0.1:8091/settings/indexes -d 'storageMode=memory_optimized'

if [ "$TYPE" = "worker" ]; then

    couchbase-cli server-add --cluster=couchbase-master:8091 --user=Administrator --password=password --server-add=`hostname -i` --server-add-username=Administrator --server-add-password=password

else

    curl -v -u Administrator:password -X POST http://127.0.0.1:8091/pools/default/buckets -d name=devnexus -d bucketType=couchbase -d ramQuotaMB=256 -d authType=sasl

    sleep 15

    curl -v http://127.0.0.1:8093/query/service -d 'statement=create primary index on `devnexus`'

fi;

fg 1
