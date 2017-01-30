set -m

/entrypoint.sh couchbase-server &

sleep 15

# Step 1 - Provision a Couchbase Server Node with HTTP
# Hint
# Define memory quotoas, services, a username and password, and index type
# CUSTOM CODE HERE

if [ "$TYPE" = "worker" ]; then

    # Step 3 - Add a New Node to a Cluster
    # Hint
    # Know the hostname of another node and the current node
    # CUSTOM CODE HERE

else

    # Step 2 - Create a New Bucket with N1QL Indexes
    # Hint
    # Create two buckets without exceeding the memory limits from the first step
    # CUSTOM CODE HERE

fi;

fg 1
